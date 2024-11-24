package com.cp.classpay.service.cache;

import com.cp.classpay.api.input.booking.BookingClassRequest;
import com.cp.classpay.api.input.class_.CancelBookingRequest;
import com.cp.classpay.api.input.waitlist.WaitlistEntry;
import com.cp.classpay.api.output.booking.BookingConfirmedClassesResponse;
import com.cp.classpay.commons.enum_.BookingStatus;
import com.cp.classpay.entity.*;
import com.cp.classpay.entity.Class;
import com.cp.classpay.exceptions.ApiBusinessException;
import com.cp.classpay.exceptions.BookingConcurrencyException;
import com.cp.classpay.exceptions.InsufficientCreditsException;
import com.cp.classpay.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
@Slf4j
public class BookingCacheService {

    @Autowired
    private BookingLockService bookingLockService;
    @Autowired
    private CreditDeductionService creditDeductionService;
    @Autowired
    private UserPackageCacheService userPackageCacheService;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private ClassCacheService classCacheService;
    @Autowired
    private WaitlistCacheService waitlistCacheService;
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private ClassRepo classRepo;
    @Autowired
    private RefundRepo refundRepo;
    @Autowired
    private UserPackageRepo userPackageRepo;

    public List<BookingConfirmedClassesResponse> bookingConfirmedClasses(String jwtToken) {
        User user = userCacheService.getUser(jwtToken);
        List<Booking> bookingList = bookingRepo.findAllByUser_UserIdAndStatus(user.getUserId(), BookingStatus.BOOKED);
        return bookingList.stream()
                .map(data -> BookingConfirmedClassesResponse.toBookingResponse(data))
                .toList();
    }

    public BookingStatus bookingClass(String jwtToken, BookingClassRequest bookingClassRequest) {
        User user = userCacheService.getUser(jwtToken);
        log.info("Booking class for user {} with request: {}", user.getUserId(), bookingClassRequest);

        // Fetch the class to be booked
        Class classEntity = classCacheService.getClassEntity(bookingClassRequest.classId()).orElseThrow(() -> new ApiBusinessException("Class not found"));
        log.debug("Fetched class entity for class ID {}", bookingClassRequest.classId());

        Booking booking = processBooking(user, classEntity, bookingClassRequest.waitlistPrefer());
        return booking.getStatus();
    }

    protected Booking processBooking(User user, Class classEntity, boolean waitlistPrefer) {
        boolean normalBooking = true;
        //If the class's available slot is actually 0 and user want to book the class with waitlist prefer true
        if (classEntity.getAvailableSlots() == 0 && waitlistPrefer) {
            normalBooking = false;
        }

        log.info("Attempting normal booking for user {} on class {}", user.getUserId(), classEntity.getClassId());
        List<UserPackage> userPackages = userPackageCacheService.getUserPackagesByCountry(user.getUserId(), user.getCountry());

        // Validate sufficient credits
        int totalAvailableCredits = userPackages.stream().mapToInt(UserPackage::getRemainingCredits).sum();
        log.debug("Total available credits for user {}: {}", user.getUserId(), totalAvailableCredits);

        if (totalAvailableCredits < classEntity.getRequiredCredits()) {
            log.warn("Insufficient credits for user {} for class {}", user.getUserId(), classEntity.getClassId());
            throw new InsufficientCreditsException("Not enough credits for booking");
        }

        boolean lockAcquired = bookingLockService.lockForBooking(user.getUserId(), classEntity.getClassId());
        if (!lockAcquired) {
            log.warn("Booking lock not acquired for user {} on class {}", user.getUserId(), classEntity.getClassId());
            throw new BookingConcurrencyException("Booking is already in process for this user and class.");
        }

        try {
            Booking booking = creditDeductionService.deductCredits(user, classEntity, userPackages, normalBooking);
            creditDeductionService.updateCacheAfterDeduct(user, classEntity);
            log.info("Booking confirmed for user {} on class {}", user.getUserId(), classEntity.getClassId());
            return booking;
        } finally {
            bookingLockService.releaseBookingLock(user.getUserId(), classEntity.getClassId());
            log.debug("Released booking lock for user {} on class {}", user.getUserId(), classEntity.getClassId());
        }
    }

    @Transactional
    public Booking cancelBooking(String jwtToken, CancelBookingRequest cancelBookingRequest) {
        User user = userCacheService.getUser(jwtToken);
        log.info("Cancelling booking for user {} with request: {}", user.getUserId(), cancelBookingRequest);

        Booking booking = bookingRepo.findById(cancelBookingRequest.bookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        log.debug("Fetched booking for ID {}", cancelBookingRequest.bookingId());

        if (!booking.getUser().equals(user)) {
            log.error("User {} is not the owner of booking {}", user.getUserId(), booking.getBookingId());
            throw new IllegalStateException("Booking is not owner of user");
        }

        if (booking.isCanceled()) {
            log.warn("Booking {} is already canceled", booking.getBookingId());
            throw new IllegalStateException("Booking is already canceled");
        }

        booking.setStatus(BookingStatus.CANCELED);
        booking.setCanceled(true);
        booking.setCancellationTime(ZonedDateTime.now());

        // Update class availability
        Class classEntity = booking.getClassEntity();
        classEntity.setAvailableSlots(classEntity.getAvailableSlots() + 1);
        classRepo.save(classEntity);
        log.info("Updated available slots for class {} to {}", classEntity.getClassId(), classEntity.getAvailableSlots());


        // Refund credits
        List<BookingDetail> bookingDetails = booking.getBookingDetails();
        for (BookingDetail bookingDetail : bookingDetails) {
            UserPackage userPackage = bookingDetail.getUserPackage();
            userPackage.setRemainingCredits(userPackage.getRemainingCredits() + bookingDetail.getCreditsDeducted());
            userPackageRepo.save(userPackage);
            log.debug("Refunded {} credits to user package {} for booking detail {}", bookingDetail.getCreditsDeducted(), userPackage.getUserPackageId(), bookingDetail.getBookingDetailId());

            Refund refund = new Refund();
            refund.setUser(user);
            refund.setUserPackage(userPackage);
            refund.setCreditRefunded(bookingDetail.getCreditsDeducted());
            refund.setRefundTime(ZonedDateTime.now());
            refundRepo.save(refund);
        }

        creditDeductionService.updateCacheAfterDeduct(user, classEntity);

        while (classEntity.getAvailableSlots() > 0) {
            log.info("Class {} available slots: {}", classEntity.getClassId(), classEntity.getAvailableSlots());
            WaitlistEntry waitlistEntry = getFromWaitlistUserToBooked(classEntity);
            if (waitlistEntry == null) break;
            User candidate = userCacheService.getUser(waitlistEntry.getUserId());
            processBooking(candidate, classEntity, false);
        }

        return bookingRepo.save(booking);
    }

    private WaitlistEntry getFromWaitlistUserToBooked(Class classEntity) {
        return waitlistCacheService.getFromWaitlist(classEntity.getClassId());
    }

}

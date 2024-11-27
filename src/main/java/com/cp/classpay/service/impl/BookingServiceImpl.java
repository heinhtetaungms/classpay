package com.cp.classpay.service.impl;

import com.cp.classpay.api.input.booking.BookingClassRequest;
import com.cp.classpay.api.input.booking.CheckInBookedClassRequest;
import com.cp.classpay.api.input.class_.CancelBookingRequest;
import com.cp.classpay.api.input.waitlist.WaitlistEntry;
import com.cp.classpay.api.output.booking.BookingConfirmedClassesResponse;
import com.cp.classpay.api.output.booking.BookingResponse;
import com.cp.classpay.api.output.booking.CancelBookingResponse;
import com.cp.classpay.commons.enum_.BookingStatus;
import com.cp.classpay.commons.enum_.PackageStatus;
import com.cp.classpay.entity.*;
import com.cp.classpay.entity.Class;
import com.cp.classpay.exceptions.ApiBusinessException;
import com.cp.classpay.exceptions.ApiValidationException;
import com.cp.classpay.exceptions.BookingConcurrencyException;
import com.cp.classpay.exceptions.InsufficientCreditsException;
import com.cp.classpay.service.BookingService;
import com.cp.classpay.service.cache.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private BookingCacheService bookingCacheService;
    @Autowired
    private BookingDetailCacheService bookingDetailCacheService;
    @Autowired
    private BookingLockService bookingLockService;
    @Autowired
    private UserPackageCacheService userPackageCacheService;
    @Autowired
    private ClassCacheService classCacheService;
    @Autowired
    private WaitlistCacheService waitlistCacheService;
    @Autowired
    private RefundCacheService refundCacheService;


    @Override
    public List<BookingConfirmedClassesResponse> bookingConfirmedClasses() {
        User user = userCacheService.getUser();
        List<Booking> bookedBookingList = bookingCacheService.findAllBookedBookingByUserId(user.getUserId());
        return bookedBookingList.stream()
                .map(BookingConfirmedClassesResponse::from)
                .toList();
    }

    @Override
    public BookingResponse bookingClass(BookingClassRequest bookingClassRequest) {

        User user = userCacheService.getUser();

        Class class_e = classCacheService.findById(bookingClassRequest.classId());

        validateOverlapClassTime(class_e);

        Booking booking = processBooking(user, class_e);
        return BookingResponse.from(booking);
    }

    @Override
    public BookingResponse checkInBookedClass(CheckInBookedClassRequest checkInBookedClassRequest) {
        Booking bookedBooking =bookingCacheService.findById(checkInBookedClassRequest.bookingId());
        bookedBooking.setStatus(BookingStatus.CHECK_IN);
        bookingCacheService.save(bookedBooking);
        return BookingResponse.from(bookedBooking);
    }

    private void validateOverlapClassTime(Class class_e) {
        ZonedDateTime requestClassStartDate = class_e.getClassStartDate();
        List<Booking> bookedBookingList = bookingCacheService.findAllBookedBookingByUserId(userCacheService.getUser().getUserId());
        boolean isOverlap = bookedBookingList.stream()
                    .anyMatch(booking -> isDateWithinRange(requestClassStartDate, booking.getClassEntity().getClassStartDate(), booking.getClassEntity().getClassEndDate()));
        if (isOverlap) {
            throw new ApiBusinessException("User can’t book the overlap class time.");
        }
    }

    private boolean isDateWithinRange(ZonedDateTime requestClassStartDate, ZonedDateTime classStartDate, ZonedDateTime classEndDate) {
        // Check if the requestClassStartDate is within the range
        return (requestClassStartDate.isAfter(classStartDate) || requestClassStartDate.isEqual(classStartDate))
                && (requestClassStartDate.isBefore(classEndDate) || requestClassStartDate.isEqual(classEndDate));
    }

    private Booking processBooking(User user, Class class_e) {
        try (BookingLockService.AutoLock lock = bookingLockService.lockForBooking(user.getUserId(), class_e.getClassId())) {

            List<UserPackage> userPackageList = userPackageCacheService.findUserPackagesByUserIdAndCountry(user.getUserId(), user.getCountry());
            validateSufficientCredits(class_e, userPackageList);

            return deductCredits(user, class_e, userPackageList);
        } catch (Exception e) {
            throw new BookingConcurrencyException("Failed to complete booking due to high demand. Please try again.");
        }
    }

    @Override
    public CancelBookingResponse cancelBooking(CancelBookingRequest cancelBookingRequest) {

        User user = userCacheService.getUser();

        Booking booking = bookingCacheService.findById(cancelBookingRequest.bookingId());
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found by id " + cancelBookingRequest.bookingId());
        }

        if (!booking.getUser().equals(user)) {
            throw new IllegalArgumentException("User can't do someone else booking cancellation process.");
        }

        if (booking.isCanceled()) {
            throw new IllegalArgumentException("Booking is already canceled.");
        }

        booking.setStatus(BookingStatus.CANCELED);
        booking.setCanceled(true);
        booking.setCancellationTime(ZonedDateTime.now());

        // Update class availability
        Class class_e = booking.getClassEntity();
        class_e.setAvailableSlots(class_e.getAvailableSlots() + 1);
        classCacheService.save(class_e);
        bookingCacheService.save(booking);

        // Refund credits
        List<BookingDetail> bookingDetailList = bookingDetailCacheService.findAllByBookingId(booking.getBookingId());
        for (BookingDetail bookingDetail : bookingDetailList) {
            UserPackage userPackage = bookingDetail.getUserPackage();
            userPackage.setRemainingCredits(userPackage.getRemainingCredits() + bookingDetail.getCreditsDeducted());
            userPackageCacheService.save(userPackage);
            log.debug("Refunded {} credits to user package {} for booking detail {}", bookingDetail.getCreditsDeducted(), userPackage.getUserPackageId(), bookingDetail.getBookingDetailId());

            Refund refund = new Refund();
            refund.setUser(user);
            refund.setUserPackage(userPackage);
            refund.setCreditRefunded(bookingDetail.getCreditsDeducted());
            refund.setRefundTime(ZonedDateTime.now());
            refundCacheService.save(refund);
        }

        addWaitlistUserAsBookedAs(class_e);

        return CancelBookingResponse.from(booking);
    }

    private void addWaitlistUserAsBookedAs(Class class_e) {
        while (class_e.getAvailableSlots() > 0) {
            log.info("Class {} available slots: {}", class_e.getClassId(), class_e.getAvailableSlots());
            WaitlistEntry waitlistEntry = waitlistCacheService.getFromWaitlist(class_e.getClassId());
            if (waitlistEntry == null) break;
            User candidate = userCacheService.getUser(waitlistEntry.getEmail());
            processBooking(candidate, class_e);
        }
    }

    private Booking deductCredits(User user, Class class_e, List<UserPackage> userPackageList) {
        boolean isWaitlist = false;
        int availableSlots = class_e.getAvailableSlots();
        if (availableSlots == 0) {
            isWaitlist = true;
            waitlistCacheService.addToWaitlist(user, class_e);
        } else {
            class_e.setAvailableSlots(availableSlots - 1);
        }

        int totalCreditsToDeduct = class_e.getRequiredCredits();

        List<UserPackage> eligiblePackages = userPackageList.stream()
                .filter(pkg -> pkg.getPackageEntity().getCountry().equals(class_e.getCountry()))
                .filter(pkg -> pkg.getStatus() == PackageStatus.ACTIVE && pkg.getRemainingCredits() > 0)
                .sorted(Comparator.comparing(UserPackage::getExpirationDate))
                .toList();

        Booking booking = Booking.builder()
                .user(user)
                .classEntity(class_e)
                .bookingTime(ZonedDateTime.now())
                .status(isWaitlist ? BookingStatus.WAITLISTED : BookingStatus.BOOKED)
                .isCanceled(false)
                .build();

        classCacheService.save(class_e);
        bookingCacheService.save(booking);

        for (UserPackage userPackage : eligiblePackages) {
            if (totalCreditsToDeduct <= 0) break;

            int creditsBalanceFromPackage = userPackage.getRemainingCredits();
            int creditsToDeductFromThisPackage = Math.min(totalCreditsToDeduct, creditsBalanceFromPackage);

            // Deduct credits from this package
            userPackage.setRemainingCredits(creditsBalanceFromPackage - creditsToDeductFromThisPackage);
            totalCreditsToDeduct -= creditsToDeductFromThisPackage;

            // Record deduction in BookingDetail
            BookingDetail detail = BookingDetail.builder()
                    .booking(booking)
                    .userPackage(userPackage)
                    .creditsDeducted(creditsToDeductFromThisPackage)
                    .build();
            userPackageCacheService.save(userPackage);
            bookingDetailCacheService.save(detail);
        }
        return booking;
    }

    private void validateSufficientCredits(Class classEntity, List<UserPackage> userPackages) {
        // Validate sufficient credits
        int totalEligibleBalanceCredits = userPackages.stream()
                .filter(pkg -> pkg.getPackageEntity().getCountry().equals(classEntity.getCountry()))
                .filter(pkg -> pkg.getStatus() == PackageStatus.ACTIVE && pkg.getRemainingCredits() > 0)
                .mapToInt(UserPackage::getRemainingCredits).sum();

        if (totalEligibleBalanceCredits < classEntity.getRequiredCredits()) {
            throw new InsufficientCreditsException("Not enough credits for booking");
        }
    }
}

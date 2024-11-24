package com.cp.classpay.service.cache;

import com.cp.classpay.commons.enum_.BookingStatus;
import com.cp.classpay.commons.enum_.PackageStatus;
import com.cp.classpay.commons.enum_.WaitlistStatus;
import com.cp.classpay.entity.*;
import com.cp.classpay.entity.Class;
import com.cp.classpay.exceptions.InsufficientCreditsException;
import com.cp.classpay.repository.BookingDetailRepo;
import com.cp.classpay.repository.BookingRepo;
import com.cp.classpay.repository.ClassRepo;
import com.cp.classpay.repository.UserPackageRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class CreditDeductionService {

    @Autowired
    private BookingDetailRepo bookingDetailRepo;
    @Autowired
    private ClassCacheService classCacheService;
    @Autowired
    private UserPackageCacheService userPackageCacheService;
    @Autowired
    private WaitlistCacheService waitlistCacheService;
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private ClassRepo classRepo;
    @Autowired
    private UserPackageRepo userPackageRepo;

    @Transactional
    public Booking deductCredits(User user, Class classEntity, List<UserPackage> userPackages, boolean normalBooking) {
        int totalCreditsToDeduct = classEntity.getRequiredCredits();

        List<UserPackage> eligiblePackages = userPackages.stream()
                .filter(pkg -> pkg.getPackageEntity().getCountry().equals(classEntity.getCountry()))
                .filter(pkg -> pkg.getStatus() == PackageStatus.ACTIVE && pkg.getRemainingCredits() > 0)
                .sorted(Comparator.comparing(UserPackage::getExpirationDate))
                .toList();

        if (normalBooking) {
            classEntity.setAvailableSlots(classEntity.getAvailableSlots() - 1);
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setClassEntity(classEntity);
        booking.setBookingTime(ZonedDateTime.now());
        booking.setStatus(normalBooking ? BookingStatus.BOOKED : BookingStatus.WAITLISTED);
        booking.setCanceled(false);
        classRepo.save(classEntity);
        bookingRepo.save(booking);

        for (UserPackage userPackage : eligiblePackages) {
            if (totalCreditsToDeduct <= 0) break;

            int creditsBalanceFromPackage = userPackage.getRemainingCredits();
            int creditsToDeductFromThisPackage = Math.min(totalCreditsToDeduct, creditsBalanceFromPackage);

            // Deduct credits from this package
            userPackage.setRemainingCredits(creditsBalanceFromPackage - creditsToDeductFromThisPackage);
            totalCreditsToDeduct -= creditsToDeductFromThisPackage;

            // Record deduction in BookingDetail
            BookingDetail detail = new BookingDetail();
            detail.setBooking(booking);
            detail.setUserPackage(userPackage);
            detail.setCreditsDeducted(creditsToDeductFromThisPackage);
            userPackageRepo.save(userPackage);
            bookingDetailRepo.save(detail);
        }

        if (!normalBooking) {
            log.info("Adding user {} to waitlist for class {}", user.getUserId(), classEntity.getClassId());
            waitlistCacheService.addToWaitlist(user, classEntity);
        }

        // If there are still credits required, booking cannot proceed
        if (totalCreditsToDeduct > 0) {
            throw new InsufficientCreditsException("Not enough credits to complete the booking.");
        }
        return booking;
    }

    public void updateCacheAfterDeduct(User user, Class classEntity) {
        classCacheService.updateCacheClassEntity(classEntity);
        classCacheService.updateCacheForAvailableClassesByCountry(classEntity.getCountry());
        userPackageCacheService.updateCacheForUserPackagesByCountry(user.getUserId(), user.getCountry());
    }
}

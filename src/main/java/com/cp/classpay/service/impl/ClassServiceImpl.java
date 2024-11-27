package com.cp.classpay.service.impl;

import com.cp.classpay.api.input.class_.ClassRegisterRequest;
import com.cp.classpay.api.output.class_.ClassRegisterResponse;
import com.cp.classpay.api.output.class_.ClassResponse;
import com.cp.classpay.commons.enum_.BookingStatus;
import com.cp.classpay.entity.*;
import com.cp.classpay.entity.Class;
import com.cp.classpay.repository.*;
import com.cp.classpay.service.ClassService;
import com.cp.classpay.service.MockEmailService;
import com.cp.classpay.service.cache.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ClassServiceImpl implements ClassService {

    private final ClassCacheService classCacheService;
    private final BusinessRepo businessRepo;
    private final BookingCacheService bookingCacheService;
    private final BookingDetailCacheService bookingDetailCacheService;
    private final RefundCacheService refundCacheService;
    private final UserPackageCacheService userPackageCacheService;
    private final BookingRepo bookingRepo;
    private final MockEmailService mockEmailService;

    public ClassServiceImpl(ClassCacheService classCacheService, BusinessRepo businessRepo, BookingCacheService bookingCacheService, BookingDetailCacheService bookingDetailCacheService, RefundCacheService refundCacheService, UserPackageCacheService userPackageCacheService, BookingRepo bookingRepo, MockEmailService mockEmailService) {
        this.classCacheService = classCacheService;
        this.businessRepo = businessRepo;
        this.bookingCacheService = bookingCacheService;
        this.bookingDetailCacheService = bookingDetailCacheService;
        this.refundCacheService = refundCacheService;
        this.userPackageCacheService = userPackageCacheService;
        this.bookingRepo = bookingRepo;
        this.mockEmailService = mockEmailService;
    }


    @Override
    public ClassRegisterResponse registerClass(ClassRegisterRequest classRegisterRequest) {
        Business business =businessRepo.findById(classRegisterRequest.businessId()).orElseThrow(() ->new IllegalArgumentException("Business not found by id " + classRegisterRequest.businessId()));
        Class clazz =  Class.builder()
                            .className(classRegisterRequest.className())
                            .country(classRegisterRequest.country())
                            .requiredCredits(classRegisterRequest.requiredCredits())
                            .availableSlots(classRegisterRequest.availableSlots())
                            .classStartDate(classRegisterRequest.classStartDate())
                            .classEndDate(classRegisterRequest.classEndDate())
                            .business(business)
                            .build();
        Class savedClass = classCacheService.save(clazz);

        return ClassRegisterResponse.from(savedClass);
    }

    @Override
    public List<ClassResponse> getAvailableClassesByCountry(String classCountry) {
        List<Class> classList = classCacheService.findAllByCountry(classCountry);
        return classList.stream()
                .map(data -> ClassResponse.from(data))
                .collect(Collectors.toList());
    }

    @Override
    public void refundWaitlistUserCreditsWhenClassEnd() {
        Set<Long> classesEndingAroundNow = classCacheService.classesEndingAroundNow();
        classesEndingAroundNow.stream().forEach(classId -> processRefund(classId));
    }

    @Override
    public void remindClassStartTimeToUser() {
        Set<Long> classesStaringAroundNow = classCacheService.classesStartingAroundNow();
        classesStaringAroundNow.stream().forEach(classId -> sendRemindEmail(classId));

    }

    private void sendRemindEmail(long classId) {
        List<Booking> bookedBookingList = bookingRepo.findAllByClassEntity_ClassIdAndStatus(classId, BookingStatus.BOOKED);
        bookedBookingList.stream().forEach(booking -> mockEmailService.sendRemindToCheckInWhenClassTimeStart(booking.getUser().getEmail(), booking.getClassEntity().getClassName()));
    }

    private void processRefund(long classId) {
        List<Booking> waitlistedBookingList = bookingRepo.findAllByClassEntity_ClassIdAndStatus(classId, BookingStatus.WAITLISTED);
        waitlistedBookingList.stream().forEach(booking -> refundCredit(booking));
    }

    private void refundCredit(Booking booking) {
        booking.setStatus(BookingStatus.REFUNDED);
        bookingCacheService.save(booking);

        List<BookingDetail> bookingDetails = bookingDetailCacheService.findAllByBookingId(booking.getBookingId());
        bookingDetails.stream().forEach(bookingDetail -> {
                                        UserPackage userPackage = bookingDetail.getUserPackage();
                                        userPackage.setRemainingCredits(userPackage.getRemainingCredits() + bookingDetail.getCreditsDeducted());
                                        userPackageCacheService.save(userPackage);
                                        log.debug("Refunded {} credits to user package {} for booking detail {}", bookingDetail.getCreditsDeducted(), userPackage.getUserPackageId(), bookingDetail.getBookingDetailId());

                                        Refund refund = new Refund();
                                        refund.setUser(booking.getUser());
                                        refund.setUserPackage(userPackage);
                                        refund.setCreditRefunded(bookingDetail.getCreditsDeducted());
                                        refund.setRefundTime(ZonedDateTime.now());
                                        refundCacheService.save(refund);
                                    });
    }
}

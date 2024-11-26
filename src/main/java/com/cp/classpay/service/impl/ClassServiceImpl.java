package com.cp.classpay.service.impl;

import com.cp.classpay.api.input.class_.ClassRegisterRequest;
import com.cp.classpay.api.output.class_.ClassRegisterResponse;
import com.cp.classpay.api.output.class_.ClassResponse;
import com.cp.classpay.commons.enum_.BookingStatus;
import com.cp.classpay.entity.*;
import com.cp.classpay.entity.Class;
import com.cp.classpay.repository.*;
import com.cp.classpay.service.ClassService;
import com.cp.classpay.service.cache.ClassCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final BookingRepo bookingRepo;
    private final UserPackageRepo userPackageRepo;
    private final RefundRepo refundRepo;

    public ClassServiceImpl(ClassCacheService classCacheService, BusinessRepo businessRepo, BookingRepo bookingRepo, UserPackageRepo userPackageRepo, RefundRepo refundRepo) {
        this.classCacheService = classCacheService;
        this.businessRepo = businessRepo;
        this.bookingRepo = bookingRepo;
        this.userPackageRepo = userPackageRepo;
        this.refundRepo = refundRepo;
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
    public void when_class_end_waitlist_user_credit_need_to_be_refunded() {
        Set<Long> classesEndingAroundNow = classCacheService.classesEndingAroundNow();
        classesEndingAroundNow.stream().forEach(classId -> {
            processRefund(classId);
        });
    }

    void processRefund(long classId) {
        List<Booking> bookingList = bookingRepo.findAllByClassEntity_ClassIdAndStatus(classId, BookingStatus.WAITLISTED);
        bookingList.stream().forEach(booking -> {
            refundCredit(booking);
        });
    }

    void refundCredit(Booking booking) {
        booking.setStatus(BookingStatus.REFUNDED);
        bookingRepo.save(booking);

        List<BookingDetail> bookingDetails = booking.getBookingDetails();
        for (BookingDetail bookingDetail : bookingDetails) {
            UserPackage userPackage = bookingDetail.getUserPackage();
            userPackage.setRemainingCredits(userPackage.getRemainingCredits() + bookingDetail.getCreditsDeducted());
            userPackageRepo.save(userPackage);
            log.debug("Refunded {} credits to user package {} for booking detail {}", bookingDetail.getCreditsDeducted(), userPackage.getUserPackageId(), bookingDetail.getBookingDetailId());

            Refund refund = new Refund();
            refund.setUser(booking.getUser());
            refund.setUserPackage(userPackage);
            refund.setCreditRefunded(bookingDetail.getCreditsDeducted());
            refund.setRefundTime(ZonedDateTime.now());
            refundRepo.save(refund);
        }
    }
}

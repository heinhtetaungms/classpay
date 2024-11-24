package com.cp.classpay.service.impl;

import com.cp.classpay.api.input.class_.ClassRegistrationRequest;
import com.cp.classpay.api.output.class_.ClassRegistrationResponse;
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

    @Autowired
    private ClassCacheService classCacheService;
    @Autowired
    private BusinessRepo businessRepo;
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private ClassRepo classRepo;
    @Autowired
    private UserPackageRepo userPackageRepo;
    @Autowired
    private RefundRepo refundRepo;

    @Override
    public ClassRegistrationResponse registerClass(ClassRegistrationRequest classRegistrationRequest) {
        Business business =businessRepo.findById(classRegistrationRequest.businessId()).orElseThrow(() ->new IllegalArgumentException("Business not found by id " + classRegistrationRequest.businessId()));
        Class clazz =  Class.builder()
                            .className(classRegistrationRequest.className())
                            .country(classRegistrationRequest.country())
                            .requiredCredits(classRegistrationRequest.requiredCredits())
                            .availableSlots(classRegistrationRequest.availableSlots())
                            .classStartDate(classRegistrationRequest.classStartDate())
                            .classEndDate(classRegistrationRequest.classEndDate())
                            .business(business)
                            .build();
        Class savedClass = classCacheService.saveClass(clazz);

        return ClassRegistrationResponse.toClassRegistrationResponse(savedClass);
    }

    @Override
    public List<ClassResponse> getAvailableClassesByCountry(String classCountry) {
        List<Class> classList = classCacheService.getAvailableClassesByCountry(classCountry);
        return classList.stream()
                .map(data -> ClassResponse.toClassResponse(data))
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

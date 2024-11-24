package com.cp.classpay.service.impl;

import com.cp.classpay.commons.enum_.PaymentStatus;
import com.cp.classpay.entity.Package;
import com.cp.classpay.entity.Payment;
import com.cp.classpay.entity.User;
import com.cp.classpay.repository.PaymentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
public class MockPaymentService {

    @Autowired
    private PaymentRepo paymentRepo;

    public void paymentCharge(Package pack, User user) {
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setAmount(pack.getPrice());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentTime(ZonedDateTime.now());
        paymentRepo.save(payment);
    }

}

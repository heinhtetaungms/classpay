package com.cp.classpay.service;

public interface MockEmailService {
    boolean sendMsisdnVerificationEmail(String email, String OTP);
    boolean sendResetPasswordEmail(String email, String OTP);
    boolean sendResetPinAsTempPassword(String email, String resetPin);
}

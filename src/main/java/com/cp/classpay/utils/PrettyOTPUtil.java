package com.cp.classpay.utils;

import com.cp.classpay.exceptions.Api_Rate_Limited_Exception;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Component
public class PrettyOTPUtil {
    @Autowired
    private RedisUtil redisUtil;

    private static final SecureRandom secureRandom = new SecureRandom();  // Cryptographically strong RNG
    private static final String DIGITS = "0123456789";

    // Generates a "pretty" OTP (6 digits by default)
    public String generatePrettyOTP() {
        StringBuilder otp = new StringBuilder(6);

        // Generate the first two digits
        int firstDigit = secureRandom.nextInt(DIGITS.length());
        int secondDigit = secureRandom.nextInt(DIGITS.length());

        // Ensure some pattern by repeating some digits
        otp.append(DIGITS.charAt(firstDigit))
                .append(DIGITS.charAt(firstDigit)) // Repeat the first digit
                .append(DIGITS.charAt(secondDigit))
                .append(DIGITS.charAt(secondDigit)) // Repeat the second digit
                .append(DIGITS.charAt(secureRandom.nextInt(DIGITS.length())))
                .append(DIGITS.charAt(secureRandom.nextInt(DIGITS.length())));

        return otp.toString();
    }

    public void storeOTP(String email, String otp) {
        redisUtil.setWithExpiration("otp:" + email, otp, 5, TimeUnit.MINUTES);
    }

    public String getOTP(String email) {
        return (String) redisUtil.get("otp:" + email);
    }

    public void rateLimitingOTP(String email) {
        Integer resendAttemptCount = (Integer) redisUtil.get("otp_resend_count:" + email);
        if (resendAttemptCount != null && resendAttemptCount >= 3) {
            throw new Api_Rate_Limited_Exception("You have exceeded the number of allowed OTP resend attempts. Please try again later.");
        }

        redisUtil.increment("otp_resend_count:" + email);
        redisUtil.expire("otp_resend_count:" + email, 30, TimeUnit.MINUTES);
    }

    public void deleteOTP(String email) {
        redisUtil.delete("otp:" + email);
    }
}


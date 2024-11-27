package com.cp.classpay.service.impl;

import com.cp.classpay.exceptions.ApiOTPExpirationException;
import com.cp.classpay.exceptions.ApiRateLimitedException;
import com.cp.classpay.service.OTPService;
import com.cp.classpay.utils.PrettyOTPUtil;
import com.cp.classpay.utils.RedisUtil;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class OTPServiceImpl implements OTPService {

    private final PrettyOTPUtil prettyOTPUtil;
    private final RedisUtil redisUtil;

    public OTPServiceImpl(PrettyOTPUtil prettyOTPUtil, RedisUtil redisUtil) {
        this.prettyOTPUtil = prettyOTPUtil;
        this.redisUtil = redisUtil;
    }

    private static final String OTP_PREFIX = "OTP:";
    private static final String OTP_RESENT_COUNT_PREFIX = "OTP_RESEND_COUNT:";

    @Override
    public String generateOTP() {
        return prettyOTPUtil.generatePrettyOTP();
    }

    @Override
    public void verifyOTP(String email, String otpToVerify) {
        String aliveOTP = getOTP(email);
        if (aliveOTP == null || !aliveOTP.equals(otpToVerify)) {
            throw new ApiOTPExpirationException("OTP expired.");
        }
        deleteOTP(email);
    }

    @Override
    public void storeOTP(String email, String otp) {
        redisUtil.setWithExpiration(OTP_PREFIX + email, otp, 5, TimeUnit.MINUTES);
    }

    @Override
    public String getOTP(String email) {
        return (String) redisUtil.get(OTP_PREFIX + email);
    }

    @Override
    public void deleteOTP(String email) {
        redisUtil.delete(OTP_PREFIX + email);
    }

    @Override
    public void rateLimitingOTP(String email) {
        Integer resendAttemptCount = (Integer) redisUtil.get(OTP_RESENT_COUNT_PREFIX + email);
        if (resendAttemptCount != null && resendAttemptCount >= 3) {
            throw new ApiRateLimitedException("You have exceeded the number of allowed OTP resend attempts. Please try again later.");
        }

        redisUtil.increment(OTP_RESENT_COUNT_PREFIX + email);
        redisUtil.expire(OTP_RESENT_COUNT_PREFIX + email, 30, TimeUnit.MINUTES);
    }
}

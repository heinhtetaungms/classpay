package com.cp.classpay.service;

import com.cp.classpay.api.input.user.ChangePasswordRequest;
import com.cp.classpay.api.input.user.ResetPasswordRequest;
import com.cp.classpay.api.input.user.UserRegistrationRequest;
import com.cp.classpay.api.output.user.UserProfileResponse;
import com.cp.classpay.api.output.user.UserRegistrationResponse;
import com.cp.classpay.entity.User;

public interface UserService {
    UserRegistrationResponse register(UserRegistrationRequest userRegistrationRequest);
    User verifyOTP(String email, String OTP);
    void resendOTP(String email);
    UserProfileResponse getProfile(String jwtToken);
    UserProfileResponse changePassword(String jwtToken, ChangePasswordRequest changePasswordRequest);
    void resetPasswordRequest(String email);
    void verifyResetPasswordOTP(String email, String OTP);
    UserProfileResponse resetPassword(ResetPasswordRequest resetPasswordRequest);
}
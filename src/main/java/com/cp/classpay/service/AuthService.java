package com.cp.classpay.service;

import com.cp.classpay.api.input.auth.*;
import com.cp.classpay.api.output.auth.UserProfileResponse;
import com.cp.classpay.api.output.auth.VerifyOTPResponse;
import com.cp.classpay.api.output.auth.UserRegistrationResponse;

public interface AuthService {
    void generateOTP(GenerateOTPRequest generateOTPRequest);
    VerifyOTPResponse verifyOTP(VerifyOTPRequest verifyOTPRequest);
    void resendOTP(ResendOTPRequest resendOTPRequest);
    UserRegistrationResponse register(UserRegistrationRequest userRegistrationRequest);
    UserProfileResponse getProfile(String jwtToken);
    UserProfileResponse changePassword(String jwtToken, ChangePasswordRequest changePasswordRequest);
    void resetPassword(ResetPasswordRequest resetPasswordRequest);
    void resetPasswordConfirm(ResetPasswordConfirmRequest resetPasswordConfirmRequest);

}

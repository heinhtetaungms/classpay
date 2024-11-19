package com.cp.classpay.api;

import com.cp.classpay.api.input.auth.*;
import com.cp.classpay.api.output.auth.VerifyOTPResponse;
import com.cp.classpay.api.output.auth.TokenResponse;
import com.cp.classpay.api.output.auth.UserProfileResponse;
import com.cp.classpay.api.output.auth.UserRegistrationResponse;
import com.cp.classpay.security.token.TokenManagementService;
import com.cp.classpay.service.AuthService;
import com.cp.classpay.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthApi {
    @Autowired
    private AuthService authService;
    @Autowired
    private TokenManagementService tokenService;
    @PostMapping("/generate-otp")
    public ResponseEntity<ApiResponse<String>> generateOTP(@Validated @RequestBody GenerateOTPRequest request, BindingResult result) {
        authService.generateOTP(request);
        String successMessage = String.format("OTP has been sent to your email %s.", request.email());
        return ApiResponse.of(successMessage, HttpStatus.OK);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<VerifyOTPResponse>> verifyOTP(@Validated @RequestBody VerifyOTPRequest request, BindingResult result) {
        VerifyOTPResponse verifyOTPResponse = authService.verifyOTP(request);
        return ApiResponse.of(verifyOTPResponse, HttpStatus.OK);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<String>> resendOTP(@Validated @RequestBody ResendOTPRequest request, BindingResult result) {
        authService.resendOTP(request);
        String successMessage = String.format("OTP has been resent to your email %s.", request.email());
        return ApiResponse.of(successMessage, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserRegistrationResponse>> register(@Validated @RequestBody UserRegistrationRequest userRegistrationRequest, BindingResult result) {
        UserRegistrationResponse userRegistrationResponse = authService.register(userRegistrationRequest);
        return ApiResponse.of(userRegistrationResponse, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> generate(@Validated @RequestBody TokenRequestForm form, BindingResult result) {
        TokenResponse tokenResponse = tokenService.generate(form);
        return ApiResponse.of(tokenResponse, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Validated @RequestBody TokenRefreshForm form, BindingResult result) {
        TokenResponse tokenResponse = tokenService.refresh(form);
        return ApiResponse.of(tokenResponse, HttpStatus.OK);
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@RequestHeader("Authorization") String jwtToken) {
        UserProfileResponse userProfileResponse = authService.getProfile(jwtToken);
        return ApiResponse.of(userProfileResponse, HttpStatus.OK);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<UserProfileResponse>> changePassword(@RequestHeader("Authorization") String jwtToken, @Validated @RequestBody ChangePasswordRequest changePasswordRequest, BindingResult result) {
        UserProfileResponse userProfileResponse = authService.changePassword(jwtToken, changePasswordRequest);
        return ApiResponse.of(userProfileResponse, HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Validated @RequestBody ResetPasswordRequest resetPasswordRequest, BindingResult result) {
        authService.resetPassword(resetPasswordRequest);
        String successMessage = String.format("Temporary OTP has been sent to %s for reset password.", resetPasswordRequest.email());
        return ApiResponse.of(successMessage, HttpStatus.OK);
    }

    @PostMapping("/reset-password-confirm")
    public ResponseEntity<ApiResponse<String>> resetPasswordConfirm(@Validated @RequestBody ResetPasswordConfirmRequest resetPasswordConfirmRequest, BindingResult result) {
        authService.resetPasswordConfirm(resetPasswordConfirmRequest);
        String successMessage = String.format("Reset Pin email sent to %s, use this rest pin to login and change password for your security.", resetPasswordConfirmRequest.email());
        return ApiResponse.of(successMessage, HttpStatus.OK);
    }

}
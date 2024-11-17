package com.cp.classpay.api;

import com.cp.classpay.api.input.user.*;
import com.cp.classpay.api.output.user.TokenResponse;
import com.cp.classpay.api.output.user.UserProfileResponse;
import com.cp.classpay.api.output.user.UserRegistrationResponse;
import com.cp.classpay.entity.User;
import com.cp.classpay.security.token.TokenManagementService;
import com.cp.classpay.service.UserService;
import com.cp.classpay.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserApi {

    private final UserService userService;
    private final TokenManagementService tokenService;
    public UserApi(UserService userService, TokenManagementService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> generate(@Validated @RequestBody TokenRequestForm form, BindingResult result) {
        return ApiResponse.of(tokenService.generate(form), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Validated @RequestBody TokenRefreshForm form, BindingResult result) {
        return ApiResponse.of(tokenService.refresh(form), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserRegistrationResponse>> register(@Validated @RequestBody UserRegistrationRequest userRegistrationRequest, BindingResult result) {
        UserRegistrationResponse userRegistrationResponse = userService.register(userRegistrationRequest);
        return ApiResponse.of(userRegistrationResponse, HttpStatus.OK);
    }

    @GetMapping("/verifyOTP")
    public ResponseEntity<ApiResponse<String>> verifyOTP(@RequestParam String email, @RequestParam String OTP) {
        User user = userService.verifyOTP(email, OTP);
        String successMessage = String.format("User with email %s has been successfully verified OTP.", user.getEmail());
        return ApiResponse.of(successMessage, HttpStatus.OK);
    }

    @PostMapping("/resendOTP")
    public ResponseEntity<ApiResponse<String>> resendOTP(@RequestParam String email) {
        userService.resendOTP(email);
        String successMessage = String.format("OTP has been resent to your email %s.", email);
        return ApiResponse.of(successMessage, HttpStatus.OK);
    }


    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@RequestHeader("Authorization") String jwtToken) {
        UserProfileResponse userProfileResponse = userService.getProfile(jwtToken);
        return ApiResponse.of(userProfileResponse, HttpStatus.OK);
    }

    @PostMapping("/changePassword")
    public ResponseEntity<ApiResponse<UserProfileResponse>> changePassword(@RequestHeader("Authorization") String jwtToken, @Validated @RequestBody ChangePasswordRequest changePasswordRequest, BindingResult result) {
        UserProfileResponse userProfileResponse = userService.changePassword(jwtToken, changePasswordRequest);
        return ApiResponse.of(userProfileResponse, HttpStatus.OK);
    }

    @PostMapping("/resetPasswordRequest")
    public ResponseEntity<ApiResponse<String>> resetPasswordRequest(@RequestParam String email) {
        userService.resetPasswordRequest(email);
        String successMessage = String.format("Reset Password OTP has been resent to your email %s.", email);
        return ApiResponse.of(successMessage, HttpStatus.OK);
    }

    @GetMapping("/verifyResetPasswordOTP")
    public ResponseEntity<ApiResponse<String>> verifyResetPasswordOTP(@RequestParam String email, @RequestParam String OTP) {
        userService.verifyResetPasswordOTP(email, OTP);
        String successMessage = String.format("User with email %s has been successfully verified for Rest Password.", email);
        return ApiResponse.of(successMessage, HttpStatus.OK);
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<ApiResponse<UserProfileResponse>> resetPassword(@Validated @RequestBody ResetPasswordRequest resetPasswordRequest, BindingResult result) {
        UserProfileResponse userProfileResponse = userService.resetPassword(resetPasswordRequest);
        return ApiResponse.of(userProfileResponse, HttpStatus.OK);
    }


}

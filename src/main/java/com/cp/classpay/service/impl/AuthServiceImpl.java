package com.cp.classpay.service.impl;

import com.cp.classpay.commons.enum_.RoleEnum;
import com.cp.classpay.api.input.auth.*;
import com.cp.classpay.api.output.auth.UserProfileResponse;
import com.cp.classpay.api.output.auth.VerifyOTPResponse;
import com.cp.classpay.api.output.auth.UserRegistrationResponse;
import com.cp.classpay.entity.Role;
import com.cp.classpay.entity.User;
import com.cp.classpay.exceptions.ApiBusinessException;
import com.cp.classpay.exceptions.ApiValidationException;
import com.cp.classpay.exceptions.ApiOTPExpirationException;
import com.cp.classpay.repository.RoleRepo;
import com.cp.classpay.security.token.JwtTokenParser;
import com.cp.classpay.commons.enum_.TokenType;
import com.cp.classpay.service.AuthService;
import com.cp.classpay.service.MockEmailService;
import com.cp.classpay.service.OTPService;
import com.cp.classpay.service.cache.UserCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final JwtTokenParser jwtTokenParser;
    private final PasswordEncoder passwordEncoder;
    private final OTPService otpService;
    private final MockEmailService mockEmailService;
    private final UserCacheService userCacheService;
    private final RoleRepo roleRepo;

    public AuthServiceImpl(JwtTokenParser jwtTokenParser, PasswordEncoder passwordEncoder, OTPService otpService, MockEmailService mockEmailService, UserCacheService userCacheService, RoleRepo roleRepo) {
        this.jwtTokenParser = jwtTokenParser;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.mockEmailService = mockEmailService;
        this.userCacheService = userCacheService;
        this.roleRepo = roleRepo;
    }

    private static final String exception_msg = "Failed to send verification email to user with ";

    @Override
    public void generateOTP(GenerateOTPRequest generateOTPRequest) {
        String newOTP = otpService.generateOTP();
        boolean emailSent = mockEmailService.sendMsisdnVerificationEmail(generateOTPRequest.email(), newOTP);

        if (!emailSent)
            throw new ApiBusinessException(exception_msg + generateOTPRequest.email());
        otpService.storeOTP(generateOTPRequest.email(), newOTP);
    }

    @Override
    public VerifyOTPResponse verifyOTP(VerifyOTPRequest verifyOTPRequest) {
        otpService.verifyOTP(verifyOTPRequest.email(), verifyOTPRequest.OTP());
        Optional<User> userExisted = userCacheService.findByEmailOrElse(verifyOTPRequest.email());
        if (userExisted.isPresent()) {
            return VerifyOTPResponse.from(userExisted.get());
        }

        User user = User.builder()
                .email(verifyOTPRequest.email())
                .username("")
                .password("")
                .country("")
                .build();
        userCacheService.save(user);
        return VerifyOTPResponse.from(user);
    }
    //if isVerified -> false -> register screen
    //if isVerified -> true -> login screen
    @Override
    public void resendOTP(ResendOTPRequest resendOTPRequest) {
        String aliveOTP = otpService.getOTP(resendOTPRequest.email());
        if (aliveOTP == null) {
            otpService.rateLimitingOTP(resendOTPRequest.email());

            // No OTP exists or expired, generate a new OTP
            String newOTP = otpService.generateOTP();
            boolean emailSent = mockEmailService.sendMsisdnVerificationEmail(resendOTPRequest.email(), newOTP);

            if (!emailSent)
                throw new ApiBusinessException(exception_msg + resendOTPRequest.email());

            otpService.storeOTP(resendOTPRequest.email(), newOTP);
        } else {
            throw new ApiOTPExpirationException("OTP still valid. Please try again after expiration.");
        }
    }

    @Override
    public UserRegistrationResponse registerUser(UserRegistrationRequest userRegistrationRequest) {
        User candidate = userCacheService.findByEmail(userRegistrationRequest.email());

        Role role = roleRepo.findByName(RoleEnum.USER.toString()).orElseThrow(() -> new ApiBusinessException("Role User not found"));

        User user = User.builder()
                .userId(candidate.getUserId())
                .email(candidate.getEmail())
                .username(userRegistrationRequest.username())
                .password(passwordEncoder.encode(userRegistrationRequest.password()))
                .country(userRegistrationRequest.country())
                .isVerified(true)
                .build();
        user.addRole(role);
        User savedUser = userCacheService.save(user);

        return UserRegistrationResponse.from(savedUser);
    }

    @Override
    public UserProfileResponse getProfile(String jwtToken) {
        var authentication = jwtTokenParser.parse(TokenType.Access, jwtToken);
        var user = userCacheService.findByEmail(authentication.getName());

        return UserProfileResponse.from(user);
    }

    @Override
    public UserProfileResponse changePassword(String jwtToken, ChangePasswordRequest changePasswordRequest) {
        var authentication = jwtTokenParser.parse(TokenType.Access, jwtToken);
        var user = userCacheService.findByEmail(authentication.getName());
        if (passwordEncoder.matches(changePasswordRequest.oldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(changePasswordRequest.newPassword()));
            userCacheService.save(user);
            return UserProfileResponse.from(user);
        } else {
            throw new ApiValidationException(List.of("Invalid old password."));
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        User user = userCacheService.findByEmail(resetPasswordRequest.email());
        boolean state = user.isVerified();
        if (state) {
            otpService.rateLimitingOTP(resetPasswordRequest.email());

            // No OTP exists or expired, generate a new OTP
            String newOTP = otpService.generateOTP();
            boolean emailSent = mockEmailService.sendResetPasswordEmail(resetPasswordRequest.email(), newOTP);

            if (!emailSent)
                throw new ApiBusinessException(exception_msg + resetPasswordRequest.email());

            otpService.storeOTP(resetPasswordRequest.email(), newOTP);
        } else {
            throw new ApiValidationException(List.of("User is not verified."));
        }
    }

    @Override
    public void resetPasswordConfirm(ResetPasswordConfirmRequest resetPasswordConfirmRequest) {
        otpService.verifyOTP(resetPasswordConfirmRequest.email(), resetPasswordConfirmRequest.OTP());
        User user = userCacheService.findByEmail(resetPasswordConfirmRequest.email());
        //Mock generate reset pin for login password
        String resetPin = otpService.generateOTP();
        user.setPassword(passwordEncoder.encode(resetPin));
        userCacheService.save(user);
        mockEmailService.sendResetPinAsTempPassword(resetPasswordConfirmRequest.email(), resetPin);
    }
}

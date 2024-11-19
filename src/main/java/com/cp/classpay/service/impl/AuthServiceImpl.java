package com.cp.classpay.service.impl;

import com.cp.classpay.api.commons.RoleEnum;
import com.cp.classpay.api.input.auth.*;
import com.cp.classpay.api.output.auth.UserProfileResponse;
import com.cp.classpay.api.output.auth.VerifyOTPResponse;
import com.cp.classpay.api.output.auth.UserRegistrationResponse;
import com.cp.classpay.entity.Role;
import com.cp.classpay.entity.User;
import com.cp.classpay.exceptions.ApiBusinessException;
import com.cp.classpay.exceptions.ApiValidationException;
import com.cp.classpay.exceptions.Api_OTP_Expiration_Exception;
import com.cp.classpay.repository.RoleRepo;
import com.cp.classpay.security.token.JwtTokenParser;
import com.cp.classpay.security.token.TokenType;
import com.cp.classpay.service.AuthService;
import com.cp.classpay.service.MockEmailService;
import com.cp.classpay.service.OTPService;
import com.cp.classpay.service.UserService;
import com.cp.classpay.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserService userService;
    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private JwtTokenParser jwtTokenParser;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private OTPService otpService;
    @Autowired
    private MockEmailService mockEmailService;
    @Autowired
    private RedisUtil redisUtil;

    private static final String USER_VERIFIED_PREFIX = "USER_VERIFIED:";

    @Override
    public void generateOTP(GenerateOTPRequest generateOTPRequest) {
        String newOTP = otpService.generateOTP();
        boolean emailSent = mockEmailService.sendMsisdnVerificationEmail(generateOTPRequest.email(), newOTP);

        if (!emailSent) {
            throw new ApiBusinessException("Failed to send verification email to user with " + generateOTPRequest.email());
        }
        otpService.storeOTP(generateOTPRequest.email(), newOTP);
    }

    @Override
    public VerifyOTPResponse verifyOTP(VerifyOTPRequest verifyOTPRequest) {
        otpService.verifyOTP(verifyOTPRequest.email(), verifyOTPRequest.OTP());
        User user = userService.findByEmailOrElse(verifyOTPRequest.email());
        boolean userVerified = isUserVerified(user);
        if (userVerified) {
            storeUserVerifiedState(verifyOTPRequest.email(), true);
        } else {
            storeUserVerifiedState(verifyOTPRequest.email(), false);
        }
        return VerifyOTPResponse.from(userVerified);
    }

    @Override
    public void resendOTP(ResendOTPRequest resendOTPRequest) {
        String aliveOTP = otpService.getOTP(resendOTPRequest.email());
        if (aliveOTP == null) {
            otpService.rateLimitingOTP(resendOTPRequest.email());

            // No OTP exists or expired, generate a new OTP
            String newOTP = otpService.generateOTP();
            boolean emailSent = mockEmailService.sendMsisdnVerificationEmail(resendOTPRequest.email(), newOTP);

            if (!emailSent) {
                throw new ApiBusinessException("Failed to send verification email to user with " + resendOTPRequest.email());
            }

            otpService.storeOTP(resendOTPRequest.email(), newOTP);
        } else {
            throw new Api_OTP_Expiration_Exception("OTP still valid. Please try again after expiration.");
        }
    }

    @Override
    public UserRegistrationResponse register(UserRegistrationRequest userRegistrationRequest) {
        User userExisted = userService.findByEmailOrElse(userRegistrationRequest.email());
        if (userExisted != null) {
            throw new ApiBusinessException("User already exists");
        }

        Role role = roleRepo.findByName(RoleEnum.USER.toString()).orElseThrow(() -> new ApiBusinessException("Role User not found"));

        User userEntity = User.builder()
                .username(userRegistrationRequest.username())
                .email(userRegistrationRequest.email())
                .password(passwordEncoder.encode(userRegistrationRequest.password()))
                .country(userRegistrationRequest.country())
                .isVerified(true)
                .build();
        userEntity.addRole(role);
        User savedUser = userService.save(userEntity);

        storeUserVerifiedState(userRegistrationRequest.email(), true);

        return UserRegistrationResponse.from(savedUser);
    }

    @Override
    public UserProfileResponse getProfile(String jwtToken) {
        var authentication = jwtTokenParser.parse(TokenType.Access, jwtToken);
        var user = userService.findByEmail(authentication.getName());

        return UserProfileResponse.from(user);
    }

    @Override
    public UserProfileResponse changePassword(String jwtToken, ChangePasswordRequest changePasswordRequest) {
        var authentication = jwtTokenParser.parse(TokenType.Access, jwtToken);
        var user = userService.findByEmail(authentication.getName());
        if (passwordEncoder.matches(changePasswordRequest.oldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(changePasswordRequest.newPassword()));
            userService.save(user);
            return UserProfileResponse.from(user);
        } else {
            throw new ApiValidationException(List.of("Invalid old password."));
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {

        Optional<Boolean> userVerifiedState = getUserVerifiedState(resetPasswordRequest.email());
        boolean state = userVerifiedState.orElse(false);
        if (state) {
            otpService.rateLimitingOTP(resetPasswordRequest.email());

            // No OTP exists or expired, generate a new OTP
            String newOTP = otpService.generateOTP();
            boolean emailSent = mockEmailService.sendResetPasswordEmail(resetPasswordRequest.email(), newOTP);

            if (!emailSent) {
                throw new ApiBusinessException("Failed to send verification email to user with " + resetPasswordRequest.email());
            }

            otpService.storeOTP(resetPasswordRequest.email(), newOTP);
        } else {
            throw new ApiValidationException(List.of("User Verified state Incorrect."));
        }
    }

    @Override
    public void resetPasswordConfirm(ResetPasswordConfirmRequest resetPasswordConfirmRequest) {
        otpService.verifyOTP(resetPasswordConfirmRequest.email(), resetPasswordConfirmRequest.OTP());
        User user = userService.findByEmail(resetPasswordConfirmRequest.email());
        //Mock generate reset pin for login password
        String resetPin = otpService.generateOTP();
        user.setPassword(passwordEncoder.encode(resetPin));
        userService.save(user);
        mockEmailService.sendResetPinAsTempPassword(resetPasswordConfirmRequest.email(), resetPin);
    }


    private boolean isUserVerified(User user) {
        // If user is null or not verified, return false
        return user != null && user.isVerified();
    }


    private void storeUserVerifiedState(String email, boolean isVerified) {
        redisUtil.setWithoutExpiration(USER_VERIFIED_PREFIX + email, isVerified);
    }

    public Optional<Boolean> getUserVerifiedState(String email) {
        try {
            return Optional.ofNullable(redisUtil.get(USER_VERIFIED_PREFIX + email))
                    .filter(Boolean.class::isInstance)
                    .map(Boolean.class::cast);
        } catch (DataAccessException e) {
            log.error("Error accessing Redis for user {} verification state: {}", email, e.getMessage());
            return Optional.empty();
        }
    }
}

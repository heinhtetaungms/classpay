package com.cp.classpay.service.impl;

import com.cp.classpay.api.commons.RoleEnum;
import com.cp.classpay.api.input.user.ChangePasswordRequest;
import com.cp.classpay.api.input.user.ResetPasswordRequest;
import com.cp.classpay.api.input.user.UserRegistrationRequest;
import com.cp.classpay.api.output.user.UserProfileResponse;
import com.cp.classpay.api.output.user.UserRegistrationResponse;
import com.cp.classpay.entity.Role;
import com.cp.classpay.entity.User;
import com.cp.classpay.exceptions.ApiBusinessException;
import com.cp.classpay.exceptions.ApiJwtTokenExpirationException;
import com.cp.classpay.exceptions.ApiValidationException;
import com.cp.classpay.exceptions.Api_EmailOTP_Expiration_Exception;
import com.cp.classpay.repository.RoleRepo;
import com.cp.classpay.repository.UserRepo;
import com.cp.classpay.security.token.JwtTokenParser;
import com.cp.classpay.security.token.TokenType;
import com.cp.classpay.service.UserService;
import com.cp.classpay.utils.PrettyOTPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final JwtTokenParser jwtTokenParser;
    private final PasswordEncoder passwordEncoder;
    private final PrettyOTPUtil prettyOTPUtil;

    public UserServiceImpl(UserRepo userRepo, RoleRepo roleRepo, JwtTokenParser jwtTokenParser, PasswordEncoder passwordEncoder, PrettyOTPUtil prettyOTPUtil) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.jwtTokenParser = jwtTokenParser;
        this.passwordEncoder = passwordEncoder;
        this.prettyOTPUtil = prettyOTPUtil;
    }

    @Override
    public UserRegistrationResponse register(UserRegistrationRequest userRegistrationRequest) {
        //TODO: although work need to refactor the condition logic for beautiful
        User userExisted = userRepo.findByEmail(userRegistrationRequest.email()).orElse(null);
        if (userExisted != null) {
            throw new ApiBusinessException("User already exists");
        }

        Role role = roleRepo.findByName(RoleEnum.USER.toString()).orElseThrow(() -> new ApiBusinessException("Role User not found"));

        String OTP = prettyOTPUtil.generatePrettyOTP();
        boolean emailSent = sendRegistrationVerifyEmail(userRegistrationRequest.email(), OTP);

        if (!emailSent) {
            throw new ApiBusinessException("Failed to send verification email to user with " + userRegistrationRequest.email());
        }

        prettyOTPUtil.storeOTP(userRegistrationRequest.email(), OTP);

        User userEntity = User.builder()
                .username(userRegistrationRequest.username())
                .email(userRegistrationRequest.email())
                .password(passwordEncoder.encode(userRegistrationRequest.password()))
                .country(userRegistrationRequest.country())
                .isVerified(false)
                .build();
        userEntity.addRole(role);
        User savedUser = userRepo.save(userEntity);

        return UserRegistrationResponse.from(savedUser);
    }

    @Override
    public User verifyOTP(String email, String OTP) {
        String aliveOTP = prettyOTPUtil.getOTP(email);
        if (aliveOTP == null || !aliveOTP.equals(OTP)) {
            throw new Api_EmailOTP_Expiration_Exception("Email verification OTP expired");
        }

        User user = userRepo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Invalid or expired email verification OTP."));
        user.setVerified(true);
        userRepo.save(user);

        prettyOTPUtil.deleteOTP(email);

        log.info("User with email {} has been successfully verified OTP.", user.getEmail());
        return user;
    }

    @Override
    public UserProfileResponse getProfile(String jwtToken) {
        var authentication = jwtTokenParser.parse(TokenType.Access, jwtToken);
        var user = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid login id."));

        return UserProfileResponse.from(user);
    }

    @Override
    public UserProfileResponse changePassword(String jwtToken, ChangePasswordRequest changePasswordRequest) {
        var authentication = jwtTokenParser.parse(TokenType.Access, jwtToken);
        var user = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid login id."));
        if (passwordEncoder.matches(changePasswordRequest.oldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(changePasswordRequest.newPassword()));
            userRepo.save(user);
            return UserProfileResponse.from(user);
        } else {
            throw new ApiValidationException(List.of("Invalid old password."));
        }
    }



    @Override
    public void resendOTP(String email) {
        var user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid login id."));

        if (user.isVerified()) {
            throw new ApiJwtTokenExpirationException("User is already verified. No need to resend OTP.");
        }

        processOTP(email);

    }

    private void processOTP(String email) {
        prettyOTPUtil.rateLimitingOTP(email);

        String aliveOTP = prettyOTPUtil.getOTP(email);
        if (aliveOTP == null) {
            // No OTP exists or expired, generate a new OTP
            String New_OTP = prettyOTPUtil.generatePrettyOTP();
            boolean emailSent = sendRegistrationVerifyEmail(email, New_OTP);

            if (!emailSent) {
                throw new ApiBusinessException("Failed to send verification email to user with " + email);
            }

            prettyOTPUtil.storeOTP(email, New_OTP);
        } else {
            throw new Api_EmailOTP_Expiration_Exception("OTP still valid. Please try again after expiration.");
        }
    }

    private boolean sendRegistrationVerifyEmail(String email, String OTP) {

        // Mock sending an email by printing or logging
        log.info("[User Registration] Mock Email sent to {} with verification link:  http://localhost:8080/cp/users/verifyOTP?email={}&OTP={}", email, email, OTP);
        return true;
    }



    @Override
    public void resetPasswordRequest(String email) {
        var user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid login id."));

        if (!user.isVerified()) {
            throw new ApiValidationException(List.of("Verify the email first before resetting password."));
        }

        resetPasswordOTPProcess(email);

    }

    @Override
    public void verifyResetPasswordOTP(String email, String OTP) {
        String aliveOTP = prettyOTPUtil.getOTP(email);
        if (aliveOTP == null || !aliveOTP.equals(OTP)) {
            throw new Api_EmailOTP_Expiration_Exception("Email verification OTP expired");
        }

        prettyOTPUtil.deleteOTP(email);

        log.info("User with email {} has been successfully verified OTP for Rest Password.", email);
    }

    @Override
    public UserProfileResponse resetPassword(ResetPasswordRequest resetPasswordRequest) {
        var user = userRepo.findByEmail(resetPasswordRequest.email())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid login id."));
        user.setPassword(passwordEncoder.encode(resetPasswordRequest.newPassword()));
        userRepo.save(user);
        return UserProfileResponse.from(user);
    }

    private void resetPasswordOTPProcess(String email) {
        prettyOTPUtil.rateLimitingOTP(email);

        String aliveOTP = prettyOTPUtil.getOTP(email);
        if (aliveOTP == null) {
            // No OTP exists or expired, generate a new OTP
            String New_OTP = prettyOTPUtil.generatePrettyOTP();
            boolean emailSent = sendResetPasswordVerifyEmail(email, New_OTP);

            if (!emailSent) {
                throw new ApiBusinessException("Failed to send verification email to user with " + email);
            }

            prettyOTPUtil.storeOTP(email, New_OTP);
        } else {
            throw new Api_EmailOTP_Expiration_Exception("OTP still valid. Please try again after expiration.");
        }
    }

    private boolean sendResetPasswordVerifyEmail(String email, String OTP) {

        // Mock sending an email by printing or logging
        log.info("[Reset Password] Mock Email sent to {} with verification link:  http://localhost:8080/cp/users/verifyResetPasswordOTP?email={}&OTP={}", email, email, OTP);
        return true;
    }

}

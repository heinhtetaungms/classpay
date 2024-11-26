package com.cp.classpay.api.output.auth;

import com.cp.classpay.entity.User;

public record VerifyOTPResponse (
        String email,
        boolean isVerified) {

    public static VerifyOTPResponse from(User user) {
        return new VerifyOTPResponse(
                user.getEmail(),
                user.isVerified());
    }
}
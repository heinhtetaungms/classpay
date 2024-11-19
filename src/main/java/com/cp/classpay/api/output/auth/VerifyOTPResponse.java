package com.cp.classpay.api.output.auth;

public record VerifyOTPResponse (
        boolean isVerified) {

    public static VerifyOTPResponse from(boolean isVerified) {
        return new VerifyOTPResponse(isVerified);
    }
}
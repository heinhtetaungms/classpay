package com.cp.classpay.api.input.auth;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordConfirmRequest(
        @NotBlank(message = "Please enter email.")
        String email,
        @NotBlank(message = "Please enter OTP.")
        String OTP
) {
}


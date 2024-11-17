package com.cp.classpay.api.input.user;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "Please enter email.")
        String email,
        @NotBlank(message = "Please enter newPassword.")
        String newPassword
) {
}


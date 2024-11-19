package com.cp.classpay.api.input.auth;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "Please enter email.")
        String email
        ) {
}


package com.cp.classpay.api.input.user;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "Please enter oldPassword.")
        String oldPassword,
        @NotBlank(message = "Please enter newPassword.")
        String newPassword
        ) {
}

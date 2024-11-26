package com.cp.classpay.api.input.package_;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record PackageRegisterRequest(
        @NotBlank(message = "Please enter package name.")
        String packageName,
        @NotBlank(message = "Please enter total credits.")
        int totalCredits,
        BigDecimal price,
        @NotBlank(message = "Please enter expiry days.")
        int expiryDays,
        @NotBlank(message = "Please enter country.")
        String country
) {
}


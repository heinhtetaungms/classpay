package com.cp.classpay.api.input.booking;

import jakarta.validation.constraints.NotNull;

public record BookingClassRequest(
        @NotNull(message = "Please enter class id.")
        Long classId,
        boolean waitlistPrefer
) {
}

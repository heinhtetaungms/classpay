package com.cp.classpay.api.input.booking;

import jakarta.validation.constraints.NotNull;

public record CheckInBookedClassRequest(
        @NotNull(message = "Please enter class id.")
        Long bookingId
) {
}

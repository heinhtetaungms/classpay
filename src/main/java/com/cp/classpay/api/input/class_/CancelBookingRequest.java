package com.cp.classpay.api.input.class_;

import jakarta.validation.constraints.NotNull;

public record CancelBookingRequest(
        @NotNull(message = "Please enter booking id.")
        Long bookingId
) {
}

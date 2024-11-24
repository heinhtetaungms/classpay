package com.cp.classpay.api.output.booking;

import com.cp.classpay.commons.enum_.BookingStatus;


public record BookingResponse(
        BookingStatus status
) {
    public static BookingResponse toBookingResponse(BookingStatus status) {
        return new BookingResponse(
                status
        );
    }
}

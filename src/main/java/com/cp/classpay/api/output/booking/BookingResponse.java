package com.cp.classpay.api.output.booking;

import com.cp.classpay.commons.enum_.BookingStatus;


public record BookingResponse(
        BookingStatus status
) {
    public static BookingResponse from(BookingStatus status) {
        return new BookingResponse(
                status
        );
    }
}

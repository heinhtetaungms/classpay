package com.cp.classpay.api.output.booking;

import com.cp.classpay.commons.enum_.BookingStatus;
import com.cp.classpay.entity.Booking;

import java.time.ZonedDateTime;

public record CancelBookingResponse(
        Long bookingId,
        String className,
        ZonedDateTime bookingTime,
        BookingStatus status,
        boolean isCanceled,
        ZonedDateTime cancellationTime

) {
    public static CancelBookingResponse from(Booking booking) {
        return new CancelBookingResponse(
                booking.getBookingId(),
                booking.getClassEntity().getClassName(),
                booking.getBookingTime(),
                booking.getStatus(),
                booking.isCanceled(),
                booking.getCancellationTime()
        );
    }
}

package com.cp.classpay.api.output.booking;

import com.cp.classpay.commons.enum_.BookingStatus;
import com.cp.classpay.entity.Booking;


public record BookingResponse(
        Long classId,
        String className,
        BookingStatus status
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getClassEntity().getClassId(),
                booking.getClassEntity().getClassName(),
                booking.getStatus()
        );
    }
}

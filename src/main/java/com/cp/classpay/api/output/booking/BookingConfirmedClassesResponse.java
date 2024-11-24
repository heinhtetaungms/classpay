package com.cp.classpay.api.output.booking;

import com.cp.classpay.entity.Booking;

import java.time.ZonedDateTime;


public record BookingConfirmedClassesResponse(
        Long bookingId,
        Long userId,
        Long classId,
        String className,
        String country,
        ZonedDateTime classDate
) {
    public static BookingConfirmedClassesResponse toBookingResponse(Booking booking) {
        return new BookingConfirmedClassesResponse(
                booking.getBookingId(),
                booking.getUser().getUserId(),
                booking.getClassEntity().getClassId(),
                booking.getClassEntity().getClassName(),
                booking.getClassEntity().getCountry(),
                booking.getClassEntity().getClassStartDate()
        );
    }
}

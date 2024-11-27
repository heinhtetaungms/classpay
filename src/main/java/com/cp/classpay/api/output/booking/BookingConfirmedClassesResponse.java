package com.cp.classpay.api.output.booking;

import com.cp.classpay.entity.Booking;

import java.time.ZonedDateTime;


public record BookingConfirmedClassesResponse(
        Long bookingId,
        Long userId,
        Long classId,
        String className,
        String country,
        ZonedDateTime classStartDate,
        ZonedDateTime classEndDate
        ) {
    public static BookingConfirmedClassesResponse from(Booking booking) {
        return new BookingConfirmedClassesResponse(
                booking.getBookingId(),
                booking.getUser().getUserId(),
                booking.getClassEntity().getClassId(),
                booking.getClassEntity().getClassName(),
                booking.getClassEntity().getCountry(),
                booking.getClassEntity().getClassStartDate(),
                booking.getClassEntity().getClassEndDate()
        );
    }
}

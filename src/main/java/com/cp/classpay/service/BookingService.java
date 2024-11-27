package com.cp.classpay.service;

import com.cp.classpay.api.input.booking.BookingClassRequest;
import com.cp.classpay.api.input.booking.CheckInBookedClassRequest;
import com.cp.classpay.api.input.class_.CancelBookingRequest;
import com.cp.classpay.api.output.booking.BookingConfirmedClassesResponse;
import com.cp.classpay.api.output.booking.BookingResponse;
import com.cp.classpay.api.output.booking.CancelBookingResponse;

import java.util.List;

public interface BookingService {
    List<BookingConfirmedClassesResponse> bookingConfirmedClasses();
    BookingResponse bookingClass(BookingClassRequest bookingClassRequest);
    BookingResponse checkInBookedClass(CheckInBookedClassRequest checkInBookedClassRequest);
    CancelBookingResponse cancelBooking(CancelBookingRequest cancelBookingRequest);
}

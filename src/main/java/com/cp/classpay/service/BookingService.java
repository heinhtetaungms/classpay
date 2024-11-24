package com.cp.classpay.service;

import com.cp.classpay.api.input.booking.BookingClassRequest;
import com.cp.classpay.api.input.class_.CancelBookingRequest;
import com.cp.classpay.api.output.booking.BookingConfirmedClassesResponse;
import com.cp.classpay.api.output.booking.BookingResponse;
import com.cp.classpay.api.output.booking.CancelBookingResponse;

import java.util.List;

public interface BookingService {
    List<BookingConfirmedClassesResponse> bookingConfirmedClasses(String jwtToken);
    BookingResponse bookingClass(String jwtToken, BookingClassRequest bookingClassRequest);
    CancelBookingResponse cancelBooking(String jwtToken, CancelBookingRequest cancelBookingRequest);
}

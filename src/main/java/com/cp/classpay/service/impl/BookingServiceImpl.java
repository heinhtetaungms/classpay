package com.cp.classpay.service.impl;

import com.cp.classpay.api.input.booking.BookingClassRequest;
import com.cp.classpay.api.input.class_.CancelBookingRequest;
import com.cp.classpay.api.output.booking.BookingConfirmedClassesResponse;
import com.cp.classpay.api.output.booking.BookingResponse;
import com.cp.classpay.api.output.booking.CancelBookingResponse;
import com.cp.classpay.commons.enum_.BookingStatus;
import com.cp.classpay.entity.Booking;
import com.cp.classpay.service.BookingService;
import com.cp.classpay.service.cache.BookingCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingCacheService bookingCacheService;

    @Override
    public List<BookingConfirmedClassesResponse> bookingConfirmedClasses(String jwtToken) {
        return bookingCacheService.bookingConfirmedClasses(jwtToken);
    }

    @Override
    public BookingResponse bookingClass(String jwtToken, BookingClassRequest bookingClassRequest) {
        BookingStatus status = bookingCacheService.bookingClass(jwtToken, bookingClassRequest);
        return BookingResponse.toBookingResponse(status);
    }

    @Override
    public CancelBookingResponse cancelBooking(String jwtToken, CancelBookingRequest cancelBookingRequest) {
        Booking booking = bookingCacheService.cancelBooking(jwtToken, cancelBookingRequest);
        return CancelBookingResponse.toCancelBookingResponse(booking);
    }

}

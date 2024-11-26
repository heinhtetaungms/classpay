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
    public List<BookingConfirmedClassesResponse> bookingConfirmedClasses() {
        return bookingCacheService.bookingConfirmedClasses();
    }

    @Override
    public BookingResponse bookingClass(BookingClassRequest bookingClassRequest) {
        BookingStatus status = bookingCacheService.bookingClass(bookingClassRequest);
        return BookingResponse.from(status);
    }

    @Override
    public CancelBookingResponse cancelBooking(CancelBookingRequest cancelBookingRequest) {
        Booking booking = bookingCacheService.cancelBooking(cancelBookingRequest);
        return CancelBookingResponse.from(booking);
    }

}

package com.cp.classpay.api.controller;

import com.cp.classpay.api.input.booking.BookingClassRequest;
import com.cp.classpay.api.input.booking.CheckInBookedClassRequest;
import com.cp.classpay.api.input.class_.CancelBookingRequest;
import com.cp.classpay.api.output.booking.BookingConfirmedClassesResponse;
import com.cp.classpay.api.output.booking.BookingResponse;
import com.cp.classpay.api.output.booking.CancelBookingResponse;
import com.cp.classpay.service.BookingService;
import com.cp.classpay.utils.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/booking")
public class BookingApi {

    private final BookingService bookingService;

    public BookingApi(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/confirmed-classes")
    public ResponseEntity<ApiResponse<List<BookingConfirmedClassesResponse>>> bookingConfirmedClasses() {
        List<BookingConfirmedClassesResponse> bookingConfirmed = bookingService.bookingConfirmedClasses();
        return ApiResponse.of(bookingConfirmed);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> bookingClass(@Validated @RequestBody BookingClassRequest bookingClassRequest, BindingResult result) {
        return ApiResponse.of(bookingService.bookingClass(bookingClassRequest));
    }

    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<BookingResponse>> checkInBookedClass(@Validated @RequestBody CheckInBookedClassRequest checkInBookedClassRequest, BindingResult result) {
        return ApiResponse.of(bookingService.checkInBookedClass(checkInBookedClassRequest));
    }

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<CancelBookingResponse>> cancelBooking(@Validated @RequestBody CancelBookingRequest cancelBookingRequest, BindingResult result) {
        return ApiResponse.of(bookingService.cancelBooking(cancelBookingRequest));
    }
}

package com.cp.classpay.api.controller;

import com.cp.classpay.api.input.booking.BookingClassRequest;
import com.cp.classpay.api.input.class_.CancelBookingRequest;
import com.cp.classpay.api.output.booking.BookingConfirmedClassesResponse;
import com.cp.classpay.api.output.booking.BookingResponse;
import com.cp.classpay.api.output.booking.CancelBookingResponse;
import com.cp.classpay.entity.Booking;
import com.cp.classpay.service.BookingService;
import com.cp.classpay.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/booking")
public class BookingApi {

    @Autowired
    private BookingService bookingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingConfirmedClassesResponse>>> bookingConfirmedClasses(@RequestHeader("Authorization") String jwtToken) {
        List<BookingConfirmedClassesResponse> bookingConfirmed = bookingService.bookingConfirmedClasses(jwtToken);
        return ApiResponse.of(bookingConfirmed);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> bookingClass(@RequestHeader("Authorization") String jwtToken, @Validated @RequestBody BookingClassRequest bookingClassRequest, BindingResult result) {
        return ApiResponse.of(bookingService.bookingClass(jwtToken, bookingClassRequest));
    }

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<CancelBookingResponse>> cancelBooking(@RequestHeader("Authorization") String jwtToken, @Validated @RequestBody CancelBookingRequest cancelBookingRequest, BindingResult result) {
        return ApiResponse.of(bookingService.cancelBooking(jwtToken, cancelBookingRequest));
    }
}

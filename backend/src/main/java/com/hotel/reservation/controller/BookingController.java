package com.hotel.reservation.controller;

import com.hotel.reservation.dto.BookingDtos.*;
import com.hotel.reservation.entity.User;
import com.hotel.reservation.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponse create(@AuthenticationPrincipal User user,
                                  @Valid @RequestBody BookingRequest req) {
        return bookingService.create(user, req);
    }

    @GetMapping("/me")
    public List<BookingResponse> myBookings(@AuthenticationPrincipal User user) {
        return bookingService.myBookings(user.getId());
    }

    @DeleteMapping("/{id}")
    public BookingResponse cancel(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return bookingService.cancel(user, id);
    }
}

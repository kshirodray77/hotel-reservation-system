package com.hotel.reservation.controller;

import com.hotel.reservation.dto.BookingDtos.BookingResponse;
import com.hotel.reservation.service.AdminService;
import com.hotel.reservation.service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final BookingService bookingService;
    private final AdminService adminService;

    public AdminController(BookingService bookingService, AdminService adminService) {
        this.bookingService = bookingService;
        this.adminService = adminService;
    }

    @GetMapping("/bookings")
    public List<BookingResponse> allBookings() {
        return bookingService.all();
    }

    @GetMapping("/reports/revenue")
    public Map<String, Object> revenueReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return adminService.revenueReport(from, to);
    }
}

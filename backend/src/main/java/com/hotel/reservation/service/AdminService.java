package com.hotel.reservation.service;

import com.hotel.reservation.repository.BookingRepository;
import com.hotel.reservation.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AdminService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public AdminService(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    public Map<String, Object> revenueReport(LocalDate from, LocalDate to) {
        if (from == null) from = LocalDate.now().withDayOfMonth(1);
        if (to   == null) to   = LocalDate.now();
        BigDecimal total = bookingRepository.totalRevenueBetween(from, to);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("from", from);
        out.put("to", to);
        out.put("totalRevenue", total);
        out.put("totalRooms", roomRepository.count());
        out.put("totalBookings", bookingRepository.count());
        return out;
    }
}

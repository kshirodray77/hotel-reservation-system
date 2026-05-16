package com.hotel.reservation.dto;

import com.hotel.reservation.entity.Booking;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingDtos {

    public record BookingRequest(
            @NotNull Long roomId,
            @NotNull @FutureOrPresent LocalDate checkIn,
            @NotNull @Future LocalDate checkOut,
            @Min(1) @Max(10) Integer guests,
            String specialRequest
    ) {}

    public record BookingResponse(
            Long id,
            Long roomId,
            String roomNumber,
            String roomType,
            String roomImageUrl,
            Long userId,
            String userEmail,
            String userFullName,
            LocalDate checkIn,
            LocalDate checkOut,
            Integer guests,
            BigDecimal totalPrice,
            String status,
            String specialRequest
    ) {
        public static BookingResponse from(Booking b) {
            return new BookingResponse(
                    b.getId(),
                    b.getRoom().getId(),
                    b.getRoom().getRoomNumber(),
                    b.getRoom().getType().name(),
                    b.getRoom().getImageUrl(),
                    b.getUser().getId(),
                    b.getUser().getEmail(),
                    b.getUser().getFullName(),
                    b.getCheckIn(),
                    b.getCheckOut(),
                    b.getGuests(),
                    b.getTotalPrice(),
                    b.getStatus().name(),
                    b.getSpecialRequest()
            );
        }
    }
}

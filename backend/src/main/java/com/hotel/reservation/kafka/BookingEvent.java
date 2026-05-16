package com.hotel.reservation.kafka;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain event broadcast whenever a booking transitions state.
 * type: CREATED | CONFIRMED | CANCELLED | COMPLETED
 */
public record BookingEvent(
        String   type,
        Long     bookingId,
        Long     userId,
        String   userEmail,
        Long     roomId,
        String   roomNumber,
        LocalDate checkIn,
        LocalDate checkOut,
        BigDecimal totalPrice,
        LocalDateTime occurredAt
) {}

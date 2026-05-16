package com.hotel.reservation.kafka;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * type: SUCCEEDED | FAILED | REFUNDED
 */
public record PaymentEvent(
        String   type,
        Long     paymentId,
        Long     bookingId,
        Long     userId,
        BigDecimal amount,
        String   transactionId,
        LocalDateTime occurredAt
) {}

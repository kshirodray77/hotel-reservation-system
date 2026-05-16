package com.hotel.reservation.dto;

import com.hotel.reservation.entity.Payment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDtos {

    public record CheckoutRequest(
            @NotNull Long bookingId,
            @NotBlank String method,        // CARD, PAYPAL, etc.
            @NotBlank String cardNumber,    // mock — not stored
            @NotBlank String cardHolder,
            @NotBlank String expiry,        // MM/YY
            @NotBlank String cvv
    ) {}

    public record PaymentResponse(
            Long id,
            Long bookingId,
            BigDecimal amount,
            String method,
            String transactionId,
            String status,
            LocalDateTime paidAt
    ) {
        public static PaymentResponse from(Payment p) {
            return new PaymentResponse(
                    p.getId(),
                    p.getBooking().getId(),
                    p.getAmount(),
                    p.getMethod(),
                    p.getTransactionId(),
                    p.getStatus().name(),
                    p.getPaidAt()
            );
        }
    }
}

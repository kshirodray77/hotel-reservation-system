package com.hotel.reservation.service;

import com.hotel.reservation.dto.PaymentDtos.*;
import com.hotel.reservation.entity.Booking;
import com.hotel.reservation.entity.Payment;
import com.hotel.reservation.entity.User;
import com.hotel.reservation.exception.ApiException;
import com.hotel.reservation.kafka.EventProducer;
import com.hotel.reservation.kafka.PaymentEvent;
import com.hotel.reservation.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final EventProducer producer;

    public PaymentService(PaymentRepository paymentRepository,
                          BookingService bookingService,
                          EventProducer producer) {
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
        this.producer = producer;
    }

    /**
     * Mock checkout. In production this would call Stripe / Adyen / etc.
     * We simulate success for any card whose number doesn't end with "0000".
     *
     * noRollbackFor = ApiException so we keep the FAILED payment row when
     * we throw HTTP 402 to the client.
     */
    @Transactional(noRollbackFor = ApiException.class)
    public PaymentResponse checkout(User user, CheckoutRequest req) {
        Booking booking = bookingService.findById(req.bookingId());
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not your booking");
        }
        if (booking.getStatus() != Booking.Status.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Booking is " + booking.getStatus() + " — cannot pay");
        }
        if (paymentRepository.findByBookingId(booking.getId()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Payment already exists for this booking");
        }

        boolean success = !req.cardNumber().endsWith("0000");
        String txn = "txn_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalPrice())
                .method(req.method())
                .transactionId(txn)
                .status(success ? Payment.Status.SUCCESS : Payment.Status.FAILED)
                .paidAt(success ? LocalDateTime.now() : null)
                .build();
        paymentRepository.save(payment);

        producer.publishPayment(new PaymentEvent(
                success ? "SUCCEEDED" : "FAILED",
                payment.getId(),
                booking.getId(),
                user.getId(),
                payment.getAmount(),
                txn,
                LocalDateTime.now()));

        if (!success) {
            throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "Payment was declined");
        }
        return PaymentResponse.from(payment);
    }
}

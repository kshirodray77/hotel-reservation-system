package com.hotel.reservation.kafka;

import com.hotel.reservation.entity.Booking;
import com.hotel.reservation.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * On a successful payment, transition the related booking
 * to CONFIRMED and emit a downstream notification.
 */
@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final BookingRepository bookingRepository;
    private final EventProducer producer;

    public PaymentEventConsumer(BookingRepository bookingRepository, EventProducer producer) {
        this.bookingRepository = bookingRepository;
        this.producer = producer;
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-events}", groupId = "booking-confirmation")
    @Transactional
    public void onPayment(PaymentEvent evt) {
        log.info("[KAFKA <- payment-events] {}", evt);
        if (!"SUCCEEDED".equals(evt.type())) return;

        bookingRepository.findById(evt.bookingId()).ifPresent(b -> {
            if (b.getStatus() == Booking.Status.PENDING) {
                b.setStatus(Booking.Status.CONFIRMED);
                bookingRepository.save(b);

                producer.publishBooking(new BookingEvent(
                        "CONFIRMED",
                        b.getId(),
                        b.getUser().getId(),
                        b.getUser().getEmail(),
                        b.getRoom().getId(),
                        b.getRoom().getRoomNumber(),
                        b.getCheckIn(),
                        b.getCheckOut(),
                        b.getTotalPrice(),
                        LocalDateTime.now()
                ));
            }
        });
    }
}

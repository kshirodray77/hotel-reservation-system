package com.hotel.reservation.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Listens for booking lifecycle events and translates them
 * into outbound notifications + analytics counters.
 */
@Component
public class BookingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventConsumer.class);

    private final EventProducer producer;

    public BookingEventConsumer(EventProducer producer) {
        this.producer = producer;
    }

    @KafkaListener(topics = "${app.kafka.topics.booking-events}", groupId = "notification-service")
    public void onBooking(BookingEvent evt) {
        log.info("[KAFKA <- booking-events] {}", evt);
        String subject;
        String body;
        switch (evt.type()) {
            case "CREATED"  -> { subject = "Booking received";  body = "We've received your booking #" + evt.bookingId() + ". Please complete payment to confirm."; }
            case "CONFIRMED"-> { subject = "Booking confirmed"; body = "Your booking #" + evt.bookingId() + " is confirmed. See you on " + evt.checkIn() + "."; }
            case "CANCELLED"-> { subject = "Booking cancelled"; body = "Booking #" + evt.bookingId() + " has been cancelled."; }
            case "COMPLETED"-> { subject = "Thanks for staying"; body = "We hope you enjoyed your stay! Please leave a review for booking #" + evt.bookingId() + "."; }
            default         -> { return; }
        }
        producer.publishNotification(new NotificationEvent(
                "EMAIL", evt.userEmail(), subject, body, LocalDateTime.now()));
    }

    @KafkaListener(topics = "${app.kafka.topics.booking-events}", groupId = "analytics-service")
    public void onBookingForAnalytics(BookingEvent evt) {
        // In production this would push into ClickHouse / BigQuery / a metrics store.
        log.info("[ANALYTICS] booking={} type={} amount={}", evt.bookingId(), evt.type(), evt.totalPrice());
    }
}

package com.hotel.reservation.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    private static final Logger log = LoggerFactory.getLogger(EventProducer.class);

    private final KafkaTemplate<String, Object> kafka;

    @Value("${app.kafka.topics.booking-events}")      private String bookingTopic;
    @Value("${app.kafka.topics.payment-events}")      private String paymentTopic;
    @Value("${app.kafka.topics.notification-events}") private String notificationTopic;

    public EventProducer(KafkaTemplate<String, Object> kafka) {
        this.kafka = kafka;
    }

    public void publishBooking(BookingEvent evt) {
        log.info("[KAFKA -> {}] {}", bookingTopic, evt);
        kafka.send(bookingTopic, String.valueOf(evt.bookingId()), evt);
    }

    public void publishPayment(PaymentEvent evt) {
        log.info("[KAFKA -> {}] {}", paymentTopic, evt);
        kafka.send(paymentTopic, String.valueOf(evt.paymentId()), evt);
    }

    public void publishNotification(NotificationEvent evt) {
        log.info("[KAFKA -> {}] {}", notificationTopic, evt);
        kafka.send(notificationTopic, evt.recipient(), evt);
    }
}

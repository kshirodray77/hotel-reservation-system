package com.hotel.reservation.kafka;

import com.hotel.reservation.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Final hop: turn NotificationEvent payloads into actual
 * emails (or SMS / push) via EmailService.
 */
@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final EmailService emailService;

    public NotificationEventConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "${app.kafka.topics.notification-events}", groupId = "email-dispatcher")
    public void onNotification(NotificationEvent evt) {
        log.info("[KAFKA <- notification-events] {}", evt);
        if ("EMAIL".equals(evt.channel())) {
            emailService.send(evt.recipient(), evt.subject(), evt.body());
        }
        // SMS / PUSH dispatchers would slot in here.
    }
}

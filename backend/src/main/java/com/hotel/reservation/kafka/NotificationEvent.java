package com.hotel.reservation.kafka;

import java.time.LocalDateTime;

/**
 * channel: EMAIL | SMS | PUSH
 */
public record NotificationEvent(
        String channel,
        String recipient,
        String subject,
        String body,
        LocalDateTime occurredAt
) {}

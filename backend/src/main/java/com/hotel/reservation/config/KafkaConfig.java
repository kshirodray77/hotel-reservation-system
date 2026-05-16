package com.hotel.reservation.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${app.kafka.topics.booking-events}")      private String bookingTopic;
    @Value("${app.kafka.topics.payment-events}")      private String paymentTopic;
    @Value("${app.kafka.topics.notification-events}") private String notificationTopic;

    @Bean
    NewTopic bookingTopic() {
        return TopicBuilder.name(bookingTopic).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic paymentTopic() {
        return TopicBuilder.name(paymentTopic).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic notificationTopic() {
        return TopicBuilder.name(notificationTopic).partitions(3).replicas(1).build();
    }
}

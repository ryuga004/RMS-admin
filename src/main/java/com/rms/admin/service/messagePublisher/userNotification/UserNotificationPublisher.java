package com.rms.admin.service.messagePublisher.userNotification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserNotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "notification.exchange";
    private static final String ROUTING_KEY = "notification.message";

    public void publish(UserNotificationEvent event) {
        event.setNotificationId(UUID.randomUUID().toString());
        event.setCreatedAt(Instant.now());
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
        log.info("Published user notification type={} userId={}", event.getType(), event.getUserId());
    }
}

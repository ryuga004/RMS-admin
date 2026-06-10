package com.rms.admin.service.messagePublisher.adminNotification;

import com.rms.admin.utils.constants.QueueConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdminNotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishEmail(Email message) {
        try {
            rabbitTemplate.convertAndSend(
                    QueueConstants.AdminNotification.EXCHANGE,
                    QueueConstants.AdminNotification.RoutingKey.MAIN,
                    message
            );
        } catch (Exception e) {
            log.error("Failed to publish email to: {}", message != null ? message.getTo() : "unknown", e);
            throw e;
        }
    }
}
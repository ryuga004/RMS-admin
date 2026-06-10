package com.rms.admin.service.messagePublisher.userNotification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for notification messaging components.
 *
 * Registers beans for:
 * - UserNotificationEventFactory - Factory for creating notification events
 * - UserNotificationPublisher - Publisher for sending events to RabbitMQ
 *
 * Enables dependency injection across handlers and services.
 */
@Configuration
public class NotificationConfig {

    /**
     * Creates the notification event factory bean.
     *
     * This factory provides type-safe methods for creating notification events
     * with automatic validation of required metadata fields.
     *
     * @return UserNotificationEventFactory bean
     */
    @Bean
    public UserNotificationEventFactory userNotificationEventFactory() {
        return new UserNotificationEventFactory();
    }
}

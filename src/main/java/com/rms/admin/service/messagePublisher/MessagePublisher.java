package com.rms.admin.service.messagePublisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public interface MessagePublisher {
    default void publish(Object message, ExchangeDetails exchangeDetails) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.convertAndSend(exchangeDetails.getName(), exchangeDetails.getRoutingKey(), message);
    }
}
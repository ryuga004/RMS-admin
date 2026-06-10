package com.rms.admin.audit;

import com.rms.admin.utils.constants.QueueConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RabbitAuditPublisher implements AuditPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(AuditEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    QueueConstants.Audit.EXCHANGE,
                    QueueConstants.Audit.RoutingKey.MAIN,
                    event
            );
        } catch (Exception e) {
            log.error("Failed to publish audit event: eventId={}, type={}", event != null ? event.getEventId() : null, event != null ? event.getType() : null, e);
            throw e;
        }
    }
}

package com.rms.admin.utils.constants;

public interface QueueConstants  {
    interface AdminNotification {
        String EXCHANGE = "admin-notification.exchange";
        interface Queue {
            String MAIN = "admin-notification.queue";
            String DLQ = "admin-notification.dlq";
        }
        interface RoutingKey {
            String MAIN = "admin.notification.routingkey";
            String DLQ = "admin.notification.dlq";
        }
    }

    interface Audit {
        String EXCHANGE = "admin-audit.exchange";
        interface Queue {
            String MAIN = "admin-audit.queue";
            String DLQ = "admin-audit.dlq";
        }
        interface RoutingKey {
            String MAIN = "admin.audit.routingkey";
            String DLQ = "admin.audit.dlq";
        }
    }
}
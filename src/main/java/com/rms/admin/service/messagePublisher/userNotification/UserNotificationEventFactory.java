package com.rms.admin.service.messagePublisher.userNotification;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating UserNotificationEvent instances.
 *
 * Replaces repetitive builder code with type-safe factory methods.
 * Each method encapsulates:
 * - Notification title & body
 * - Metadata structure
 * - Metadata validation
 *
 * Usage:
 *   UserNotificationEvent event = factory.tenancyRequestReceived(ownerId, assetId, requesterId);
 *   notificationPublisher.publish(event);
 *
 * Replaces:
 *   notificationPublisher.publish(UserNotificationEvent.builder()
 *     .userId(ownerId)
 *     .title("New Tenancy Request")
 *     .body("A new tenancy request has been received for your property")
 *     .type("TENANCY_REQUEST_RECEIVED")
 *     .metadata(Map.of("assetId", assetId, "requesterId", requesterId))
 *     .build());
 */
public class UserNotificationEventFactory {

    // ============================================
    // TENANCY REQUEST NOTIFICATIONS
    // ============================================

    /**
     * Creates notification for when property owner receives a tenancy request.
     */
    public UserNotificationEvent tenancyRequestReceived(
            Long ownerId,
            Long assetId,
            Long requesterId) {
        return build(
            NotificationType.TENANCY_REQUEST_RECEIVED,
            ownerId,
            Map.of("assetId", assetId, "requesterId", requesterId)
        );
    }

    /**
     * Creates notification for when requester's tenancy request is approved.
     */
    public UserNotificationEvent tenancyRequestApproved(
            Long requesterId,
            Long assetId) {
        return build(
            NotificationType.TENANCY_REQUEST_APPROVED,
            requesterId,
            Map.of("assetId", assetId)
        );
    }

    /**
     * Creates notification for when requester's tenancy request is rejected.
     */
    public UserNotificationEvent tenancyRequestRejected(
            Long requesterId,
            Long assetId) {
        return build(
            NotificationType.TENANCY_REQUEST_REJECTED,
            requesterId,
            Map.of("assetId", assetId)
        );
    }

    /**
     * Creates notification for when a tenancy request is cancelled.
     */
    public UserNotificationEvent tenancyRequestCancelled(
            Long recipientId,
            Long assetId) {
        return build(
            NotificationType.TENANCY_REQUEST_CANCELLED,
            recipientId,
            Map.of("assetId", assetId)
        );
    }

    // ============================================
    // INVITATION NOTIFICATIONS
    // ============================================

    /**
     * Creates notification for when tenant receives a rental invitation.
     */
    public UserNotificationEvent invitationReceived(
            Long tenantUserId,
            Long assetId,
            Long ownerId) {
        return build(
            NotificationType.INVITATION_RECEIVED,
            tenantUserId,
            Map.of("assetId", assetId, "ownerId", ownerId)
        );
    }

    /**
     * Creates notification for when owner's invitation is accepted.
     */
    public UserNotificationEvent invitationAccepted(
            Long ownerId,
            Long assetId,
            Long tenantUserId) {
        return build(
            NotificationType.INVITATION_ACCEPTED,
            ownerId,
            Map.of("assetId", assetId, "tenantUserId", tenantUserId)
        );
    }

    /**
     * Creates notification for when owner's invitation is rejected.
     */
    public UserNotificationEvent invitationRejected(
            Long ownerId,
            Long assetId,
            Long tenantUserId) {
        return build(
            NotificationType.INVITATION_REJECTED,
            ownerId,
            Map.of("assetId", assetId, "tenantUserId", tenantUserId)
        );
    }

    /**
     * Creates notification for when an invitation expires.
     */
    public UserNotificationEvent invitationExpired(
            Long tenantUserId,
            Long assetId) {
        return build(
            NotificationType.INVITATION_EXPIRED,
            tenantUserId,
            Map.of("assetId", assetId, "tenantUserId", tenantUserId)
        );
    }

    // ============================================
    // TENANT/ASSET NOTIFICATIONS
    // ============================================

    /**
     * Creates notification for when a tenant is removed from property.
     */
    public UserNotificationEvent tenantRemoved(
            Long ownerId,
            Long assetId,
            Long tenantUserId) {
        return build(
            NotificationType.TENANT_REMOVED,
            ownerId,
            Map.of("assetId", assetId, "tenantUserId", tenantUserId)
        );
    }

    /**
     * Creates notification for when a property is updated.
     */
    public UserNotificationEvent assetUpdated(
            Long ownerId,
            Long assetId) {
        return build(
            NotificationType.ASSET_UPDATED,
            ownerId,
            Map.of("assetId", assetId)
        );
    }

    /**
     * Creates notification for when a property is deleted.
     */
    public UserNotificationEvent assetDeleted(
            Long ownerId,
            Long assetId) {
        return build(
            NotificationType.ASSET_DELETED,
            ownerId,
            Map.of("assetId", assetId)
        );
    }

    // ============================================
    // MESSAGING NOTIFICATIONS
    // ============================================

    /**
     * Creates notification for new message.
     */
    public UserNotificationEvent newMessage(
            Long userId,
            Long conversationId) {
        return build(
            NotificationType.NEW_MESSAGE,
            userId,
            Map.of("conversationId", conversationId)
        );
    }

    // ============================================
    // PAYMENT NOTIFICATIONS
    // ============================================

    /**
     * Creates notification for owner when a tenant makes a payment.
     * In-app only — sent to the property owner.
     */
    public UserNotificationEvent paymentReceived(
            Long ownerId,
            Long assetId,
            Long tenantUserId,
            java.math.BigDecimal amount) {
        return build(
            NotificationType.PAYMENT_RECEIVED,
            ownerId,
            Map.of("assetId", assetId, "tenantUserId", tenantUserId, "amount", amount)
        );
    }

    /**
     * Creates notification for tenant confirming their payment succeeded.
     */
    public UserNotificationEvent paymentCompletedTenant(
            Long tenantUserId,
            Long assetId,
            java.math.BigDecimal amount) {
        return build(
            NotificationType.PAYMENT_COMPLETED_TENANT,
            tenantUserId,
            Map.of("assetId", assetId, "amount", amount)
        );
    }

    // ============================================
    // SYSTEM NOTIFICATIONS
    // ============================================

    /**
     * Creates system-wide announcement notification.
     * Sent to all users (broadcast).
     */
    public UserNotificationEvent systemAnnouncement(String body) {
        return UserNotificationEvent.builder()
            .userId(null) // null = broadcast to all
            .title(NotificationType.SYSTEM_ANNOUNCEMENT.getTitle())
            .body(body)
            .type(NotificationType.SYSTEM_ANNOUNCEMENT.name())
            .broadcast(true)
            .metadata(new HashMap<>()) // System notifications need no specific metadata
            .build();
    }

    // ============================================
    // INTERNAL BUILD METHOD
    // ============================================

    /**
     * Internal method that builds the event with validation.
     *
     * @param type The notification type
     * @param userId The target user ID (null for broadcasts)
     * @param metadata The notification metadata
     * @return Built UserNotificationEvent
     * @throws IllegalArgumentException if metadata validation fails
     */
    private UserNotificationEvent build(
            NotificationType type,
            Long userId,
            Map<String, Object> metadata) {

        // Validate metadata has all required fields
        type.validateMetadata(metadata);

        return UserNotificationEvent.builder()
            .userId(userId)
            .title(type.getTitle())
            .body(type.getDefaultBody())
            .type(type.name())
            .broadcast(false)
            .metadata(metadata)
            .build();
    }
}

package com.rms.admin.service.messagePublisher.userNotification;

import java.util.Set;

/**
 * Defines all notification types with their metadata requirements.
 * Each type specifies:
 * - Display title & body
 * - Required metadata fields
 * - Category for frontend grouping
 */
public enum NotificationType {
    // ============================================
    // TENANCY REQUEST NOTIFICATIONS
    // ============================================
    TENANCY_REQUEST_RECEIVED(
        "New Tenancy Request",
        "A new tenancy request has been received for your property",
        Set.of("assetId", "requesterId")
    ),
    TENANCY_REQUEST_APPROVED(
        "Tenancy Request Approved",
        "Your tenancy request has been approved",
        Set.of("assetId")
    ),
    TENANCY_REQUEST_REJECTED(
        "Tenancy Request Rejected",
        "Your tenancy request has been rejected",
        Set.of("assetId")
    ),
    TENANCY_REQUEST_CANCELLED(
        "Tenancy Request Cancelled",
        "A tenancy request has been cancelled",
        Set.of("assetId")
    ),

    // ============================================
    // INVITATION NOTIFICATIONS
    // ============================================
    INVITATION_RECEIVED(
        "Rental Invitation",
        "You have received a rental invitation",
        Set.of("assetId", "ownerId")
    ),
    INVITATION_ACCEPTED(
        "Invitation Accepted",
        "Your rental invitation has been accepted",
        Set.of("assetId", "tenantUserId")
    ),
    INVITATION_REJECTED(
        "Invitation Rejected",
        "Your rental invitation has been rejected",
        Set.of("assetId", "tenantUserId")
    ),
    INVITATION_EXPIRED(
        "Invitation Expired",
        "Your rental invitation has expired",
        Set.of("assetId", "tenantUserId")
    ),

    // ============================================
    // TENANT/ASSET NOTIFICATIONS
    // ============================================
    TENANT_REMOVED(
        "Tenant Removed",
        "A tenant has been removed from your property",
        Set.of("assetId", "tenantUserId")
    ),
    ASSET_UPDATED(
        "Property Updated",
        "A property has been updated",
        Set.of("assetId")
    ),
    ASSET_DELETED(
        "Property Deleted",
        "A property has been deleted",
        Set.of("assetId")
    ),

    // ============================================
    // MESSAGING NOTIFICATIONS
    // ============================================
    NEW_MESSAGE(
        "New Message",
        "You have a new message",
        Set.of("conversationId")
    ),

    // ============================================
    // PAYMENT NOTIFICATIONS
    // ============================================
    PAYMENT_RECEIVED(
        "Payment Received",
        "A tenant has made a payment for your property",
        Set.of("assetId", "tenantUserId", "amount")
    ),
    PAYMENT_COMPLETED_TENANT(
        "Payment Successful",
        "Your payment has been processed successfully",
        Set.of("assetId", "amount")
    ),

    // ============================================
    // SYSTEM NOTIFICATIONS
    // ============================================
    SYSTEM_ANNOUNCEMENT(
        "System Announcement",
        "An important system announcement",
        Set.of() // System notifications have no required metadata
    );

    // ============================================
    // FIELDS
    // ============================================
    private final String title;
    private final String defaultBody;
    private final Set<String> requiredMetadataFields;

    // ============================================
    // CONSTRUCTOR
    // ============================================
    NotificationType(String title, String defaultBody, Set<String> requiredMetadataFields) {
        this.title = title;
        this.defaultBody = defaultBody;
        this.requiredMetadataFields = requiredMetadataFields;
    }

    // ============================================
    // GETTERS
    // ============================================
    public String getTitle() {
        return title;
    }

    public String getDefaultBody() {
        return defaultBody;
    }

    public Set<String> getRequiredMetadataFields() {
        return requiredMetadataFields;
    }

    /**
     * Validates that all required metadata fields are present.
     * @throws IllegalArgumentException if metadata is missing required fields
     */
    public void validateMetadata(java.util.Map<String, Object> metadata) {
        if (metadata == null) {
            if (!requiredMetadataFields.isEmpty()) {
                throw new IllegalArgumentException(
                    String.format("%s requires metadata: %s", this.name(), requiredMetadataFields)
                );
            }
            return;
        }

        for (String field : requiredMetadataFields) {
            if (!metadata.containsKey(field)) {
                throw new IllegalArgumentException(
                    String.format(
                        "Notification type %s missing required metadata field: %s",
                        this.name(), field
                    )
                );
            }
        }
    }
}

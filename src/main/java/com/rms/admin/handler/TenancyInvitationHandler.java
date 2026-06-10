package com.rms.admin.handler;

import com.rms.admin.data.dao.interfaces.IAssetDao;
import com.rms.admin.data.dto.asset.AssetCapacityResult;
import com.rms.admin.data.dto.asset.TenancyInvitationListItemResult;
import com.rms.admin.data.dto.asset.TenancyInvitationResponse;
import com.rms.admin.exception.BadRequestException;
import com.rms.admin.exception.ConflictException;
import com.rms.admin.exception.NotFoundException;
import com.rms.admin.service.messagePublisher.userNotification.UserNotificationEventFactory;
import com.rms.admin.service.messagePublisher.userNotification.UserNotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenancyInvitationHandler {

    private static final String ASSET_NOT_FOUND = "ASSET_NOT_FOUND";
    private static final String CAPACITY_FULL = "CAPACITY_FULL";
    private static final String ALREADY_TENANT = "ALREADY_TENANT";
    private static final String ALREADY_INVITED = "ALREADY_INVITED";
    private static final String INVITATION_NOT_FOUND = "INVITATION_NOT_FOUND";

    private final IAssetDao assetDao;
    private final UserNotificationPublisher notificationPublisher;
    private final UserNotificationEventFactory notificationFactory;

    @Transactional
    public void createInvitation(Long assetId, Long tenantUserId, Long ownerId) {
        if (!assetDao.existsByAssetIdAndOwnerId(assetId, ownerId)) {
            throw new NotFoundException(ASSET_NOT_FOUND);
        }
        AssetCapacityResult cap = assetDao.getCapacityAndCurrentTenantCount(assetId);
        if (cap == null) {
            throw new NotFoundException(ASSET_NOT_FOUND);
        }
        if (cap.getCurrentTenantCount() >= cap.getCapacity()) {
            throw new BadRequestException(CAPACITY_FULL, "Asset has no available capacity");
        }
        if (assetDao.isUserTenantOfAnyAsset(tenantUserId)) {
            throw new BadRequestException(ALREADY_TENANT, "User is already a tenant of an asset");
        }
        if (assetDao.hasInvitation(tenantUserId, assetId)) {
            throw new ConflictException(ALREADY_INVITED, "User already has a pending invitation for this asset");
        }
        assetDao.insertTenancyInvitation(tenantUserId, assetId);
        notificationPublisher.publish(
            notificationFactory.invitationReceived(tenantUserId, assetId, ownerId)
        );
    }

    @Transactional(readOnly = true)
    public List<TenancyInvitationResponse> listInvitationsForTenant(Long tenantUserId) {
        List<TenancyInvitationListItemResult> results = assetDao.findInvitationsByTenantUserId(tenantUserId);
        return results.stream()
                .map(r -> TenancyInvitationResponse.builder()
                        .assetId(r.getAssetId())
                        .assetTitle(r.getAssetTitle())
                        .createdAt(r.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional
    public void acceptInvitation(Long assetId, Long tenantUserId) {
        if (!assetDao.hasInvitation(tenantUserId, assetId)) {
            throw new NotFoundException(INVITATION_NOT_FOUND);
        }
        if (assetDao.isUserTenantOfAnyAsset(tenantUserId)) {
            throw new BadRequestException(ALREADY_TENANT, "You are already a tenant of an asset");
        }
        AssetCapacityResult cap = assetDao.getCapacityAndCurrentTenantCount(assetId);
        if (cap == null) {
            throw new NotFoundException(ASSET_NOT_FOUND);
        }
        if (cap.getCurrentTenantCount() >= cap.getCapacity()) {
            throw new BadRequestException(CAPACITY_FULL, "Asset has no available capacity");
        }
        assetDao.insertTenantMapping(tenantUserId, assetId);
        assetDao.deleteTenancyRequest(tenantUserId, assetId);
        Long ownerId = assetDao.getOwnerIdByAssetId(assetId);
        if (ownerId != null) {
            notificationPublisher.publish(
                notificationFactory.invitationAccepted(ownerId, assetId, tenantUserId)
            );
        }
    }

    @Transactional
    public void rejectInvitation(Long assetId, Long tenantUserId) {
        if (!assetDao.hasInvitation(tenantUserId, assetId)) {
            throw new NotFoundException(INVITATION_NOT_FOUND);
        }
        assetDao.deleteTenancyRequest(tenantUserId, assetId);
        Long ownerId = assetDao.getOwnerIdByAssetId(assetId);
        if (ownerId != null) {
            notificationPublisher.publish(
                notificationFactory.invitationRejected(ownerId, assetId, tenantUserId)
            );
        }
    }
}

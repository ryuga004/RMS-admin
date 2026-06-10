package com.rms.admin.handler;

import com.rms.admin.data.dao.interfaces.IAssetDao;
import com.rms.admin.data.dto.asset.AssetCapacityResult;
import com.rms.admin.data.dto.asset.TenancyRequestListItemResult;
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
public class AssetTenancyRequestHandler {

    private static final String ASSET_NOT_FOUND = "ASSET_NOT_FOUND";
    private static final String CAPACITY_FULL = "CAPACITY_FULL";
    private static final String ALREADY_TENANT = "ALREADY_TENANT";
    private static final String ALREADY_REQUESTED = "ALREADY_REQUESTED";
    private static final String TENANCY_REQUEST_NOT_FOUND = "TENANCY_REQUEST_NOT_FOUND";

    private final IAssetDao assetDao;
    private final UserNotificationPublisher notificationPublisher;
    private final UserNotificationEventFactory notificationFactory;

    @Transactional
    public void approve(Long assetId, Long requestUserId, Long ownerId) {
        if (!assetDao.existsByAssetIdAndOwnerId(assetId, ownerId)) {
            throw new NotFoundException(ASSET_NOT_FOUND);
        }
        if (!assetDao.hasTenancyRequest(requestUserId, assetId)) {
            throw new NotFoundException(TENANCY_REQUEST_NOT_FOUND);
        }
        if (assetDao.isUserTenantOfAnyAsset(requestUserId)) {
            throw new BadRequestException(ALREADY_TENANT, "Requester is already a tenant of another asset");
        }
        AssetCapacityResult cap = assetDao.getCapacityAndCurrentTenantCount(assetId);
        if (cap == null) {
            throw new NotFoundException(ASSET_NOT_FOUND);
        }
        if (cap.getCurrentTenantCount() >= cap.getCapacity()) {
            throw new BadRequestException(CAPACITY_FULL, "Asset has no available capacity");
        }
        assetDao.insertTenantMapping(requestUserId, assetId);
        assetDao.deleteTenancyRequest(requestUserId, assetId);
        notificationPublisher.publish(
            notificationFactory.tenancyRequestApproved(requestUserId, assetId)
        );
    }

    @Transactional
    public void reject(Long assetId, Long requestUserId, Long ownerId) {
        if (!assetDao.existsByAssetIdAndOwnerId(assetId, ownerId)) {
            throw new NotFoundException(ASSET_NOT_FOUND);
        }
        if (!assetDao.hasTenancyRequest(requestUserId, assetId)) {
            throw new NotFoundException(TENANCY_REQUEST_NOT_FOUND);
        }
        assetDao.deleteTenancyRequest(requestUserId, assetId);
        notificationPublisher.publish(
            notificationFactory.tenancyRequestRejected(requestUserId, assetId)
        );
    }

    @Transactional
    public void createTenancyRequest(Long assetId, Long requesterUserId) {
        AssetCapacityResult cap = assetDao.getCapacityAndCurrentTenantCount(assetId);
        if (cap == null) {
            throw new NotFoundException(ASSET_NOT_FOUND);
        }
        if (cap.getCurrentTenantCount() >= cap.getCapacity()) {
            throw new BadRequestException(CAPACITY_FULL, "Asset has no available capacity");
        }
        if (assetDao.isUserTenantOfAnyAsset(requesterUserId)) {
            throw new BadRequestException(ALREADY_TENANT, "You are already a tenant of an asset");
        }
        if (assetDao.hasTenancyRequest(requesterUserId, assetId)) {
            throw new ConflictException(ALREADY_REQUESTED, "You have already requested tenancy for this asset");
        }
        assetDao.insertTenancyRequest(requesterUserId, assetId);
        Long ownerId = assetDao.getOwnerIdByAssetId(assetId);
        if (ownerId != null) {
            notificationPublisher.publish(
                notificationFactory.tenancyRequestReceived(ownerId, assetId, requesterUserId)
            );
        }
    }

    @Transactional(readOnly = true)
    public List<TenancyRequestListItemResult> listSentRequests(Long userId) {
        return assetDao.findRequestsByRequesterUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<TenancyRequestListItemResult> listIncomingRequests(Long ownerId) {
        return assetDao.findRequestsForOwner(ownerId);
    }
}

package com.rms.admin.data.dao.interfaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rms.admin.data.dto.asset.AssetCapacityResult;
import com.rms.admin.data.dto.asset.AssetDetailResponse;
import com.rms.admin.data.dto.asset.AssetListItemResult;
import com.rms.admin.data.dto.asset.TenancyInvitationListItemResult;
import com.rms.admin.data.dto.asset.TenancyRequestListItemResult;
import com.rms.admin.data.dto.asset.TenantListItemResult;
import com.rms.admin.data.dto.users.ImageIdKey;

import java.math.BigDecimal;
import java.util.List;

public interface IAssetDao {
    List<AssetListItemResult> findAllByOwnerId(Long ownerId, int page, int limit, String searchText);
    long countByOwnerId(Long ownerId, String searchText);
    List<AssetListItemResult> findAll(int page, int limit, String searchText, List<Long> categoryIds, List<Long> adminIds, String sortBy, String sortDirection);
    long countAll(String searchText, List<Long> categoryIds, List<Long> adminIds);
    AssetDetailResponse findById(Long id);
    Long insert(String title, String description, Long categoryId, Long capacity, BigDecimal rent, List<String> tags, Long ownerId, Long addressId) throws JsonProcessingException;
    void update(Long id, String title, String description, Long categoryId, Long capacity, BigDecimal rent, List<String> tags) throws JsonProcessingException;

    void insertImageMapping(Long assetId, Long imageId);
    Long getAddressIdByAssetIdAndOwner(Long assetId, Long userId);
    List<ImageIdKey> getImageIdKeysByAssetId(Long assetId);
    void deleteImageMappingsByAssetId(Long assetId);
    void deleteById(Long assetId);

    AssetCapacityResult getCapacityAndCurrentTenantCount(Long assetId);
    boolean isUserTenantOfAnyAsset(Long userId);
    boolean hasTenancyRequest(Long userId, Long assetId);
    void insertTenancyRequest(Long userId, Long assetId);

    boolean hasInvitation(Long tenantUserId, Long assetId);
    void insertTenancyInvitation(Long tenantUserId, Long assetId);
    List<TenancyInvitationListItemResult> findInvitationsByTenantUserId(Long tenantUserId);
    List<TenancyRequestListItemResult> findRequestsByRequesterUserId(Long userId);
    List<TenancyRequestListItemResult> findRequestsForOwner(Long ownerId);

    boolean hasExitRequest(Long userId, Long assetId);
    void insertExitRequest(Long userId, Long assetId);

    boolean existsByAssetIdAndOwnerId(Long assetId, Long ownerId);

    Long getOwnerIdByAssetId(Long assetId);
    void deleteTenancyRequest(Long userId, Long assetId);
    void insertTenantMapping(Long userId, Long assetId);
    void removeTenant(Long userId, Long assetId);
    List<TenantListItemResult> findTenants(Long ownerId, int page, int limit, String searchText, List<Long> assetIds);
    long countTenants(Long ownerId, String searchText, List<Long> assetIds);
    List<AssetListItemResult> findAssetsByTenantUserId(Long userId);
}
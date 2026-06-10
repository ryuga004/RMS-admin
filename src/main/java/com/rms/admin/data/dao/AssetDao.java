package com.rms.admin.data.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rms.admin.data.dao.interfaces.IAssetDao;
import com.rms.admin.data.dto.asset.AssetCapacityResult;
import com.rms.admin.data.dto.asset.AssetDetailResponse;
import com.rms.admin.data.dto.asset.AssetListItemResult;
import com.rms.admin.data.dto.asset.TenancyInvitationListItemResult;
import com.rms.admin.data.dto.asset.TenancyRequestListItemResult;
import com.rms.admin.data.dto.asset.TenantListItemResult;
import com.rms.admin.data.dto.users.ImageIdKey;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

import static com.rms.admin.persistence.tables.Address.ADDRESS;
import static com.rms.admin.persistence.tables.Asset.ASSET;
import static com.rms.admin.persistence.tables.AssetImageMapping.ASSET_IMAGE_MAPPING;
import static com.rms.admin.persistence.tables.AssetTenantMapping.ASSET_TENANT_MAPPING;
import com.rms.admin.utils.constants.AssetTenantRequestType;

import static com.rms.admin.persistence.tables.AssetTenantRequest.ASSET_TENANT_REQUEST;
import static com.rms.admin.persistence.tables.Category.CATEGORY;
import static com.rms.admin.persistence.tables.Images.IMAGES;
import static com.rms.admin.persistence.tables.Users.USERS;

@Repository
@RequiredArgsConstructor
public class AssetDao implements IAssetDao {

    private final DSLContext dsl;
    private final ObjectMapper objectMapper;

    @Override
    public List<AssetListItemResult> findAllByOwnerId(Long ownerId, int page, int limit, String searchText) {
        Condition searchCondition = buildSearchCondition(searchText);
        int offset = (page) * limit;
        return dsl.select(
                ASSET.ID,
                ASSET.TITLE,
                ASSET.RENT,
                ASSET.CAPACITY,
                CATEGORY.NAME.as("categoryName"),
                imageKeySubquery().limit(1).asField("imageKey"))
                .from(ASSET)
                .leftJoin(CATEGORY).on(ASSET.CATEGORY_ID.eq(CATEGORY.ID))
                .where(ASSET.OWNER_ID.eq(ownerId))
                .and(searchCondition)
                .orderBy(ASSET.ID)
                .limit(limit)
                .offset(offset)
                .fetchInto(AssetListItemResult.class);
    }

    @Override
    public List<AssetListItemResult> findAll(int page, int limit, String searchText, List<Long> categoryIds, List<Long> adminIds, String sortBy, String sortDirection) {
        Condition finalCondition = buildGlobalCondition(searchText, categoryIds, adminIds);
        int offset = (page) * limit;

        var query = dsl.select(
                ASSET.ID,
                ASSET.TITLE,
                ASSET.RENT,
                ASSET.CAPACITY,
                CATEGORY.NAME.as("categoryName"),
                imageKeySubquery().limit(1).asField("imageKey"))
                .from(ASSET)
                .leftJoin(CATEGORY).on(ASSET.CATEGORY_ID.eq(CATEGORY.ID))
                .where(finalCondition);

        // Sorting
        var field = sortBy != null && sortBy.equals("rent") ? ASSET.RENT : ASSET.ID;
        var orderField = sortDirection != null && sortDirection.equalsIgnoreCase("desc") ? field.desc() : field.asc();

        return query.orderBy(orderField)
                .limit(limit)
                .offset(offset)
                .fetchInto(AssetListItemResult.class);
    }

    @Override
    public long countAll(String searchText, List<Long> categoryIds, List<Long> adminIds) {
        Condition finalCondition = buildGlobalCondition(searchText, categoryIds, adminIds);
        return dsl.selectCount()
                .from(ASSET)
                .where(finalCondition)
                .fetchOneInto(Long.class);
    }

    private Condition buildGlobalCondition(String searchText, List<Long> categoryIds, List<Long> adminIds) {
        Condition searchCondition = buildSearchCondition(searchText);
        if (categoryIds != null && !categoryIds.isEmpty()) {
            searchCondition = searchCondition.and(ASSET.CATEGORY_ID.in(categoryIds));
        }
        if (adminIds != null && !adminIds.isEmpty()) {
            searchCondition = searchCondition.and(ASSET.OWNER_ID.in(adminIds));
        }
        return searchCondition;
    }

    @Override
    public AssetDetailResponse findById(Long id) {
        return dsl.select(
                ASSET.ID.as("id"),
                ASSET.TITLE.as("title"),
                ASSET.DESCRIPTION.as("description"),
                CATEGORY.NAME.as("categoryName"),
                ASSET.CAPACITY.as("capacity"),
                ASSET.RENT.as("rent"),
                ASSET.TAGS.as("tags"),
                ASSET.OWNER_ID.as("ownerId"),
                ASSET.CREATED_AT.as("createdAt"),
                DSL.multiset(imageKeySubquery()).convertFrom(r->r.into(String.class)).as("imageKeys"),
                ADDRESS.LOCAL_ADDRESS.as("AddressDetails.localAddress"),
                ADDRESS.CITY.as("AddressDetails.city"),
                ADDRESS.STATE.as("AddressDetails.state"),
                ADDRESS.COUNTRY.as("AddressDetails.country"))
                .from(ASSET)
                .leftJoin(CATEGORY).on(ASSET.CATEGORY_ID.eq(CATEGORY.ID))
                .leftJoin(ADDRESS).on(ASSET.LOCATION.eq(ADDRESS.ID))
                .where(ASSET.ID.eq(id))
                .fetchOneInto(AssetDetailResponse.class);
    }

    @Override
    public void update(Long id, String title, String description, Long categoryId, Long capacity, BigDecimal rent, List<String> tags) throws JsonProcessingException {
        JSONB tagsJson = JSONB.valueOf(objectMapper.writeValueAsString(tags));
        dsl.update(ASSET)
                .set(ASSET.TITLE, title)
                .set(ASSET.DESCRIPTION, description)
                .set(ASSET.CATEGORY_ID, categoryId)
                .set(ASSET.CAPACITY, capacity != null ? capacity : 0L)
                .set(ASSET.RENT, rent != null ? rent : BigDecimal.ZERO)
                .set(ASSET.TAGS, tagsJson)
                .where(ASSET.ID.eq(id))
                .execute();
    }

    @Override
    public Long insert(String title, String description, Long categoryId, Long capacity, BigDecimal rent,
            List<String> tags, Long ownerId, Long addressId) throws JsonProcessingException {
        JSONB tagsJson = JSONB.valueOf(objectMapper.writeValueAsString(tags));
        return dsl.insertInto(ASSET)
                .columns(
                        ASSET.TITLE,
                        ASSET.DESCRIPTION,
                        ASSET.CATEGORY_ID,
                        ASSET.CAPACITY,
                        ASSET.RENT,
                        ASSET.TAGS,
                        ASSET.OWNER_ID,
                        ASSET.LOCATION)
                .values(
                        title,
                        description,
                        categoryId,
                        capacity != null ? capacity : 0L,
                        rent != null ? rent : BigDecimal.ZERO,
                        tagsJson,
                        ownerId,
                        addressId)
                .returningResult(ASSET.ID)
                .fetchOneInto(Long.class);
    }

    @Override
    public void insertImageMapping(Long assetId, Long imageId) {
        dsl.insertInto(ASSET_IMAGE_MAPPING)
                .columns(ASSET_IMAGE_MAPPING.ASSET_ID, ASSET_IMAGE_MAPPING.IMAGE_ID)
                .values(assetId, imageId)
                .execute();
    }

    @Override
    public Long getAddressIdByAssetIdAndOwner(Long assetId, Long userId) {
        var row = dsl.select(ASSET.LOCATION)
                .from(ASSET)
                .where(ASSET.ID.eq(assetId).and(ASSET.OWNER_ID.eq(userId)))
                .fetchOne();
        return row != null ? row.get(ASSET.LOCATION) : null;
    }

    @Override
    public List<ImageIdKey> getImageIdKeysByAssetId(Long assetId) {
        return dsl.select(IMAGES.ID.as("imageId"), IMAGES.KEY.as("objectKey"))
                .from(ASSET_IMAGE_MAPPING)
                .join(IMAGES).on(ASSET_IMAGE_MAPPING.IMAGE_ID.eq(IMAGES.ID))
                .where(ASSET_IMAGE_MAPPING.ASSET_ID.eq(assetId))
                .fetchInto(ImageIdKey.class);
    }

    @Override
    public void deleteImageMappingsByAssetId(Long assetId) {
        dsl.deleteFrom(ASSET_IMAGE_MAPPING).where(ASSET_IMAGE_MAPPING.ASSET_ID.eq(assetId)).execute();
    }

    @Override
    public void deleteById(Long assetId) {
        dsl.deleteFrom(ASSET).where(ASSET.ID.eq(assetId)).execute();
    }

    @Override
    @SuppressWarnings("all")
    public long countByOwnerId(Long ownerId, String searchText) {
        Condition searchCondition = buildSearchCondition(searchText);
        return dsl.selectCount()
                .from(ASSET)
                .where(ASSET.OWNER_ID.eq(ownerId))
                .and(searchCondition)
                .fetchOneInto(Long.class);
    }

    @Override
    public AssetCapacityResult getCapacityAndCurrentTenantCount(Long assetId) {
        return dsl.select(
                ASSET.CAPACITY.as("capacity"),
                DSL.coalesce(DSL.count(ASSET_TENANT_MAPPING.USER_ID), 0L).as("currentTenantCount"))
                .from(ASSET)
                .leftJoin(ASSET_TENANT_MAPPING).on(ASSET.ID.eq(ASSET_TENANT_MAPPING.ASSET_ID))
                .where(ASSET.ID.eq(assetId))
                .groupBy(ASSET.ID, ASSET.CAPACITY)
                .fetchOneInto(AssetCapacityResult.class);
    }

    @Override
    public boolean isUserTenantOfAnyAsset(Long userId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(ASSET_TENANT_MAPPING)
                        .where(ASSET_TENANT_MAPPING.USER_ID.eq(userId)));
    }

    @Override
    public boolean hasTenancyRequest(Long userId, Long assetId) {
        return hasRequestOfType(userId, assetId, AssetTenantRequestType.REQUEST);
    }

    @Override
    public void insertTenancyRequest(Long userId, Long assetId) {
        insertRequest(userId, assetId, AssetTenantRequestType.REQUEST);
    }

    @Override
    public boolean hasInvitation(Long tenantUserId, Long assetId) {
        return hasRequestOfType(tenantUserId, assetId, AssetTenantRequestType.INVITATION);
    }

    @Override
    public void insertTenancyInvitation(Long tenantUserId, Long assetId) {
        insertRequest(tenantUserId, assetId, AssetTenantRequestType.INVITATION);
    }

    @Override
    public boolean hasExitRequest(Long userId, Long assetId) {
        return hasRequestOfType(userId, assetId, AssetTenantRequestType.EXIT);
    }

    @Override
    public void insertExitRequest(Long userId, Long assetId) {
        insertRequest(userId, assetId, AssetTenantRequestType.EXIT);
    }

    private boolean hasRequestOfType(Long userId, Long assetId, AssetTenantRequestType type) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(ASSET_TENANT_REQUEST)
                        .where(ASSET_TENANT_REQUEST.USER_ID.eq(userId)
                                .and(ASSET_TENANT_REQUEST.ASSET_ID.eq(assetId))
                                .and(ASSET_TENANT_REQUEST.TYPE.eq(type.name()))));
    }

    private void insertRequest(Long userId, Long assetId, AssetTenantRequestType type) {
        dsl.insertInto(ASSET_TENANT_REQUEST)
                .columns(ASSET_TENANT_REQUEST.USER_ID, ASSET_TENANT_REQUEST.ASSET_ID, ASSET_TENANT_REQUEST.TYPE)
                .values(userId, assetId, type.name())
                .execute();
    }

    @Override
    public List<TenancyInvitationListItemResult> findInvitationsByTenantUserId(Long tenantUserId) {
        return dsl.select(
                ASSET_TENANT_REQUEST.ASSET_ID.as("assetId"),
                ASSET.TITLE.as("assetTitle"),
                ASSET_TENANT_REQUEST.CREATED_AT.as("createdAt"))
                .from(ASSET_TENANT_REQUEST)
                .join(ASSET).on(ASSET_TENANT_REQUEST.ASSET_ID.eq(ASSET.ID))
                .where(ASSET_TENANT_REQUEST.USER_ID.eq(tenantUserId))
                .and(ASSET_TENANT_REQUEST.TYPE.eq(AssetTenantRequestType.INVITATION.name()))
                .orderBy(ASSET_TENANT_REQUEST.CREATED_AT.desc())
                .fetchInto(TenancyInvitationListItemResult.class);
    }

    @Override
    public boolean existsByAssetIdAndOwnerId(Long assetId, Long ownerId) {
        return dsl.fetchExists(ASSET,ASSET.ID.eq(assetId).and(ASSET.OWNER_ID.eq(ownerId)));
    }

    @Override
    public Long getOwnerIdByAssetId(Long assetId) {
        return dsl.select(ASSET.OWNER_ID)
                .from(ASSET)
                .where(ASSET.ID.eq(assetId))
                .fetchOneInto(Long.class);
    }

    @Override
    public List<TenancyRequestListItemResult> findRequestsByRequesterUserId(Long userId) {
        return dsl.select(
                ASSET_TENANT_REQUEST.ASSET_ID.as("assetId"),
                ASSET.TITLE.as("assetTitle"),
                ASSET_TENANT_REQUEST.USER_ID.as("requesterUserId"),
                USERS.NAME.as("requesterName"),
                ASSET_TENANT_REQUEST.CREATED_AT.as("createdAt"))
                .from(ASSET_TENANT_REQUEST)
                .join(ASSET).on(ASSET_TENANT_REQUEST.ASSET_ID.eq(ASSET.ID))
                .join(USERS).on(ASSET_TENANT_REQUEST.USER_ID.eq(USERS.ID))
                .where(ASSET_TENANT_REQUEST.USER_ID.eq(userId))
                .and(ASSET_TENANT_REQUEST.TYPE.eq(AssetTenantRequestType.REQUEST.name()))
                .orderBy(ASSET_TENANT_REQUEST.CREATED_AT.desc())
                .fetchInto(TenancyRequestListItemResult.class);
    }

    @Override
    public List<TenancyRequestListItemResult> findRequestsForOwner(Long ownerId) {
        return dsl.select(
                ASSET_TENANT_REQUEST.ASSET_ID.as("assetId"),
                ASSET.TITLE.as("assetTitle"),
                ASSET_TENANT_REQUEST.USER_ID.as("requesterUserId"),
                USERS.NAME.as("requesterName"),
                ASSET_TENANT_REQUEST.CREATED_AT.as("createdAt"))
                .from(ASSET_TENANT_REQUEST)
                .join(ASSET).on(ASSET_TENANT_REQUEST.ASSET_ID.eq(ASSET.ID))
                .join(USERS).on(ASSET_TENANT_REQUEST.USER_ID.eq(USERS.ID))
                .where(ASSET.OWNER_ID.eq(ownerId))
                .and(ASSET_TENANT_REQUEST.TYPE.eq(AssetTenantRequestType.REQUEST.name()))
                .orderBy(ASSET_TENANT_REQUEST.CREATED_AT.desc())
                .fetchInto(TenancyRequestListItemResult.class);
    }

    @Override
    public void deleteTenancyRequest(Long userId, Long assetId) {
        dsl.deleteFrom(ASSET_TENANT_REQUEST)
                .where(ASSET_TENANT_REQUEST.USER_ID.eq(userId).and(ASSET_TENANT_REQUEST.ASSET_ID.eq(assetId)))
                .execute();
    }

    @Override
    public void insertTenantMapping(Long userId, Long assetId) {
        dsl.insertInto(ASSET_TENANT_MAPPING)
                .columns(ASSET_TENANT_MAPPING.USER_ID, ASSET_TENANT_MAPPING.ASSET_ID)
                .values(userId, assetId)
                .execute();
    }

    @Override
    public void removeTenant(Long userId, Long assetId) {
        dsl.deleteFrom(ASSET_TENANT_MAPPING)
                .where(ASSET_TENANT_MAPPING.USER_ID.eq(userId).and(ASSET_TENANT_MAPPING.ASSET_ID.eq(assetId)))
                .execute();
    }

    @Override
    public List<TenantListItemResult> findTenants(Long ownerId, int page, int limit, String searchText, List<Long> assetIds) {
        int offset = page * limit;
        Condition condition = ASSET.OWNER_ID.eq(ownerId);
        if (assetIds != null && !assetIds.isEmpty()) {
            condition = condition.and(ASSET.ID.in(assetIds));
        }
        if (searchText != null && !searchText.isBlank()) {
            String pattern = "%" + searchText.trim() + "%";
            condition = condition.and(USERS.NAME.likeIgnoreCase(pattern).or(ASSET.TITLE.likeIgnoreCase(pattern)));
        }

        return dsl.select(
                ASSET_TENANT_MAPPING.USER_ID.as("tenantUserId"),
                USERS.NAME.as("tenantName"),
                USERS.EMAIL.as("tenantEmail"),
                ASSET.ID.as("assetId"),
                ASSET.TITLE.as("assetTitle"))
                .from(ASSET_TENANT_MAPPING)
                .join(ASSET).on(ASSET_TENANT_MAPPING.ASSET_ID.eq(ASSET.ID))
                .join(USERS).on(ASSET_TENANT_MAPPING.USER_ID.eq(USERS.ID))
                .where(condition)
                .limit(limit)
                .offset(offset)
                .fetchInto(TenantListItemResult.class);
    }

    @Override
    public long countTenants(Long ownerId, String searchText, List<Long> assetIds) {
        Condition condition = ASSET.OWNER_ID.eq(ownerId);
        if (assetIds != null && !assetIds.isEmpty()) {
            condition = condition.and(ASSET.ID.in(assetIds));
        }
        if (searchText != null && !searchText.isBlank()) {
            String pattern = "%" + searchText.trim() + "%";
            condition = condition.and(USERS.NAME.likeIgnoreCase(pattern).or(ASSET.TITLE.likeIgnoreCase(pattern)));
        }

        return dsl.selectCount()
                .from(ASSET_TENANT_MAPPING)
                .join(ASSET).on(ASSET_TENANT_MAPPING.ASSET_ID.eq(ASSET.ID))
                .join(USERS).on(ASSET_TENANT_MAPPING.USER_ID.eq(USERS.ID))
                .where(condition)
                .fetchOneInto(Long.class);
    }

    @Override
    public List<AssetListItemResult> findAssetsByTenantUserId(Long userId) {
        return dsl.select(
                ASSET.ID,
                ASSET.TITLE,
                ASSET.RENT,
                ASSET.CAPACITY,
                CATEGORY.NAME.as("categoryName"),
                imageKeySubquery().limit(1).asField("imageKey"))
                .from(ASSET)
                .join(ASSET_TENANT_MAPPING).on(ASSET.ID.eq(ASSET_TENANT_MAPPING.ASSET_ID))
                .leftJoin(CATEGORY).on(ASSET.CATEGORY_ID.eq(CATEGORY.ID))
                .where(ASSET_TENANT_MAPPING.USER_ID.eq(userId))
                .fetchInto(AssetListItemResult.class);
    }

    private static SelectConditionStep<?> imageKeySubquery() {
        return DSL.select(IMAGES.KEY)
                .from(ASSET_IMAGE_MAPPING)
                .join(IMAGES).on(ASSET_IMAGE_MAPPING.IMAGE_ID.eq(IMAGES.ID))
                .where(ASSET_IMAGE_MAPPING.ASSET_ID.eq(ASSET.ID));
    }

    private static Condition buildSearchCondition(String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return DSL.trueCondition();
        }
        String pattern = "%" + searchText.trim() + "%";
        return ASSET.TITLE.likeIgnoreCase(pattern);
    }
}

package com.rms.admin.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rms.admin.config.StorageProperties;
import com.rms.admin.data.dao.interfaces.IAddressDao;
import com.rms.admin.data.dao.interfaces.IAssetDao;
import com.rms.admin.data.dao.interfaces.ICategoryDao;
import com.rms.admin.data.dao.interfaces.IImageDao;
import com.rms.admin.data.dto.PaginationResponse;
import com.rms.admin.data.dto.users.ImageIdKey;
import com.rms.admin.data.dto.asset.AddressDetails;
import com.rms.admin.data.dto.asset.AssetDetailResponse;
import com.rms.admin.data.dto.asset.AssetListItemResult;
import com.rms.admin.data.dto.asset.CreateAssetRequest;
import com.rms.admin.data.dto.asset.TenantListItemResult;
import com.rms.admin.data.dto.asset.UpdateAssetRequest;
import com.rms.admin.exception.BadRequestException;
import com.rms.admin.service.storage.StorageService;
import com.rms.admin.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.rms.admin.service.storage.S3StorageService.buildAssetImageKey;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetHandler {

    private static final String ASSET_NOT_FOUND = "ASSET_NOT_FOUND";
    private static final String CATEGORY_NOT_FOUND = "CATEGORY_NOT_FOUND";

    private final IAssetDao assetDao;
    private final IAddressDao addressDao;
    private final ICategoryDao categoryDao;
    private final IImageDao imageDao;
    private final StorageService storageService;
    private final StorageProperties storageProperties;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public PaginationResponse getAllAssets(Long ownerId, int page, int limit, String searchText) {
        List<AssetListItemResult> results = assetDao.findAllByOwnerId(ownerId, page, limit, searchText);
        long totalCount = assetDao.countByOwnerId(ownerId, searchText);
        processImageUrls(results);
        return PaginationResponse.builder().result(results).totalCount(totalCount).build();
    }

    @Transactional(readOnly = true)
    public PaginationResponse getAllAssetsGlobal(int page, int limit, String searchText, List<Long> categoryIds, List<Long> adminIds, String sortBy, String sortDirection) {
        List<AssetListItemResult> results = assetDao.findAll(page, limit, searchText, categoryIds, adminIds, sortBy, sortDirection);
        long totalCount = assetDao.countAll(searchText, categoryIds, adminIds);
        processImageUrls(results);
        return PaginationResponse.builder().result(results).totalCount(totalCount).build();
    }

    private void processImageUrls(List<AssetListItemResult> results) {
        results.forEach(result -> {
            if(result.getImageKey()!=null) {
                result.setImageUrl(storageService.generatePresignedUrl(result.getImageKey(),
                        storageProperties.getPresignedExpiryMinutes()));
            }
        });
    }

    @Transactional(readOnly = true)
    public AssetDetailResponse getAssetById(Long id) {
        AssetDetailResponse result = assetDao.findById(id);
        if (result == null) {
            throw new NotFoundException(ASSET_NOT_FOUND);
        }
        List<String> imageUrls = Optional.ofNullable(result.getImageKeys())
                .orElse(List.of())
                .stream()
                .filter(Objects::nonNull)
                .map(imageKey -> storageService.generatePresignedUrl(imageKey, storageProperties.getPresignedExpiryMinutes()))
                .toList();
        result.setImageUrls(imageUrls);
        return result;
    }

    @Transactional
    public void updateAsset(Long assetId, UpdateAssetRequest request, List<MultipartFile> imageFiles, Long ownerId) {
        Long addressId = assetDao.getAddressIdByAssetIdAndOwner(assetId, ownerId);
        if (addressId == null) {
            throw new NotFoundException(ASSET_NOT_FOUND);
        }
        
        try {
            assetDao.update(
                    assetId,
                    request.getTitle(),
                    request.getDescription(),
                    request.getCategoryId(),
                    request.getCapacity(),
                    request.getRent(),
                    request.getTags());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        AddressDetails addr = request.getAddressDetails();
        com.rms.admin.data.dto.users.Address addressDto = new com.rms.admin.data.dto.users.Address();
        addressDto.setLocalAddress(addr.getLocalAddress());
        addressDto.setCity(addr.getCity());
        addressDto.setState(addr.getState());
        addressDto.setCountry(addr.getCountry());
        addressDao.update(addressId, addressDto);

        if (!CollectionUtils.isEmpty(imageFiles)) {
            for (MultipartFile file : imageFiles) {
                if (file == null || file.isEmpty()) continue;
                String objectKey = buildAssetImageKey(assetId);
                storageService.uploadFile(file, objectKey);
                Long imageId = imageDao.insert(objectKey);
                assetDao.insertImageMapping(assetId, imageId);
            }
        }
    }

    @Transactional
    public Long createAsset(CreateAssetRequest request, List<MultipartFile> imageFiles, Long ownerId)  {
        if (request.getCategoryId() != null && !categoryDao.existsById(request.getCategoryId())) {
            throw new BadRequestException(CATEGORY_NOT_FOUND, "Category does not exist");
        }
        AddressDetails addr = request.getAddressDetails();
        Long addressId = addressDao.insert(
                addr.getLocalAddress(),
                addr.getCity(),
                addr.getState(),
                addr.getCountry());
        Long capacity = request.getCapacity() != null ? request.getCapacity() : 0L;
        BigDecimal rent = request.getRent() != null ? request.getRent() : BigDecimal.ZERO;
        Long assetId = null;
        try {
            assetId = assetDao.insert(
                    request.getTitle(),
                    request.getDescription(),
                    request.getCategoryId(),
                    capacity,
                    rent,
                    request.getTags(),
                    ownerId,
                    addressId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (!CollectionUtils.isEmpty(imageFiles)) {
            for (MultipartFile file : imageFiles) {
                if (file == null || file.isEmpty()) continue;
                String objectKey = buildAssetImageKey(assetId);
                storageService.uploadFile(file, objectKey);
                Long imageId = imageDao.insert(objectKey);
                assetDao.insertImageMapping(assetId, imageId);
            }
        }
        return assetId;
    }

    @Transactional
    public void deleteAsset(Long assetId, Long userId) {
        Long addressId = assetDao.getAddressIdByAssetIdAndOwner(assetId, userId);
        if (addressId == null) {
            throw new NotFoundException(ASSET_NOT_FOUND);
        }
        List<ImageIdKey> imageIdKeys = assetDao.getImageIdKeysByAssetId(assetId);
        assetDao.deleteImageMappingsByAssetId(assetId);
        for (ImageIdKey imageIdKey : imageIdKeys) {
            storageService.deleteFile(imageIdKey.getObjectKey());
            imageDao.deleteById(imageIdKey.getImageId());
        }
        assetDao.deleteById(assetId);
        addressDao.deleteById(addressId);
    }

    @Transactional(readOnly = true)
    public PaginationResponse getTenants(Long ownerId, int page, int limit, String searchText, List<Long> assetIds) {
        List<TenantListItemResult> results = assetDao.findTenants(ownerId, page, limit, searchText, assetIds);
        long totalCount = assetDao.countTenants(ownerId, searchText, assetIds);
        return PaginationResponse.builder().result(results).totalCount(totalCount).build();
    }

    @Transactional
    public void removeTenant(Long userId, Long assetId, Long ownerId) {
        if (!assetDao.existsByAssetIdAndOwnerId(assetId, ownerId)) {
            throw new NotFoundException(ASSET_NOT_FOUND);
        }
        assetDao.removeTenant(userId, assetId);
    }

    @Transactional(readOnly = true)
    public List<AssetListItemResult> getMyRentals(Long userId) {
        List<AssetListItemResult> results = assetDao.findAssetsByTenantUserId(userId);
        processImageUrls(results);
        return results;
    }
}
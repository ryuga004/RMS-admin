package com.rms.admin.controller;

import com.rms.admin.data.dto.ApiResponse;
import com.rms.admin.data.dto.PaginationResponse;
import com.rms.admin.data.dto.asset.AssetDetailResponse;
import com.rms.admin.data.dto.asset.AssetListItemResult;
import com.rms.admin.data.dto.asset.CreateAssetRequest;
import com.rms.admin.data.dto.asset.UpdateAssetRequest;
import com.rms.admin.handler.AssetHandler;
import com.rms.admin.security.JwtPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetHandler assetHandler;

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaginationResponse>> getAllAssetsForAdmin(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(defaultValue = "10") @Min(0) int pageSize,
            @RequestParam(required = false) String searchText) {
        Long ownerId = principal.getUserId();
        PaginationResponse response = assetHandler.getAllAssets(ownerId, pageNumber, pageSize, searchText);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse>> getAllAssetsGlobal(
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(defaultValue = "10") @Min(0) int pageSize,
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> adminIds,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        PaginationResponse response = assetHandler.getAllAssetsGlobal(pageNumber, pageSize, searchText, categoryIds, adminIds, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetDetailResponse>> getAssetById(
            @PathVariable @NotNull Long id) {
        return ResponseEntity.ok(ApiResponse.success(assetHandler.getAssetById(id)));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<Long>> createAsset(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestPart("asset") @Valid CreateAssetRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        Long ownerId = principal.getUserId();
        Long assetId = assetHandler.createAsset(request, images, ownerId);
        URI location = URI.create("/assets/" + assetId);
        return ResponseEntity.created(location).body(ApiResponse.success(assetId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateAsset(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestPart("asset") @Valid UpdateAssetRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        Long ownerId = principal.getUserId();
        assetHandler.updateAsset(id, request, images, ownerId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable @NotNull Long id) {
        Long userId = principal.getUserId();
        assetHandler.deleteAsset(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tenants")
    public ResponseEntity<ApiResponse<PaginationResponse>> getTenants(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(defaultValue = "10") @Min(0) int pageSize,
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) List<Long> assetIds) {
        return ResponseEntity.ok(ApiResponse.success(assetHandler.getTenants(principal.getUserId(), pageNumber, pageSize, searchText, assetIds)));
    }

    @DeleteMapping("/{assetId}/tenants/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeTenant(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable @NotNull Long assetId,
            @PathVariable @NotNull Long userId) {
        assetHandler.removeTenant(userId, assetId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Tenant removed successfully"));
    }

    @GetMapping("/my-rentals")
    public ResponseEntity<ApiResponse<List<AssetListItemResult>>> getMyRentals(
            @AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(assetHandler.getMyRentals(principal.getUserId())));
    }
}
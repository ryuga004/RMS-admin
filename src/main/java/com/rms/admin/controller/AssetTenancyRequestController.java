package com.rms.admin.controller;

import com.rms.admin.data.dto.ApiResponse;
import com.rms.admin.data.dto.asset.CreateTenancyInvitationRequest;
import com.rms.admin.data.dto.asset.TenancyRequestListItemResult;
import com.rms.admin.handler.AssetTenancyRequestHandler;
import com.rms.admin.handler.TenancyInvitationHandler;
import com.rms.admin.security.JwtPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetTenancyRequestController {

    private final AssetTenancyRequestHandler assetTenancyRequestHandler;
    private final TenancyInvitationHandler tenancyInvitationHandler;

    @PostMapping("/{assetId}/tenancy-requests")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponse<Void>> createTenancyRequest(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable @NotNull Long assetId) {
        assetTenancyRequestHandler.createTenancyRequest(assetId, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null, "Tenancy request submitted successfully"));
    }

    @PostMapping("/{assetId}/tenancy-requests/{userId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveTenancyRequest(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable @NotNull Long assetId,
            @PathVariable @NotNull Long userId) {
        assetTenancyRequestHandler.approve(assetId, userId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Tenancy request approved"));
    }

    @PostMapping("/{assetId}/tenancy-requests/{userId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectTenancyRequest(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable @NotNull Long assetId,
            @PathVariable @NotNull Long userId) {
        assetTenancyRequestHandler.reject(assetId, userId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Tenancy request rejected"));
    }

    @PostMapping("/{assetId}/tenancy-invitations")
    public ResponseEntity<ApiResponse<Void>> createTenancyInvitation(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable @NotNull Long assetId,
            @RequestBody @Valid CreateTenancyInvitationRequest request) {
        tenancyInvitationHandler.createInvitation(assetId, request.getTenantUserId(), principal.getUserId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Invitation sent"));
    }

    @GetMapping("/tenancy-requests/sent")
    public ResponseEntity<ApiResponse<List<TenancyRequestListItemResult>>> listSentRequests(
            @AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                assetTenancyRequestHandler.listSentRequests(principal.getUserId()),
                "Sent requests listed"));
    }

    @GetMapping("/tenancy-requests/incoming")
    public ResponseEntity<ApiResponse<List<TenancyRequestListItemResult>>> listIncomingRequests(
            @AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                assetTenancyRequestHandler.listIncomingRequests(principal.getUserId()),
                "Incoming requests listed"));
    }
}

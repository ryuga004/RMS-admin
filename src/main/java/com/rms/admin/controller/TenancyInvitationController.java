package com.rms.admin.controller;

import com.rms.admin.data.dto.ApiResponse;
import com.rms.admin.data.dto.asset.TenancyInvitationResponse;
import com.rms.admin.handler.TenancyInvitationHandler;
import com.rms.admin.security.JwtPrincipal;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tenancy-invitations")
@RequiredArgsConstructor
public class TenancyInvitationController {

    private final TenancyInvitationHandler tenancyInvitationHandler;

    @GetMapping("")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponse<List<TenancyInvitationResponse>>> listMyInvitations(
            @AuthenticationPrincipal JwtPrincipal principal) {
        List<TenancyInvitationResponse> list = tenancyInvitationHandler.listInvitationsForTenant(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/{assetId}/accept")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponse<Void>> acceptInvitation(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable @NotNull Long assetId) {
        tenancyInvitationHandler.acceptInvitation(assetId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Invitation accepted"));
    }

    @PostMapping("/{assetId}/reject")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponse<Void>> rejectInvitation(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable @NotNull Long assetId) {
        tenancyInvitationHandler.rejectInvitation(assetId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Invitation rejected"));
    }
}

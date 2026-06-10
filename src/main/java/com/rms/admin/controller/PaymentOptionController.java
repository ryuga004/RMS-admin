package com.rms.admin.controller;

import com.rms.admin.data.dto.ApiResponse;
import com.rms.admin.data.dto.PaginationResponse;
import com.rms.admin.data.dto.payment.CreatePaymentOptionRequest;
import com.rms.admin.data.dto.payment.PaymentOptionResponse;
import com.rms.admin.data.dto.payment.UpdatePaymentOptionRequest;
import com.rms.admin.handler.PaymentOptionHandler;
import com.rms.admin.security.JwtPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/payment-options")
@RequiredArgsConstructor
public class PaymentOptionController {

    private final PaymentOptionHandler paymentOptionHandler;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> create(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody CreatePaymentOptionRequest request) {
        Long id = paymentOptionHandler.create(request, principal.getUserId());
        URI location = URI.create("/payment-options/" + id);
        return ResponseEntity.created(location).body(ApiResponse.success(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody UpdatePaymentOptionRequest request) {
        paymentOptionHandler.update(id, request, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtPrincipal principal) {
        paymentOptionHandler.delete(id, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaginationResponse>> getByOwner(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize) {
        PaginationResponse response = paymentOptionHandler.getByOwner(principal.getUserId(), pageNumber, pageSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT')")
    public ResponseEntity<ApiResponse<PaymentOptionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(paymentOptionHandler.getById(id)));
    }

    @GetMapping("/by-asset/{assetId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT')")
    public ResponseEntity<ApiResponse<List<PaymentOptionResponse>>> getByAsset(@PathVariable Long assetId) {
        return ResponseEntity.ok(ApiResponse.success(paymentOptionHandler.getByAsset(assetId)));
    }
}

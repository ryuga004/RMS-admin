package com.rms.admin.controller;

import com.rms.admin.data.dto.ApiResponse;
import com.rms.admin.data.dto.PaginationResponse;
import com.rms.admin.data.dto.payment.CheckoutSessionResponse;
import com.rms.admin.data.dto.payment.CreateCheckoutSessionRequest;
import com.rms.admin.handler.PaymentHandler;
import com.rms.admin.security.JwtPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentHandler paymentHandler;

    /**
     * Tenant initiates a payment — creates a Stripe Checkout Session.
     */
    @PostMapping("/checkout-session")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponse<CheckoutSessionResponse>> createCheckoutSession(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody CreateCheckoutSessionRequest request) {
        CheckoutSessionResponse response = paymentHandler.createCheckoutSession(request, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Stripe webhook endpoint — must be public (no auth).
     * Receives events such as checkout.session.completed.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            HttpServletRequest httpRequest,
            @RequestHeader("Stripe-Signature") String sigHeader) throws IOException {
        byte[] bodyBytes = httpRequest.getInputStream().readAllBytes();
        String payload = new String(bodyBytes, StandardCharsets.UTF_8);
        paymentHandler.processWebhook(payload, sigHeader);
        return ResponseEntity.ok("OK");
    }

    /**
     * Owner views their received payments (paginated).
     */
    @GetMapping("/history/owner")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaginationResponse>> getOwnerHistory(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize) {
        PaginationResponse response = paymentHandler.getHistoryByOwner(principal.getUserId(), pageNumber, pageSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Tenant views their own payment history (paginated).
     */
    @GetMapping("/history/tenant")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponse<PaginationResponse>> getTenantHistory(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize) {
        PaginationResponse response = paymentHandler.getHistoryByTenant(principal.getUserId(), pageNumber, pageSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

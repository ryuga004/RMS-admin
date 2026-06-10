package com.rms.admin.service;

import com.rms.admin.config.StripeProperties;
import com.rms.admin.data.dto.payment.PaymentOptionResponse;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    private final StripeProperties stripeProperties;

    /**
     * Creates a Stripe Checkout Session for a payment option.
     *
     * @param paymentOption  The payment option being paid for
     * @param paymentId      Internal payment record ID (stored in Stripe metadata)
     * @param tenantEmail    Pre-fill customer email in Stripe
     * @param successUrl     URL Stripe redirects to on success (include {CHECKOUT_SESSION_ID})
     * @param cancelUrl      URL Stripe redirects to on cancel
     * @return Stripe Session object
     */
    public Session createCheckoutSession(PaymentOptionResponse paymentOption,
                                         Long paymentId,
                                         String tenantEmail,
                                         String successUrl,
                                         String cancelUrl) {
        try {
            // Convert amount to cents (Stripe uses smallest currency unit)
            long amountInCents = paymentOption.getAmount()
                    .multiply(java.math.BigDecimal.valueOf(100))
                    .longValue();

            String currency = paymentOption.getCurrency() != null
                    ? paymentOption.getCurrency().toLowerCase()
                    : stripeProperties.getCurrency();

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .setCustomerEmail(tenantEmail)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(currency)
                                                    .setUnitAmount(amountInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(paymentOption.getName())
                                                                    .setDescription(paymentOption.getDescription() != null
                                                                            ? paymentOption.getDescription()
                                                                            : "Payment for " + paymentOption.getName())
                                                                    .build())
                                                    .build())
                                    .build())
                    .putMetadata("paymentId", String.valueOf(paymentId))
                    .putMetadata("assetId", String.valueOf(paymentOption.getAssetId()))
                    .putMetadata("paymentOptionId", String.valueOf(paymentOption.getId()))
                    .build();

            Session session = Session.create(params);
            log.info("Created Stripe checkout session={} for paymentId={}", session.getId(), paymentId);
            return session;
        } catch (StripeException e) {
            log.error("Failed to create Stripe checkout session for paymentId={}", paymentId, e);
            throw new RuntimeException("Failed to create checkout session: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies and constructs a Stripe webhook event.
     *
     * @param payload   Raw request body as string
     * @param sigHeader Stripe-Signature header value
     * @return Verified Stripe Event
     * @throws SignatureVerificationException if signature is invalid
     */
    public Event constructWebhookEvent(String payload, String sigHeader) throws SignatureVerificationException {
        return Webhook.constructEvent(payload, sigHeader, stripeProperties.getWebhookSecret());
    }
}

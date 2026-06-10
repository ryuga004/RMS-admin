package com.rms.admin.handler;

import com.rms.admin.data.dao.interfaces.IPaymentDao;
import com.rms.admin.data.dao.interfaces.IPaymentOptionDao;
import com.rms.admin.data.dao.interfaces.IUserDao;
import com.rms.admin.data.dto.PaginationResponse;
import com.rms.admin.data.dto.email.EmailTemplateDto;
import com.rms.admin.data.dto.payment.CheckoutSessionResponse;
import com.rms.admin.data.dto.payment.CreateCheckoutSessionRequest;
import com.rms.admin.data.dto.payment.PaymentListItemResult;
import com.rms.admin.data.dto.payment.PaymentOptionResponse;
import com.rms.admin.data.dto.users.UserResponse;
import com.rms.admin.exception.BadRequestException;
import com.rms.admin.exception.NotFoundException;
import com.rms.admin.service.StripeService;
import com.rms.admin.service.messagePublisher.adminNotification.AdminNotificationPublisher;
import com.rms.admin.service.messagePublisher.adminNotification.Email;
import com.rms.admin.service.messagePublisher.userNotification.UserNotificationEventFactory;
import com.rms.admin.service.messagePublisher.userNotification.UserNotificationPublisher;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentHandler {

    private static final String PAYMENT_OPTION_NOT_FOUND = "PAYMENT_OPTION_NOT_FOUND";
    private static final String PAYMENT_OPTION_INACTIVE = "PAYMENT_OPTION_INACTIVE";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final IPaymentOptionDao paymentOptionDao;
    private final IPaymentDao paymentDao;
    private final IUserDao userDao;
    private final StripeService stripeService;
    private final UserNotificationPublisher notificationPublisher;
    private final UserNotificationEventFactory notificationFactory;
    private final AdminNotificationPublisher emailPublisher;
    private final EmailTemplateHandler emailTemplateHandler;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // =========================================================================
    // CREATE CHECKOUT SESSION
    // =========================================================================

    @Transactional
    public CheckoutSessionResponse createCheckoutSession(CreateCheckoutSessionRequest request, Long tenantUserId) {
        PaymentOptionResponse paymentOption = paymentOptionDao.findById(request.getPaymentOptionId());
        if (paymentOption == null) {
            throw new NotFoundException(PAYMENT_OPTION_NOT_FOUND, "Payment option not found");
        }
        if (!paymentOption.isActive()) {
            throw new BadRequestException(PAYMENT_OPTION_INACTIVE, "This payment option is no longer active");
        }

        UserResponse tenant = userDao.findById(tenantUserId);

        // 1. Insert PENDING payment record
        String description = "Payment for " + paymentOption.getName()
                + (paymentOption.getAssetTitle() != null ? " – " + paymentOption.getAssetTitle() : "");
        Long paymentId = paymentDao.insert(
                paymentOption.getId(),
                paymentOption.getAssetId(),
                tenantUserId,
                paymentOption.getOwnerId(),
                paymentOption.getAmount(),
                paymentOption.getCurrency(),
                description);

        // 2. Create Stripe session
        String successUrl = frontendUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl = frontendUrl + "/payment/cancel";

        Session session = stripeService.createCheckoutSession(
                paymentOption, paymentId,
                tenant != null ? tenant.getEmail() : null,
                successUrl, cancelUrl);

        // 3. Persist session ID on payment
        paymentDao.updateStripeSessionId(paymentId, session.getId());

        log.info("Checkout session created for paymentId={} tenantUserId={}", paymentId, tenantUserId);
        return CheckoutSessionResponse.builder()
                .sessionId(session.getId())
                .checkoutUrl(session.getUrl())
                .paymentId(paymentId)
                .build();
    }

    // =========================================================================
    // PROCESS WEBHOOK
    // =========================================================================

    public void processWebhook(String payload, String sigHeader) {
        Event event;
        try {
            event = stripeService.constructWebhookEvent(payload, sigHeader);
        } catch (SignatureVerificationException e) {
            log.warn("Webhook signature verification failed: {}", e.getMessage());
            throw new BadRequestException("INVALID_SIGNATURE", "Webhook signature verification failed");
        }

        log.info("Received Stripe webhook event type={}", event.getType());

        if ("checkout.session.completed".equals(event.getType())) {
            event.getDataObjectDeserializer().getObject().ifPresent(obj -> {
                if (obj instanceof Session session) {
                    handleCheckoutCompleted(session);
                }
            });
        }
    }

    private void handleCheckoutCompleted(Session session) {
        String sessionId = session.getId();
        String paymentIntentId = session.getPaymentIntent();

        paymentDao.updateStatusAndPaymentIntentId(sessionId, "COMPLETED", paymentIntentId);
        paymentDao.updatePaidAt(sessionId);

        PaymentListItemResult payment = paymentDao.findByStripeCheckoutSessionId(sessionId);
        if (payment == null) {
            log.warn("No payment record found for sessionId={}", sessionId);
            return;
        }

        log.info("Payment completed paymentId={} tenantId={} ownerId={}",
                payment.getId(), payment.getTenantUserId(), payment.getOwnerUserId());

        // In-app notification to owner
        try {
            notificationPublisher.publish(
                    notificationFactory.paymentReceived(
                            payment.getOwnerUserId(),
                            payment.getAssetId(),
                            payment.getTenantUserId(),
                            payment.getAmount()));
        } catch (Exception e) {
            log.error("Failed to publish in-app notification for payment id={}", payment.getId(), e);
        }

        // Email to owner
        try {
            sendPaymentEmailToOwner(payment);
        } catch (Exception e) {
            log.error("Failed to send payment email to owner for payment id={}", payment.getId(), e);
        }

        // Email to tenant
        try {
            sendPaymentReceiptToTenant(payment);
        } catch (Exception e) {
            log.error("Failed to send payment receipt to tenant for payment id={}", payment.getId(), e);
        }
    }

    private void sendPaymentEmailToOwner(PaymentListItemResult payment) {
        EmailTemplateDto template = emailTemplateHandler.getPaymentReceivedOwnerTemplate();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("owner_name", nvl(payment.getOwnerName()));
        placeholders.put("currency", nvl(payment.getCurrency()));
        placeholders.put("amount", payment.getAmount() != null ? payment.getAmount().toPlainString() : "0.00");
        placeholders.put("asset_title", nvl(payment.getAssetTitle()));
        placeholders.put("payment_option_name", nvl(payment.getPaymentOptionName()));
        placeholders.put("tenant_name", nvl(payment.getTenantName()));
        placeholders.put("tenant_email", nvl(payment.getTenantEmail()));
        placeholders.put("paid_at", payment.getPaidAt() != null ? payment.getPaidAt().format(DATE_FORMATTER) : "N/A");
        placeholders.put("payment_intent_id", nvl(payment.getStripePaymentIntentId()));

        String body = EmailTemplateHandler.fillTemplate(template.getTemplate(), placeholders);

        emailPublisher.publishEmail(Email.builder()
                .to(payment.getOwnerEmail())
                .subject(template.getSubject())
                .body(body)
                .html(true)
                .build());
    }

    private void sendPaymentReceiptToTenant(PaymentListItemResult payment) {
        EmailTemplateDto template = emailTemplateHandler.getPaymentReceiptTenantTemplate();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("tenant_name", nvl(payment.getTenantName()));
        placeholders.put("currency", nvl(payment.getCurrency()));
        placeholders.put("amount", payment.getAmount() != null ? payment.getAmount().toPlainString() : "0.00");
        placeholders.put("asset_title", nvl(payment.getAssetTitle()));
        placeholders.put("payment_option_name", nvl(payment.getPaymentOptionName()));
        placeholders.put("paid_at", payment.getPaidAt() != null ? payment.getPaidAt().format(DATE_FORMATTER) : "N/A");
        placeholders.put("payment_intent_id", nvl(payment.getStripePaymentIntentId()));

        String body = EmailTemplateHandler.fillTemplate(template.getTemplate(), placeholders);

        emailPublisher.publishEmail(Email.builder()
                .to(payment.getTenantEmail())
                .subject(template.getSubject())
                .body(body)
                .html(true)
                .build());
    }

    // =========================================================================
    // PAYMENT HISTORY
    // =========================================================================

    @Transactional(readOnly = true)
    public PaginationResponse getHistoryByOwner(Long ownerUserId, int page, int limit) {
        List<PaymentListItemResult> items = paymentDao.findByOwnerUserId(ownerUserId, page, limit);
        long total = paymentDao.countByOwnerUserId(ownerUserId);
        return PaginationResponse.builder().result(items).totalCount(total).build();
    }

    @Transactional(readOnly = true)
    public PaginationResponse getHistoryByTenant(Long tenantUserId, int page, int limit) {
        List<PaymentListItemResult> items = paymentDao.findByTenantUserId(tenantUserId, page, limit);
        long total = paymentDao.countByTenantUserId(tenantUserId);
        return PaginationResponse.builder().result(items).totalCount(total).build();
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private String nvl(String value) {
        return value != null ? value : "";
    }
}

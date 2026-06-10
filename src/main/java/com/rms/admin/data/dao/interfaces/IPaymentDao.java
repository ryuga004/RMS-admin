package com.rms.admin.data.dao.interfaces;

import com.rms.admin.data.dto.payment.PaymentListItemResult;

import java.math.BigDecimal;
import java.util.List;

public interface IPaymentDao {

    Long insert(Long paymentOptionId, Long assetId, Long tenantUserId, Long ownerUserId,
                BigDecimal amount, String currency, String description);

    void updateStripeSessionId(Long paymentId, String stripeCheckoutSessionId);

    void updateStatusAndPaymentIntentId(String stripeCheckoutSessionId, String status, String stripePaymentIntentId);

    void updatePaidAt(String stripeCheckoutSessionId);

    PaymentListItemResult findByStripeCheckoutSessionId(String stripeCheckoutSessionId);

    List<PaymentListItemResult> findByTenantUserId(Long tenantUserId, int page, int limit);

    long countByTenantUserId(Long tenantUserId);

    List<PaymentListItemResult> findByOwnerUserId(Long ownerUserId, int page, int limit);

    long countByOwnerUserId(Long ownerUserId);
}

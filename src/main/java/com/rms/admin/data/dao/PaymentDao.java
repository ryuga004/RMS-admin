package com.rms.admin.data.dao;

import com.rms.admin.data.dao.interfaces.IPaymentDao;
import com.rms.admin.data.dto.payment.PaymentListItemResult;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static com.rms.admin.persistence.tables.Asset.ASSET;
import static com.rms.admin.persistence.tables.PaymentOptions.PAYMENT_OPTIONS;
import static com.rms.admin.persistence.tables.Payments.PAYMENTS;
import static com.rms.admin.persistence.tables.Users.USERS;

@Repository
@RequiredArgsConstructor
public class PaymentDao implements IPaymentDao {

    private final DSLContext dsl;

    private static final com.rms.admin.persistence.tables.Users TENANT_USER = USERS.as("tenantUser");
    private static final com.rms.admin.persistence.tables.Users OWNER_USER = USERS.as("ownerUser");

    @Override
    public Long insert(Long paymentOptionId, Long assetId, Long tenantUserId, Long ownerUserId,
                       BigDecimal amount, String currency, String description) {
        return dsl.insertInto(PAYMENTS)
                .columns(
                        PAYMENTS.PAYMENT_OPTION_ID,
                        PAYMENTS.ASSET_ID,
                        PAYMENTS.TENANT_USER_ID,
                        PAYMENTS.OWNER_USER_ID,
                        PAYMENTS.AMOUNT,
                        PAYMENTS.CURRENCY,
                        PAYMENTS.DESCRIPTION,
                        PAYMENTS.STATUS)
                .values(paymentOptionId, assetId, tenantUserId, ownerUserId,
                        amount,
                        currency != null ? currency : "USD",
                        description,
                        "PENDING")
                .returningResult(PAYMENTS.ID)
                .fetchOneInto(Long.class);
    }

    @Override
    public void updateStripeSessionId(Long paymentId, String stripeCheckoutSessionId) {
        dsl.update(PAYMENTS)
                .set(PAYMENTS.STRIPE_CHECKOUT_SESSION_ID, stripeCheckoutSessionId)
                .where(PAYMENTS.ID.eq(paymentId))
                .execute();
    }

    @Override
    public void updateStatusAndPaymentIntentId(String stripeCheckoutSessionId, String status, String stripePaymentIntentId) {
        dsl.update(PAYMENTS)
                .set(PAYMENTS.STATUS, status)
                .set(PAYMENTS.STRIPE_PAYMENT_INTENT_ID, stripePaymentIntentId)
                .where(PAYMENTS.STRIPE_CHECKOUT_SESSION_ID.eq(stripeCheckoutSessionId))
                .execute();
    }

    @Override
    public void updatePaidAt(String stripeCheckoutSessionId) {
        dsl.update(PAYMENTS)
                .set(PAYMENTS.PAID_AT, OffsetDateTime.now())
                .where(PAYMENTS.STRIPE_CHECKOUT_SESSION_ID.eq(stripeCheckoutSessionId))
                .execute();
    }

    @Override
    public PaymentListItemResult findByStripeCheckoutSessionId(String stripeCheckoutSessionId) {
        return buildPaymentQuery()
                .and(PAYMENTS.STRIPE_CHECKOUT_SESSION_ID.eq(stripeCheckoutSessionId))
                .fetchOneInto(PaymentListItemResult.class);
    }

    @Override
    public List<PaymentListItemResult> findByTenantUserId(Long tenantUserId, int page, int limit) {
        int offset = page * limit;
        return buildPaymentQuery()
                .and(PAYMENTS.TENANT_USER_ID.eq(tenantUserId))
                .orderBy(PAYMENTS.CREATED_AT.desc())
                .limit(limit)
                .offset(offset)
                .fetchInto(PaymentListItemResult.class);
    }

    @Override
    public long countByTenantUserId(Long tenantUserId) {
        return dsl.selectCount()
                .from(PAYMENTS)
                .where(PAYMENTS.TENANT_USER_ID.eq(tenantUserId))
                .fetchOneInto(Long.class);
    }

    @Override
    public List<PaymentListItemResult> findByOwnerUserId(Long ownerUserId, int page, int limit) {
        int offset = page * limit;
        return buildPaymentQuery()
                .and(PAYMENTS.OWNER_USER_ID.eq(ownerUserId))
                .orderBy(PAYMENTS.CREATED_AT.desc())
                .limit(limit)
                .offset(offset)
                .fetchInto(PaymentListItemResult.class);
    }

    @Override
    public long countByOwnerUserId(Long ownerUserId) {
        return dsl.selectCount()
                .from(PAYMENTS)
                .where(PAYMENTS.OWNER_USER_ID.eq(ownerUserId))
                .fetchOneInto(Long.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private org.jooq.SelectConditionStep<org.jooq.Record> buildPaymentQuery() {
        com.rms.admin.persistence.tables.Users tenantAlias = USERS.as("tenantUser");
        com.rms.admin.persistence.tables.Users ownerAlias = USERS.as("ownerUser");

        var step = dsl.select(
                        PAYMENTS.ID,
                        PAYMENTS.PAYMENT_OPTION_ID,
                        PAYMENT_OPTIONS.NAME.as("paymentOptionName"),
                        PAYMENTS.ASSET_ID,
                        ASSET.TITLE.as("assetTitle"),
                        PAYMENTS.TENANT_USER_ID,
                        tenantAlias.NAME.as("tenantName"),
                        tenantAlias.EMAIL.as("tenantEmail"),
                        PAYMENTS.OWNER_USER_ID,
                        ownerAlias.NAME.as("ownerName"),
                        ownerAlias.EMAIL.as("ownerEmail"),
                        PAYMENTS.AMOUNT,
                        PAYMENTS.CURRENCY,
                        PAYMENTS.STATUS,
                        PAYMENTS.STRIPE_CHECKOUT_SESSION_ID,
                        PAYMENTS.STRIPE_PAYMENT_INTENT_ID,
                        PAYMENTS.DESCRIPTION,
                        PAYMENTS.PAID_AT,
                        PAYMENTS.CREATED_AT)
                .from(PAYMENTS)
                .leftJoin(PAYMENT_OPTIONS).on(PAYMENTS.PAYMENT_OPTION_ID.eq(PAYMENT_OPTIONS.ID))
                .leftJoin(ASSET).on(PAYMENTS.ASSET_ID.eq(ASSET.ID))
                .leftJoin(tenantAlias).on(PAYMENTS.TENANT_USER_ID.eq(tenantAlias.ID))
                .leftJoin(ownerAlias).on(PAYMENTS.OWNER_USER_ID.eq(ownerAlias.ID))
                .where(org.jooq.impl.DSL.trueCondition());
        // Safe: callers only use fetchInto / fetchOneInto which work on any record type
        return (org.jooq.SelectConditionStep<org.jooq.Record>) (org.jooq.SelectConditionStep) step;
    }
}

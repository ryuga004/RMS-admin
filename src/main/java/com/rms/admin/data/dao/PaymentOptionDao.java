package com.rms.admin.data.dao;

import com.rms.admin.data.dao.interfaces.IPaymentOptionDao;
import com.rms.admin.data.dto.payment.PaymentOptionResponse;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

import static com.rms.admin.persistence.tables.Asset.ASSET;
import static com.rms.admin.persistence.tables.PaymentOptions.PAYMENT_OPTIONS;

@Repository
@RequiredArgsConstructor
public class PaymentOptionDao implements IPaymentOptionDao {

    private final DSLContext dsl;

    @Override
    public Long insert(Long assetId, Long ownerId, String name, String description,
                       BigDecimal amount, String currency, String paymentType,
                       boolean isRecurring, String recurringInterval) {
        return dsl.insertInto(PAYMENT_OPTIONS)
                .columns(
                        PAYMENT_OPTIONS.ASSET_ID,
                        PAYMENT_OPTIONS.OWNER_ID,
                        PAYMENT_OPTIONS.NAME,
                        PAYMENT_OPTIONS.DESCRIPTION,
                        PAYMENT_OPTIONS.AMOUNT,
                        PAYMENT_OPTIONS.CURRENCY,
                        PAYMENT_OPTIONS.PAYMENT_TYPE,
                        PAYMENT_OPTIONS.IS_RECURRING,
                        PAYMENT_OPTIONS.RECURRING_INTERVAL)
                .values(assetId, ownerId, name, description, amount,
                        currency != null ? currency : "USD",
                        paymentType != null ? paymentType : "RENT",
                        isRecurring,
                        recurringInterval)
                .returningResult(PAYMENT_OPTIONS.ID)
                .fetchOneInto(Long.class);
    }

    @Override
    public void update(Long id, String name, String description, BigDecimal amount,
                       String currency, String paymentType, boolean isRecurring,
                       String recurringInterval, boolean isActive) {
        dsl.update(PAYMENT_OPTIONS)
                .set(PAYMENT_OPTIONS.NAME, name)
                .set(PAYMENT_OPTIONS.DESCRIPTION, description)
                .set(PAYMENT_OPTIONS.AMOUNT, amount)
                .set(PAYMENT_OPTIONS.CURRENCY, currency)
                .set(PAYMENT_OPTIONS.PAYMENT_TYPE, paymentType)
                .set(PAYMENT_OPTIONS.IS_RECURRING, isRecurring)
                .set(PAYMENT_OPTIONS.RECURRING_INTERVAL, recurringInterval)
                .set(PAYMENT_OPTIONS.IS_ACTIVE, isActive)
                .where(PAYMENT_OPTIONS.ID.eq(id))
                .execute();
    }

    @Override
    public void deleteById(Long id) {
        dsl.deleteFrom(PAYMENT_OPTIONS)
                .where(PAYMENT_OPTIONS.ID.eq(id))
                .execute();
    }

    @Override
    public PaymentOptionResponse findById(Long id) {
        return dsl.select(
                        PAYMENT_OPTIONS.ID,
                        PAYMENT_OPTIONS.ASSET_ID,
                        ASSET.TITLE.as("assetTitle"),
                        PAYMENT_OPTIONS.OWNER_ID,
                        PAYMENT_OPTIONS.NAME,
                        PAYMENT_OPTIONS.DESCRIPTION,
                        PAYMENT_OPTIONS.AMOUNT,
                        PAYMENT_OPTIONS.CURRENCY,
                        PAYMENT_OPTIONS.PAYMENT_TYPE,
                        PAYMENT_OPTIONS.IS_RECURRING,
                        PAYMENT_OPTIONS.RECURRING_INTERVAL,
                        PAYMENT_OPTIONS.IS_ACTIVE,
                        PAYMENT_OPTIONS.CREATED_AT,
                        PAYMENT_OPTIONS.UPDATED_AT)
                .from(PAYMENT_OPTIONS)
                .leftJoin(ASSET).on(PAYMENT_OPTIONS.ASSET_ID.eq(ASSET.ID))
                .where(PAYMENT_OPTIONS.ID.eq(id))
                .fetchOneInto(PaymentOptionResponse.class);
    }

    @Override
    public List<PaymentOptionResponse> findByOwnerId(Long ownerId, int page, int limit) {
        int offset = page * limit;
        return dsl.select(
                        PAYMENT_OPTIONS.ID,
                        PAYMENT_OPTIONS.ASSET_ID,
                        ASSET.TITLE.as("assetTitle"),
                        PAYMENT_OPTIONS.OWNER_ID,
                        PAYMENT_OPTIONS.NAME,
                        PAYMENT_OPTIONS.DESCRIPTION,
                        PAYMENT_OPTIONS.AMOUNT,
                        PAYMENT_OPTIONS.CURRENCY,
                        PAYMENT_OPTIONS.PAYMENT_TYPE,
                        PAYMENT_OPTIONS.IS_RECURRING,
                        PAYMENT_OPTIONS.RECURRING_INTERVAL,
                        PAYMENT_OPTIONS.IS_ACTIVE,
                        PAYMENT_OPTIONS.CREATED_AT,
                        PAYMENT_OPTIONS.UPDATED_AT)
                .from(PAYMENT_OPTIONS)
                .leftJoin(ASSET).on(PAYMENT_OPTIONS.ASSET_ID.eq(ASSET.ID))
                .where(PAYMENT_OPTIONS.OWNER_ID.eq(ownerId))
                .orderBy(PAYMENT_OPTIONS.CREATED_AT.desc())
                .limit(limit)
                .offset(offset)
                .fetchInto(PaymentOptionResponse.class);
    }

    @Override
    public long countByOwnerId(Long ownerId) {
        return dsl.selectCount()
                .from(PAYMENT_OPTIONS)
                .where(PAYMENT_OPTIONS.OWNER_ID.eq(ownerId))
                .fetchOneInto(Long.class);
    }

    @Override
    public List<PaymentOptionResponse> findByAssetId(Long assetId) {
        return dsl.select(
                        PAYMENT_OPTIONS.ID,
                        PAYMENT_OPTIONS.ASSET_ID,
                        ASSET.TITLE.as("assetTitle"),
                        PAYMENT_OPTIONS.OWNER_ID,
                        PAYMENT_OPTIONS.NAME,
                        PAYMENT_OPTIONS.DESCRIPTION,
                        PAYMENT_OPTIONS.AMOUNT,
                        PAYMENT_OPTIONS.CURRENCY,
                        PAYMENT_OPTIONS.PAYMENT_TYPE,
                        PAYMENT_OPTIONS.IS_RECURRING,
                        PAYMENT_OPTIONS.RECURRING_INTERVAL,
                        PAYMENT_OPTIONS.IS_ACTIVE,
                        PAYMENT_OPTIONS.CREATED_AT,
                        PAYMENT_OPTIONS.UPDATED_AT)
                .from(PAYMENT_OPTIONS)
                .leftJoin(ASSET).on(PAYMENT_OPTIONS.ASSET_ID.eq(ASSET.ID))
                .where(PAYMENT_OPTIONS.ASSET_ID.eq(assetId))
                .and(PAYMENT_OPTIONS.IS_ACTIVE.isTrue())
                .orderBy(PAYMENT_OPTIONS.CREATED_AT.desc())
                .fetchInto(PaymentOptionResponse.class);
    }

    @Override
    public boolean existsByIdAndOwnerId(Long id, Long ownerId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(PAYMENT_OPTIONS)
                        .where(PAYMENT_OPTIONS.ID.eq(id))
                        .and(PAYMENT_OPTIONS.OWNER_ID.eq(ownerId)));
    }
}

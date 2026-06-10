package com.rms.admin.data.dao.interfaces;

import com.rms.admin.data.dto.payment.PaymentOptionResponse;

import java.math.BigDecimal;
import java.util.List;

public interface IPaymentOptionDao {

    Long insert(Long assetId, Long ownerId, String name, String description,
                BigDecimal amount, String currency, String paymentType,
                boolean isRecurring, String recurringInterval);

    void update(Long id, String name, String description, BigDecimal amount,
                String currency, String paymentType, boolean isRecurring,
                String recurringInterval, boolean isActive);

    void deleteById(Long id);

    PaymentOptionResponse findById(Long id);

    List<PaymentOptionResponse> findByOwnerId(Long ownerId, int page, int limit);

    long countByOwnerId(Long ownerId);

    List<PaymentOptionResponse> findByAssetId(Long assetId);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);
}

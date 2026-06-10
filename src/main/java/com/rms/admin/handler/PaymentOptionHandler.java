package com.rms.admin.handler;

import com.rms.admin.data.dao.interfaces.IAssetDao;
import com.rms.admin.data.dao.interfaces.IPaymentOptionDao;
import com.rms.admin.data.dto.PaginationResponse;
import com.rms.admin.data.dto.payment.CreatePaymentOptionRequest;
import com.rms.admin.data.dto.payment.PaymentOptionResponse;
import com.rms.admin.data.dto.payment.UpdatePaymentOptionRequest;
import com.rms.admin.exception.BadRequestException;
import com.rms.admin.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentOptionHandler {

    private static final String PAYMENT_OPTION_NOT_FOUND = "PAYMENT_OPTION_NOT_FOUND";
    private static final String ASSET_NOT_FOUND = "ASSET_NOT_FOUND";
    private static final String ASSET_NOT_OWNED = "ASSET_NOT_OWNED";

    private final IPaymentOptionDao paymentOptionDao;
    private final IAssetDao assetDao;

    @Transactional
    public Long create(CreatePaymentOptionRequest request, Long ownerId) {
        // Verify the owner actually owns this asset
        if (!assetDao.existsByAssetIdAndOwnerId(request.getAssetId(), ownerId)) {
            throw new NotFoundException(ASSET_NOT_FOUND, "Asset not found or not owned by you");
        }

        Long id = paymentOptionDao.insert(
                request.getAssetId(),
                ownerId,
                request.getName(),
                request.getDescription(),
                request.getAmount(),
                request.getCurrency(),
                request.getPaymentType(),
                request.isRecurring(),
                request.getRecurringInterval());

        log.info("Created payment option id={} for assetId={} ownerId={}", id, request.getAssetId(), ownerId);
        return id;
    }

    @Transactional
    public void update(Long id, UpdatePaymentOptionRequest request, Long ownerId) {
        if (!paymentOptionDao.existsByIdAndOwnerId(id, ownerId)) {
            throw new NotFoundException(PAYMENT_OPTION_NOT_FOUND, "Payment option not found");
        }

        paymentOptionDao.update(
                id,
                request.getName(),
                request.getDescription(),
                request.getAmount(),
                request.getCurrency(),
                request.getPaymentType(),
                request.isRecurring(),
                request.getRecurringInterval(),
                request.isActive());

        log.info("Updated payment option id={} ownerId={}", id, ownerId);
    }

    @Transactional
    public void delete(Long id, Long ownerId) {
        if (!paymentOptionDao.existsByIdAndOwnerId(id, ownerId)) {
            throw new NotFoundException(PAYMENT_OPTION_NOT_FOUND, "Payment option not found");
        }
        paymentOptionDao.deleteById(id);
        log.info("Deleted payment option id={} ownerId={}", id, ownerId);
    }

    @Transactional(readOnly = true)
    public PaginationResponse getByOwner(Long ownerId, int page, int limit) {
        List<PaymentOptionResponse> items = paymentOptionDao.findByOwnerId(ownerId, page, limit);
        long total = paymentOptionDao.countByOwnerId(ownerId);
        return PaginationResponse.builder()
                .result(items)
                .totalCount(total)
                .build();
    }

    @Transactional(readOnly = true)
    public PaymentOptionResponse getById(Long id) {
        PaymentOptionResponse response = paymentOptionDao.findById(id);
        if (response == null) {
            throw new NotFoundException(PAYMENT_OPTION_NOT_FOUND, "Payment option not found");
        }
        return response;
    }

    @Transactional(readOnly = true)
    public List<PaymentOptionResponse> getByAsset(Long assetId) {
        return paymentOptionDao.findByAssetId(assetId);
    }
}

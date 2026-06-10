package com.rms.admin.persistence.tables.records;

import com.rms.admin.persistence.tables.PaymentOptions;
import org.jooq.impl.UpdatableRecordImpl;

@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PaymentOptionsRecord extends UpdatableRecordImpl<PaymentOptionsRecord> {

    private static final long serialVersionUID = 1L;

    public PaymentOptionsRecord() {
        super(PaymentOptions.PAYMENT_OPTIONS);
    }
}

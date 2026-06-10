package com.rms.admin.persistence.tables.records;

import com.rms.admin.persistence.tables.Payments;
import org.jooq.impl.UpdatableRecordImpl;

@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PaymentsRecord extends UpdatableRecordImpl<PaymentsRecord> {

    private static final long serialVersionUID = 1L;

    public PaymentsRecord() {
        super(Payments.PAYMENTS);
    }
}

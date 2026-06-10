package com.rms.admin.utils.constants;

import java.util.Arrays;
import java.util.Optional;

public enum AssetTenantRequestType {
    REQUEST,
    INVITATION,
    EXIT;

    public static Optional<AssetTenantRequestType> from(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(t -> t.name().equalsIgnoreCase(value.trim()))
                .findFirst();
    }
}

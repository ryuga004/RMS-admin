package com.rms.admin.utils.constants;

import lombok.Getter;

import java.util.Optional;

@Getter
public enum DefaultRole {
    ADMIN(2L),
    TENANT(3L),
    SUPER_ADMIN(1L);

    private final Long roleId;

    DefaultRole(Long roleId) {
        this.roleId = roleId;
    }

    public static Optional<DefaultRole> fromRoleId(Long roleId) {
        if (roleId == null)
            return Optional.empty();
        for (DefaultRole r : values()) {
            if (r.roleId.equals(roleId))
                return Optional.of(r);
        }
        return Optional.empty();
    }
}
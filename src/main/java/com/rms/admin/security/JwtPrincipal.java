package com.rms.admin.security;

import io.jsonwebtoken.Claims;
import lombok.Getter;

@Getter
public class JwtPrincipal {

    private final String subject;
    private final String email;
    private final Long roleId;

    public JwtPrincipal(Claims claims) {
        this.subject = claims.getSubject();
        this.email = claims.get("email", String.class);
        this.roleId = claims.get("roleId", Long.class);
    }

    public Long getUserId() {
        return subject != null ? Long.valueOf(subject) : null;
    }
}

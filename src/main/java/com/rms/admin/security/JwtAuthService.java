package com.rms.admin.security;

import com.rms.admin.data.dao.interfaces.IUserDao;
import com.rms.admin.utils.constants.DefaultRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtAuthService {

    private static final String CACHE_PREFIX = "jwt_secret:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final IUserDao userDao;
    private final StringRedisTemplate redisTemplate;


    public Optional<UsernamePasswordAuthenticationToken> authenticate(String token) {
        try {
            Long userId = extractUserIdFromPayload(token);
            String secret = resolveSecret(userId);
            if (secret == null) return Optional.empty();
            Claims claims = parseAndVerify(token, secret);
            JwtPrincipal principal = new JwtPrincipal(claims);
            String authority = roleToAuthority(principal.getRoleId());
            return Optional.of(new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority(authority))));
        } catch (Exception e) {
            log.debug("JWT authentication failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String resolveSecret(Long userId) {
        String cacheKey = CACHE_PREFIX + userId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return cached;
        return userDao.findJwtSecretByUserId(userId)
                .map(secret -> {
                    redisTemplate.opsForValue().set(cacheKey, secret, CACHE_TTL);
                    return secret;
                })
                .orElse(null);
    }

    private Long extractUserIdFromPayload(String token) {
        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
        int subIndex = payload.indexOf("\"sub\":\"");
        if (subIndex == -1) throw new IllegalArgumentException("Token missing subject");
        int start = subIndex + 7;
        int end = payload.indexOf("\"", start);
        return Long.valueOf(payload.substring(start, end));
    }

    private Claims parseAndVerify(String token, String secret) {
        return Jwts.parser()
                .verifyWith(buildKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static SecretKey buildKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private static String roleToAuthority(Long roleId) {
        if (roleId == null) return "ROLE_UNKNOWN";
        return DefaultRole.fromRoleId(roleId)
                .map(r -> "ROLE_" + r.name())
                .orElse("ROLE_" + roleId);
    }
}

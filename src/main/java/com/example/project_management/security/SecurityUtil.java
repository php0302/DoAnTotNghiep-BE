package com.example.project_management.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

public class SecurityUtil {

    private SecurityUtil() {}

    public static Optional<String> getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return Optional.ofNullable(jwtAuth.getToken().getSubject());
        }
        if (auth != null && auth.getPrincipal() instanceof String principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static Optional<Long> getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Object userIdRaw = jwtAuth.getToken().getClaims().get("userId");
            if (userIdRaw instanceof Long l) return Optional.of(l);
            if (userIdRaw instanceof Integer i) return Optional.of(i.longValue());
            if (userIdRaw instanceof String s) return Optional.of(Long.parseLong(s));
        }
        return Optional.empty();
    }
}

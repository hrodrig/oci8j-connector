package com.connectors.oracle8i.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Hardening config (SPEC §7.3).
 * YAML: hardening.*; env via config.yaml placeholders.
 */
@Component
public class HardeningConfig {

    /** Max requests per window per client IP. 0 = disabled. */
    @Value("${hardening.rate_limit_max:${RATE_LIMIT_MAX:0}}")
    private int rateLimitMax;

    @Value("${hardening.rate_limit_window_seconds:${RATE_LIMIT_WINDOW_SECONDS:60}}")
    private int rateLimitWindowSeconds;

    /** Comma-separated origins. Empty = allow *. */
    @Value("${hardening.cors_origins:${CORS_ORIGINS:}}")
    private String corsOrigins;

    public int getRateLimitMax() {
        return rateLimitMax;
    }

    public int getRateLimitWindowSeconds() {
        return rateLimitWindowSeconds <= 0 ? 60 : rateLimitWindowSeconds;
    }

    public boolean isRateLimitEnabled() {
        return rateLimitMax > 0;
    }

    public String getCorsOrigins() {
        return corsOrigins == null ? "" : corsOrigins.trim();
    }
}

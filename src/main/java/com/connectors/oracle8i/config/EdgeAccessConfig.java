package com.connectors.oracle8i.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Edge identity config (SPEC §7.2). Empty defaults = off.
 */
@Component
public class EdgeAccessConfig {

    @Value("${TRUSTED_PROXIES:}")
    private String trustedProxies;

    @Value("${ALLOWED_CIDRS:}")
    private String allowedCidrs;

    public String getTrustedProxies() {
        return trustedProxies == null ? "" : trustedProxies.trim();
    }

    public String getAllowedCidrs() {
        return allowedCidrs == null ? "" : allowedCidrs.trim();
    }
}

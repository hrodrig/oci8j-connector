package com.connectors.oracle8i.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Edge identity + probe access (SPEC §7.2 / §7.2.1).
 */
@Component
public class EdgeAccessConfig {

    @Value("${TRUSTED_PROXIES:}")
    private String trustedProxies;

    @Value("${ALLOWED_CIDRS:}")
    private String allowedCidrs;

    /** Default true — probes skip Basic Auth. */
    @Value("${PROBES_PUBLIC:true}")
    private boolean probesPublic;

    @Value("${PROBES_ALLOWED_CIDRS:}")
    private String probesAllowedCidrs;

    public String getTrustedProxies() {
        return trustedProxies == null ? "" : trustedProxies.trim();
    }

    public String getAllowedCidrs() {
        return allowedCidrs == null ? "" : allowedCidrs.trim();
    }

    public boolean isProbesPublic() {
        return probesPublic;
    }

    public String getProbesAllowedCidrs() {
        return probesAllowedCidrs == null ? "" : probesAllowedCidrs.trim();
    }
}

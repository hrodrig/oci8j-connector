package com.connectors.oracle8i.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Configuration for Basic Authentication
 * Only enabled if both username and password are provided
 */
@Configuration
public class BasicAuthConfig {

    @Value("${basic_auth.enabled:false}")
    private boolean enabled;

    @Value("${basic_auth.username:}")
    private String username;

    @Value("${basic_auth.password:}")
    private String password;

    /**
     * Check if Basic Auth is enabled and properly configured
     * @return true if both username and password are provided
     */
    public boolean isEnabled() {
        return enabled && StringUtils.hasText(username) && StringUtils.hasText(password);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Get Basic Auth status for logging
     * @return status string
     */
    public String getStatus() {
        if (!enabled) {
            return "DISABLED (not enabled)";
        }
        if (!StringUtils.hasText(username)) {
            return "DISABLED (no username)";
        }
        if (!StringUtils.hasText(password)) {
            return "DISABLED (no password)";
        }
        return "ENABLED";
    }
}

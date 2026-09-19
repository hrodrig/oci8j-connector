package com.connectors.oracle8i.config;

import com.connectors.oracle8i.security.CidrMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * CORS allow-list from CORS_ORIGINS (SPEC §7.3). Empty = * with warn if auth off.
 */
@Configuration
public class CorsConfig implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

    @Autowired
    private HardeningConfig hardeningConfig;

    @Autowired
    private BasicAuthConfig basicAuthConfig;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = CidrMatcher.splitCsv(hardeningConfig.getCorsOrigins());
        if (origins.isEmpty()) {
            config.setAllowedOrigins(Collections.singletonList("*"));
        } else {
            config.setAllowedOrigins(origins);
        }
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Override
    public void run(ApplicationArguments args) {
        if (hardeningConfig.getCorsOrigins().isEmpty() && !basicAuthConfig.isEnabled()) {
            log.warn("CORS_ORIGINS is empty (allowing *); Basic Auth is off — set CORS_ORIGINS or enable Basic Auth for production");
        }
    }
}

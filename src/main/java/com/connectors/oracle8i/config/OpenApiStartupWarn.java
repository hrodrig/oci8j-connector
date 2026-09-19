package com.connectors.oracle8i.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

/**
 * Startup warn when OpenAPI is on without Basic Auth (SPEC §7.1).
 */
@Configuration
public class OpenApiStartupWarn implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(OpenApiStartupWarn.class);

    @Value("${oci8j.openapi.enabled:false}")
    private boolean openApiEnabled;

    @Autowired
    private BasicAuthConfig basicAuthConfig;

    @Override
    public void run(ApplicationArguments args) {
        if (openApiEnabled && !basicAuthConfig.isEnabled()) {
            log.warn("OpenAPI/Swagger is enabled but Basic Auth is off — do not expose this in production");
        }
    }
}

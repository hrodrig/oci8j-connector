package com.connectors.oracle8i.config;

import com.connectors.oracle8i.security.BasicAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Security configuration for Basic Authentication
 */
@Configuration
public class SecurityConfig {

    @Autowired
    private BasicAuthFilter basicAuthFilter;

    @Bean
    public FilterRegistrationBean<BasicAuthFilter> basicAuthFilterRegistration() {
        FilterRegistrationBean<BasicAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(basicAuthFilter);
        registration.addUrlPatterns("/api/v1/oci8j-connector/*");
        registration.setName("basicAuthFilter");
        registration.setOrder(1);
        return registration;
    }
}

package com.connectors.oracle8i.config;

import com.connectors.oracle8i.security.BasicAuthFilter;
import com.connectors.oracle8i.security.IpAllowListFilter;
import com.connectors.oracle8i.security.RateLimitFilter;
import com.connectors.oracle8i.security.SecurityHeadersFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Security filters: headers → IP allow-list → Basic Auth → rate limit.
 * OpenAPI paths share IP/auth/rate gates when docs are enabled.
 */
@Configuration
public class SecurityConfig {

    private static final String[] API_AND_DOCS = new String[] {
            "/api/v1/oci8j-connector/*",
            "/v3/api-docs",
            "/v3/api-docs/*",
            "/swagger-ui/*",
            "/swagger-ui.html"
    };

    @Autowired
    private BasicAuthFilter basicAuthFilter;

    @Autowired
    private IpAllowListFilter ipAllowListFilter;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @Autowired
    private SecurityHeadersFilter securityHeadersFilter;

    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilterRegistration() {
        FilterRegistrationBean<SecurityHeadersFilter> registration = new FilterRegistrationBean<SecurityHeadersFilter>();
        registration.setFilter(securityHeadersFilter);
        registration.addUrlPatterns("/*");
        registration.setName("securityHeadersFilter");
        registration.setOrder(0);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<IpAllowListFilter> ipAllowListFilterRegistration() {
        FilterRegistrationBean<IpAllowListFilter> registration = new FilterRegistrationBean<IpAllowListFilter>();
        registration.setFilter(ipAllowListFilter);
        registration.addUrlPatterns(API_AND_DOCS);
        registration.setName("ipAllowListFilter");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<BasicAuthFilter> basicAuthFilterRegistration() {
        FilterRegistrationBean<BasicAuthFilter> registration = new FilterRegistrationBean<BasicAuthFilter>();
        registration.setFilter(basicAuthFilter);
        registration.addUrlPatterns(API_AND_DOCS);
        registration.setName("basicAuthFilter");
        registration.setOrder(2);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration() {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<RateLimitFilter>();
        registration.setFilter(rateLimitFilter);
        registration.addUrlPatterns(API_AND_DOCS);
        registration.setName("rateLimitFilter");
        registration.setOrder(3);
        return registration;
    }
}

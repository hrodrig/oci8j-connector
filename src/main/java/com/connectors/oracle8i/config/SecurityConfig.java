package com.connectors.oracle8i.config;

import com.connectors.oracle8i.security.BasicAuthFilter;
import com.connectors.oracle8i.security.IpAllowListFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Security filters: IP allow-list (order 0), Basic Auth (order 1).
 */
@Configuration
public class SecurityConfig {

    @Autowired
    private BasicAuthFilter basicAuthFilter;

    @Autowired
    private IpAllowListFilter ipAllowListFilter;

    @Bean
    public FilterRegistrationBean<IpAllowListFilter> ipAllowListFilterRegistration() {
        FilterRegistrationBean<IpAllowListFilter> registration = new FilterRegistrationBean<IpAllowListFilter>();
        registration.setFilter(ipAllowListFilter);
        registration.addUrlPatterns("/api/v1/oci8j-connector/*");
        registration.setName("ipAllowListFilter");
        registration.setOrder(0);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<BasicAuthFilter> basicAuthFilterRegistration() {
        FilterRegistrationBean<BasicAuthFilter> registration = new FilterRegistrationBean<BasicAuthFilter>();
        registration.setFilter(basicAuthFilter);
        registration.addUrlPatterns("/api/v1/oci8j-connector/*");
        registration.setName("basicAuthFilter");
        registration.setOrder(1);
        return registration;
    }
}

package com.connectors.oracle8i.security;

import com.connectors.oracle8i.config.EdgeAccessConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * API IP allow-list using resolved client IP (SPEC §7.2).
 * Empty ALLOWED_CIDRS = no filter. Probes exempt.
 */
@Component
public class IpAllowListFilter implements Filter {

    @Autowired
    private EdgeAccessConfig edgeAccessConfig;

    private CidrMatcher trustedProxies;
    private CidrMatcher allowedCidrs;

    @PostConstruct
    void initMatchers() {
        trustedProxies = new CidrMatcher(edgeAccessConfig.getTrustedProxies());
        allowedCidrs = new CidrMatcher(edgeAccessConfig.getAllowedCidrs());
    }

    // package-visible for tests
    void setMatchers(CidrMatcher trusted, CidrMatcher allowed) {
        this.trustedProxies = trusted;
        this.allowedCidrs = allowed;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (allowedCidrs == null || allowedCidrs.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        if (isProbe(uri)) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = ClientIpResolver.resolve(httpRequest, trustedProxies);
        if (allowedCidrs.contains(clientIp)) {
            chain.doFilter(request, response);
            return;
        }

        httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
        httpResponse.setContentType("application/json");
        httpResponse.getWriter().write("{\"code\":403,\"message\":\"Forbidden\"}");
    }

    private static boolean isProbe(String uri) {
        return uri != null && (uri.endsWith("/healthz") || uri.endsWith("/ready") || uri.endsWith("/readyz"));
    }
}

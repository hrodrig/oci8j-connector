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
 * IP allow-lists (SPEC §7.2 / §7.2.1).
 * API routes: ALLOWED_CIDRS. Probes: PROBES_ALLOWED_CIDRS only.
 */
@Component
public class IpAllowListFilter implements Filter {

    @Autowired
    private EdgeAccessConfig edgeAccessConfig;

    private CidrMatcher trustedProxies;
    private CidrMatcher allowedCidrs;
    private CidrMatcher probesAllowedCidrs;

    @PostConstruct
    void initMatchers() {
        trustedProxies = new CidrMatcher(edgeAccessConfig.getTrustedProxies());
        allowedCidrs = new CidrMatcher(edgeAccessConfig.getAllowedCidrs());
        probesAllowedCidrs = new CidrMatcher(edgeAccessConfig.getProbesAllowedCidrs());
    }

    // package-visible for tests
    void setMatchers(CidrMatcher trusted, CidrMatcher allowed) {
        setMatchers(trusted, allowed, new CidrMatcher(""));
    }

    void setMatchers(CidrMatcher trusted, CidrMatcher allowed, CidrMatcher probesAllowed) {
        this.trustedProxies = trusted;
        this.allowedCidrs = allowed;
        this.probesAllowedCidrs = probesAllowed;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String uri = httpRequest.getRequestURI();

        if (ProbePaths.isProbe(uri)) {
            if (probesAllowedCidrs != null && !probesAllowedCidrs.isEmpty()) {
                String clientIp = ClientIpResolver.resolve(httpRequest, trustedProxies);
                if (!probesAllowedCidrs.contains(clientIp)) {
                    forbid(httpResponse);
                    return;
                }
            }
            chain.doFilter(request, response);
            return;
        }

        if (allowedCidrs == null || allowedCidrs.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = ClientIpResolver.resolve(httpRequest, trustedProxies);
        if (allowedCidrs.contains(clientIp)) {
            chain.doFilter(request, response);
            return;
        }

        forbid(httpResponse);
    }

    private static void forbid(HttpServletResponse httpResponse) throws IOException {
        httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
        httpResponse.setContentType("application/json");
        httpResponse.getWriter().write("{\"code\":403,\"message\":\"Forbidden\"}");
    }
}

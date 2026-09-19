package com.connectors.oracle8i.security;

import com.connectors.oracle8i.config.BasicAuthConfig;
import com.connectors.oracle8i.config.EdgeAccessConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Basic Authentication Filter.
 * Probes skip auth when PROBES_PUBLIC=true (default). SPEC §7.2.1.
 */
@Component
public class BasicAuthFilter implements Filter {

    @Autowired
    private BasicAuthConfig basicAuthConfig;

    @Autowired
    private EdgeAccessConfig edgeAccessConfig;

    // package-visible for unit tests
    void setBasicAuthConfig(BasicAuthConfig basicAuthConfig) {
        this.basicAuthConfig = basicAuthConfig;
    }

    void setEdgeAccessConfig(EdgeAccessConfig edgeAccessConfig) {
        this.edgeAccessConfig = edgeAccessConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!basicAuthConfig.isEnabled()) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        if ("/".equals(requestURI) || "/error".equals(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        if (ProbePaths.isProbe(requestURI) && edgeAccessConfig.isProbesPublic()) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            sendUnauthorized(httpResponse);
            return;
        }

        try {
            String encodedCredentials = authHeader.substring(6);
            String credentials = new String(Base64.getDecoder().decode(encodedCredentials), StandardCharsets.UTF_8);
            String[] parts = credentials.split(":", 2);

            if (parts.length != 2) {
                sendUnauthorized(httpResponse);
                return;
            }

            String username = parts[0];
            String password = parts[1];

            if (basicAuthConfig.getUsername().equals(username) &&
                basicAuthConfig.getPassword().equals(password)) {
                chain.doFilter(request, response);
            } else {
                sendUnauthorized(httpResponse);
            }

        } catch (Exception e) {
            sendUnauthorized(httpResponse);
        }
    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "Basic realm=\"Oracle 8i Connector\"");
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Basic authentication required\"}");
    }
}

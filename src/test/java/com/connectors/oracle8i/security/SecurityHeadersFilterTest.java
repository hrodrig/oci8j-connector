package com.connectors.oracle8i.security;

import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

class SecurityHeadersFilterTest {

    @Test
    void setsBaselineHeaders() throws Exception {
        SecurityHeadersFilter filter = new SecurityHeadersFilter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(response).setHeader("X-Content-Type-Options", "nosniff");
        verify(response).setHeader("X-Frame-Options", "DENY");
        verify(response).setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        verify(response).setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        verify(chain).doFilter(request, response);
    }
}

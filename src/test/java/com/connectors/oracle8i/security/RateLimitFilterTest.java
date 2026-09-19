package com.connectors.oracle8i.security;

import com.connectors.oracle8i.config.HardeningConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RateLimitFilter filter;
    private HardeningConfig hardening;
    private FilterChain chain;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter body;

    @BeforeEach
    void setUp() throws Exception {
        filter = new RateLimitFilter();
        hardening = mock(HardeningConfig.class);
        filter.setHardeningConfig(hardening);
        filter.setTrustedProxies(new CidrMatcher(""));
        chain = mock(FilterChain.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        when(request.getRemoteAddr()).thenReturn("203.0.113.1");
        when(request.getRequestURI()).thenReturn("/api/v1/oci8j-connector/query");
    }

    @Test
    void disabledPassesThrough() throws Exception {
        when(hardening.isRateLimitEnabled()).thenReturn(false);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void exceedsMaxReturns429() throws Exception {
        when(hardening.isRateLimitEnabled()).thenReturn(true);
        when(hardening.getRateLimitMax()).thenReturn(2);
        when(hardening.getRateLimitWindowSeconds()).thenReturn(60);

        filter.doFilter(request, response, chain);
        filter.doFilter(request, response, chain);
        filter.doFilter(request, response, chain);

        verify(chain, times(2)).doFilter(request, response);
        verify(response).setStatus(429);
        assertTrue(body.toString().contains("\"code\":429"));
    }

    @Test
    void probesExempt() throws Exception {
        when(hardening.isRateLimitEnabled()).thenReturn(true);
        when(hardening.getRateLimitMax()).thenReturn(1);
        when(hardening.getRateLimitWindowSeconds()).thenReturn(60);
        when(request.getRequestURI()).thenReturn("/api/v1/oci8j-connector/healthz");

        filter.doFilter(request, response, chain);
        filter.doFilter(request, response, chain);

        verify(chain, times(2)).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }
}

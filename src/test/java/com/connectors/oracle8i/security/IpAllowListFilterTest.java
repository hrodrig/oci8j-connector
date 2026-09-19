package com.connectors.oracle8i.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IpAllowListFilterTest {

    private IpAllowListFilter filter;
    private FilterChain chain;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter body;

    @BeforeEach
    void setUp() throws Exception {
        filter = new IpAllowListFilter();
        chain = mock(FilterChain.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));
    }

    @Test
    void emptyAllowListPassesThrough() throws Exception {
        filter.setMatchers(new CidrMatcher(""), new CidrMatcher(""));
        when(request.getRequestURI()).thenReturn("/api/v1/oci8j-connector/query");
        when(request.getRemoteAddr()).thenReturn("8.8.8.8");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void deniesOutsideAllowList() throws Exception {
        filter.setMatchers(new CidrMatcher(""), new CidrMatcher("10.0.0.0/8"));
        when(request.getRequestURI()).thenReturn("/api/v1/oci8j-connector/query");
        when(request.getRemoteAddr()).thenReturn("8.8.8.8");

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(any(), any());
        verify(response).setStatus(403);
        assertTrue(body.toString().contains("\"code\":403"));
    }

    @Test
    void allowsInsideAllowList() throws Exception {
        filter.setMatchers(new CidrMatcher(""), new CidrMatcher("10.0.0.0/8"));
        when(request.getRequestURI()).thenReturn("/api/v1/oci8j-connector/info");
        when(request.getRemoteAddr()).thenReturn("10.1.2.3");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void probeExemptFromApiAllowList() throws Exception {
        filter.setMatchers(new CidrMatcher(""), new CidrMatcher("10.0.0.0/8"));
        when(request.getRequestURI()).thenReturn("/api/v1/oci8j-connector/healthz");
        when(request.getRemoteAddr()).thenReturn("8.8.8.8");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void probeCidrDeniesOutside() throws Exception {
        filter.setMatchers(new CidrMatcher(""), new CidrMatcher(""), new CidrMatcher("10.0.0.0/8"));
        when(request.getRequestURI()).thenReturn("/api/v1/oci8j-connector/readyz");
        when(request.getRemoteAddr()).thenReturn("8.8.8.8");

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(any(), any());
        verify(response).setStatus(403);
    }

    @Test
    void probeCidrAllowsInsideEvenIfApiListDifferent() throws Exception {
        filter.setMatchers(new CidrMatcher(""), new CidrMatcher("192.168.0.0/16"), new CidrMatcher("10.0.0.0/8"));
        when(request.getRequestURI()).thenReturn("/api/v1/oci8j-connector/ready");
        when(request.getRemoteAddr()).thenReturn("10.1.2.3");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void usesResolvedClientIpBehindTrustedProxy() throws Exception {
        filter.setMatchers(new CidrMatcher("10.0.0.0/8"), new CidrMatcher("198.51.100.0/24"));
        when(request.getRequestURI()).thenReturn("/api/v1/oci8j-connector/query");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(request.getHeader("X-Forwarded-For")).thenReturn("198.51.100.50");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}

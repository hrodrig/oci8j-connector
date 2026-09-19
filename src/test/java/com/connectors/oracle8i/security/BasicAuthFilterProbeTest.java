package com.connectors.oracle8i.security;

import com.connectors.oracle8i.config.BasicAuthConfig;
import com.connectors.oracle8i.config.EdgeAccessConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.mockito.Mockito.*;

class BasicAuthFilterProbeTest {

    private BasicAuthFilter filter;
    private BasicAuthConfig basicAuth;
    private EdgeAccessConfig edge;
    private FilterChain chain;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() throws Exception {
        filter = new BasicAuthFilter();
        basicAuth = mock(BasicAuthConfig.class);
        edge = mock(EdgeAccessConfig.class);
        filter.setBasicAuthConfig(basicAuth);
        filter.setEdgeAccessConfig(edge);
        chain = mock(FilterChain.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        when(basicAuth.isEnabled()).thenReturn(true);
        when(basicAuth.getUsername()).thenReturn("user");
        when(basicAuth.getPassword()).thenReturn("pass");
    }

    @Test
    void probesPublicSkipsAuth() throws Exception {
        when(edge.isProbesPublic()).thenReturn(true);
        when(request.getRequestURI()).thenReturn("/api/v1/oci8j-connector/healthz");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(401);
    }

    @Test
    void readyzPublicSkipsAuth() throws Exception {
        when(edge.isProbesPublic()).thenReturn(true);
        when(request.getRequestURI()).thenReturn("/api/v1/oci8j-connector/readyz");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void probesPrivateRequiresAuth() throws Exception {
        when(edge.isProbesPublic()).thenReturn(false);
        when(request.getRequestURI()).thenReturn("/api/v1/oci8j-connector/ready");
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(any(), any());
        verify(response).setStatus(401);
    }

    @Test
    void probesPrivateAcceptsValidBasic() throws Exception {
        when(edge.isProbesPublic()).thenReturn(false);
        when(request.getRequestURI()).thenReturn("/api/v1/oci8j-connector/readyz");
        String token = Base64.getEncoder().encodeToString("user:pass".getBytes(StandardCharsets.UTF_8));
        when(request.getHeader("Authorization")).thenReturn("Basic " + token);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}

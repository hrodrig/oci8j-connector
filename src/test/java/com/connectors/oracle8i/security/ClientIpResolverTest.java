package com.connectors.oracle8i.security;

import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientIpResolverTest {

    @Test
    void emptyTrustedIgnoresForwardedHeaders() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRemoteAddr()).thenReturn("203.0.113.10");
        when(req.getHeader("X-Forwarded-For")).thenReturn("8.8.8.8");
        when(req.getHeader("X-Real-IP")).thenReturn("1.1.1.1");

        CidrMatcher trusted = new CidrMatcher("");
        assertEquals("203.0.113.10", ClientIpResolver.resolve(req, trusted));
    }

    @Test
    void untrustedPeerIgnoresForwardedHeaders() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRemoteAddr()).thenReturn("203.0.113.10");
        when(req.getHeader("X-Forwarded-For")).thenReturn("8.8.8.8");

        CidrMatcher trusted = new CidrMatcher("10.0.0.0/8");
        assertEquals("203.0.113.10", ClientIpResolver.resolve(req, trusted));
    }

    @Test
    void trustedPeerUsesLeftmostAfterStrippingTrusted() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRemoteAddr()).thenReturn("10.0.0.2");
        when(req.getHeader("X-Forwarded-For")).thenReturn("198.51.100.7, 10.0.0.1");

        CidrMatcher trusted = new CidrMatcher("10.0.0.0/8");
        assertEquals("198.51.100.7", ClientIpResolver.resolve(req, trusted));
    }

    @Test
    void trustedPeerFallsBackToXRealIp() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRemoteAddr()).thenReturn("10.0.0.2");
        when(req.getHeader("X-Forwarded-For")).thenReturn(null);
        when(req.getHeader("X-Real-IP")).thenReturn("198.51.100.9");

        CidrMatcher trusted = new CidrMatcher("10.0.0.0/8");
        assertEquals("198.51.100.9", ClientIpResolver.resolve(req, trusted));
    }
}

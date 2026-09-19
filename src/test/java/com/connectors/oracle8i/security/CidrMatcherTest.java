package com.connectors.oracle8i.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CidrMatcherTest {

    @Test
    void emptyMatcherContainsNothing() {
        CidrMatcher m = new CidrMatcher("");
        assertTrue(m.isEmpty());
        assertFalse(m.contains("10.0.0.1"));
    }

    @Test
    void exactIpMatch() {
        CidrMatcher m = new CidrMatcher("10.0.0.5");
        assertTrue(m.contains("10.0.0.5"));
        assertFalse(m.contains("10.0.0.6"));
    }

    @Test
    void ipv4CidrMatch() {
        CidrMatcher m = new CidrMatcher("192.168.1.0/24");
        assertTrue(m.contains("192.168.1.1"));
        assertTrue(m.contains("192.168.1.254"));
        assertFalse(m.contains("192.168.2.1"));
    }

    @Test
    void csvMultiple() {
        CidrMatcher m = new CidrMatcher("10.0.0.0/8, 172.16.0.0/12");
        assertTrue(m.contains("10.1.2.3"));
        assertTrue(m.contains("172.16.5.5"));
        assertFalse(m.contains("8.8.8.8"));
    }
}

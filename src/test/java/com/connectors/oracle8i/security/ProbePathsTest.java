package com.connectors.oracle8i.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProbePathsTest {

    @Test
    void detectsProbeSuffixes() {
        assertTrue(ProbePaths.isProbe("/api/v1/oci8j-connector/healthz"));
        assertTrue(ProbePaths.isProbe("/api/v1/oci8j-connector/ready"));
        assertTrue(ProbePaths.isProbe("/api/v1/oci8j-connector/readyz"));
        assertFalse(ProbePaths.isProbe("/api/v1/oci8j-connector/query"));
        assertFalse(ProbePaths.isProbe(null));
    }
}

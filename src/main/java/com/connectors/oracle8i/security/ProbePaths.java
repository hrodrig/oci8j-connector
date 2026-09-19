package com.connectors.oracle8i.security;

/**
 * Shared probe path detection for filters (SPEC §7.2.1).
 */
public final class ProbePaths {

    private ProbePaths() {
    }

    public static boolean isProbe(String uri) {
        if (uri == null) {
            return false;
        }
        return uri.endsWith("/healthz") || uri.endsWith("/ready") || uri.endsWith("/readyz");
    }
}

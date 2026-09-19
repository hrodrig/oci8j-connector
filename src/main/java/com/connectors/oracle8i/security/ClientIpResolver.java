package com.connectors.oracle8i.security;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolves client IP per SPEC §7.2: peer RemoteAddr; honor XFF / X-Real-IP
 * only when peer is in TRUSTED_PROXIES.
 */
public final class ClientIpResolver {

    private ClientIpResolver() {
    }

    public static String resolve(HttpServletRequest request, CidrMatcher trustedProxies) {
        String peer = request.getRemoteAddr();
        if (peer == null) {
            peer = "";
        }
        if (trustedProxies == null || trustedProxies.isEmpty()) {
            return peer;
        }
        if (!trustedProxies.contains(peer)) {
            return peer;
        }

        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.trim().isEmpty()) {
            List<String> hops = splitHops(xff);
            // Strip trailing hops that are trusted proxies; leftmost remaining = client
            while (!hops.isEmpty() && trustedProxies.contains(hops.get(hops.size() - 1))) {
                hops.remove(hops.size() - 1);
            }
            if (!hops.isEmpty()) {
                return hops.get(0).trim();
            }
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.trim().isEmpty()) {
            return realIp.trim();
        }
        return peer;
    }

    private static List<String> splitHops(String xff) {
        String[] parts = xff.split(",");
        List<String> hops = new ArrayList<String>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) {
                hops.add(t);
            }
        }
        return hops;
    }
}

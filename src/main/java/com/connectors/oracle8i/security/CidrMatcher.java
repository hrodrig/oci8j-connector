package com.connectors.oracle8i.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parses comma-separated CIDRs/IPs and tests membership.
 * IPv4: full CIDR. IPv6 / bare IPs: exact match only.
 */
public final class CidrMatcher {

    private final List<Cidr> cidrs;

    public CidrMatcher(String csv) {
        this.cidrs = parse(csv);
    }

    public boolean isEmpty() {
        return cidrs.isEmpty();
    }

    public boolean contains(String ip) {
        if (ip == null || ip.isEmpty() || cidrs.isEmpty()) {
            return false;
        }
        String normalized = normalizeIp(ip);
        if (normalized == null) {
            return false;
        }
        for (Cidr c : cidrs) {
            if (c.contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> splitCsv(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = csv.split(",");
        List<String> out = new ArrayList<String>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) {
                out.add(t);
            }
        }
        return out;
    }

    private static List<Cidr> parse(String csv) {
        List<Cidr> list = new ArrayList<Cidr>();
        for (String token : splitCsv(csv)) {
            Cidr c = Cidr.parse(token);
            if (c != null) {
                list.add(c);
            }
        }
        return list;
    }

    static String normalizeIp(String ip) {
        String s = ip.trim();
        if (s.startsWith("[") && s.endsWith("]")) {
            s = s.substring(1, s.length() - 1);
        }
        // Strip zone id if present (fe80::1%lo0)
        int zone = s.indexOf('%');
        if (zone >= 0) {
            s = s.substring(0, zone);
        }
        try {
            return InetAddress.getByName(s).getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private static final class Cidr {
        private final byte[] network;
        private final int prefix;
        private final boolean ipv4;

        private Cidr(byte[] network, int prefix, boolean ipv4) {
            this.network = network;
            this.prefix = prefix;
            this.ipv4 = ipv4;
        }

        static Cidr parse(String token) {
            String t = token.trim();
            int slash = t.indexOf('/');
            String addrPart = slash >= 0 ? t.substring(0, slash) : t;
            String norm = normalizeIp(addrPart);
            if (norm == null) {
                return null;
            }
            try {
                InetAddress addr = InetAddress.getByName(norm);
                byte[] bytes = addr.getAddress();
                boolean v4 = bytes.length == 4;
                int max = v4 ? 32 : 128;
                int prefix = max;
                if (slash >= 0) {
                    prefix = Integer.parseInt(t.substring(slash + 1).trim());
                    if (prefix < 0 || prefix > max) {
                        return null;
                    }
                }
                return new Cidr(bytes, prefix, v4);
            } catch (Exception e) {
                return null;
            }
        }

        boolean contains(String normalizedIp) {
            try {
                byte[] addr = InetAddress.getByName(normalizedIp).getAddress();
                if ((addr.length == 4) != ipv4) {
                    return false;
                }
                if (prefix == 0) {
                    return true;
                }
                int fullBytes = prefix / 8;
                int remBits = prefix % 8;
                for (int i = 0; i < fullBytes; i++) {
                    if (network[i] != addr[i]) {
                        return false;
                    }
                }
                if (remBits == 0) {
                    return true;
                }
                int mask = 0xFF << (8 - remBits);
                return (network[fullBytes] & mask) == (addr[fullBytes] & mask);
            } catch (UnknownHostException e) {
                return false;
            }
        }
    }
}

package com.connectors.oracle8i.security;

import com.connectors.oracle8i.config.EdgeAccessConfig;
import com.connectors.oracle8i.config.HardeningConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Per resolved-client-IP rate limit (SPEC §7.3). Probes exempt. 0 max = off.
 */
@Component
public class RateLimitFilter implements Filter {

    @Autowired
    private HardeningConfig hardeningConfig;

    @Autowired
    private EdgeAccessConfig edgeAccessConfig;

    private CidrMatcher trustedProxies;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<String, Window>();

    @PostConstruct
    void init() {
        trustedProxies = new CidrMatcher(edgeAccessConfig.getTrustedProxies());
    }

    void setTrustedProxies(CidrMatcher trustedProxies) {
        this.trustedProxies = trustedProxies;
    }

    void setHardeningConfig(HardeningConfig hardeningConfig) {
        this.hardeningConfig = hardeningConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (hardeningConfig == null || !hardeningConfig.isRateLimitEnabled()) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (ProbePaths.isProbe(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = ClientIpResolver.resolve(httpRequest, trustedProxies);
        long now = System.currentTimeMillis();
        long windowMs = hardeningConfig.getRateLimitWindowSeconds() * 1000L;
        int max = hardeningConfig.getRateLimitMax();

        prune(now, windowMs);

        Window w = windows.get(clientIp);
        if (w == null || now - w.windowStart >= windowMs) {
            w = new Window(now);
            windows.put(clientIp, w);
        }

        if (w.count.incrementAndGet() > max) {
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"code\":429,\"message\":\"Too Many Requests\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private void prune(long now, long windowMs) {
        if (windows.size() < 1000) {
            return;
        }
        Iterator<Map.Entry<String, Window>> it = windows.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Window> e = it.next();
            if (now - e.getValue().windowStart >= windowMs) {
                it.remove();
            }
        }
    }

    private static final class Window {
        final long windowStart;
        final AtomicInteger count = new AtomicInteger(0);

        Window(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}

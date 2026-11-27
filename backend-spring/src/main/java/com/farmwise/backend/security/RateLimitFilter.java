package com.farmwise.backend.security;

import com.farmwise.backend.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Simple in-memory per-IP token-bucket rate limiter.
 * Auth endpoints get a tighter limit; general API endpoints a broader one.
 * Buckets are cleaned up lazily every 10 minutes.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties props;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private volatile long lastCleanup = System.currentTimeMillis();
    private static final long CLEANUP_INTERVAL_MS = 600_000; // 10 minutes

    public RateLimitFilter(RateLimitProperties props) {
        this.props = props;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        boolean isAuthPath = path.startsWith("/api/v1/auth/");
        int limit = isAuthPath ? props.authRequestsPerMinute() : props.apiRequestsPerMinute();
        String bucketKey = clientIp + ":" + (isAuthPath ? "auth" : "api");

        cleanupIfNeeded();

        Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> new Bucket(limit));

        if (!bucket.tryConsume(limit)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void cleanupIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup > CLEANUP_INTERVAL_MS) {
            lastCleanup = now;
            long cutoff = Instant.now().minusSeconds(120).toEpochMilli();
            buckets.entrySet().removeIf(e -> e.getValue().lastAccess < cutoff);
        }
    }

    private static class Bucket {
        private final AtomicInteger tokens;
        private volatile long windowStart;
        volatile long lastAccess;

        Bucket(int limit) {
            this.tokens = new AtomicInteger(limit);
            this.windowStart = System.currentTimeMillis();
            this.lastAccess = windowStart;
        }

        boolean tryConsume(int limit) {
            long now = System.currentTimeMillis();
            lastAccess = now;
            if (now - windowStart > 60_000) {
                // Reset window
                windowStart = now;
                tokens.set(limit);
            }
            return tokens.getAndDecrement() > 0;
        }
    }
}

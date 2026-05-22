package com.omatheusmesmo.shoppmate.auth.configs;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-IP rate limiter for authentication endpoints.
 * Applies to configured HTTP methods and path patterns.
 */
@Component
public class RateLimitFilter  extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public RateLimitFilter(RateLimitProperties properties) {
        this.properties = properties;
    }

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(properties.getCapacity())
                        .refillGreedy(
                                properties.getRefillTokens(),
                                Duration.ofMinutes(
                                        properties.getRefillMinutes()
                                )
                        )
                        .build())
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("RATE LIMIT CHECK: method="
                + request.getMethod()
                + ", path="
                + request.getRequestURI()
                + ", shouldRateLimit="
                + shouldRateLimit(request));

        if (shouldRateLimit(request)) {

            String ip = request.getRemoteAddr();

            Bucket bucket = buckets.computeIfAbsent(
                    ip,
                    k -> createNewBucket()
            );

            if (!bucket.tryConsume(1)) {
                response.setStatus(429);
                response.getWriter().write("Too Many Requests");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
    private boolean shouldRateLimit(HttpServletRequest request) {
        return isRateLimitedMethod(request) && isRateLimitedPath(request);
    }

    private boolean isRateLimitedMethod(HttpServletRequest request) {
        return properties.getEnabledMethods().stream()
                .anyMatch(method -> method.equalsIgnoreCase(request.getMethod()));
    }

    private boolean isRateLimitedPath(HttpServletRequest request) {
        String path = request.getRequestURI();

        return properties.getIncludedPaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}


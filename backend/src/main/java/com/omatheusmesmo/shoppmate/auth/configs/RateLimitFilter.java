package com.omatheusmesmo.shoppmate.auth.configs;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.proxy.RemoteBucketBuilder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);

    private final RateLimitProperties properties;
    private final ProxyManager<Long> proxyManager;
    private final RateLimitViolationTracker violationTracker;
    private final BucketConfiguration bucketConfiguration;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public RateLimitFilter(RateLimitProperties properties,
                           ProxyManager<Long> proxyManager,
                           RateLimitViolationTracker violationTracker) {
        this.properties = properties;
        this.proxyManager = proxyManager;
        this.violationTracker = violationTracker;
        this.bucketConfiguration = createBucketConfiguration();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Only configured methods and paths are rate-limited;
        // all other requests continue through the normal filter chain.
        if (shouldRateLimit(request)) {
            String ip = resolveClientIp(request);

            // Bucket key includes client IP, HTTP method, and matched path group
            // so each client is limited separately per configured endpoint group.
            Long bucketKey = bucketKey(ip, request);

            Optional<Long> activePenaltySeconds = violationTracker.getActivePenaltySeconds(bucketKey);

            if (activePenaltySeconds.isPresent()) {
                long secondsToWait = activePenaltySeconds.get();

                logger.warn("PERMANENT_LIMIT_VIOLATION clientIp={} method={} path={} retryAfterSeconds={}",
                        ip,
                        request.getMethod(),
                        request.getRequestURI(),
                        secondsToWait);

                writeTooManyRequestsResponse(response, secondsToWait);
                return;
            }

            RemoteBucketBuilder<Long> bucketBuilder = proxyManager.builder();

            var bucket = bucketBuilder.build(bucketKey, bucketConfiguration);

            var probe = bucket.tryConsumeAndReturnRemaining(1);

            // Bucket is exhausted, so stop the request before it reaches the controller.
            if (!probe.isConsumed()) {
                long baseSecondsToWait = Math.max(
                        1,
                        TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill())
                );

                long secondsToWait = violationTracker.recordViolationAndCalculatePenaltySeconds(
                        bucketKey,
                        baseSecondsToWait
                );

                logger.warn("PERMANENT_LIMIT_VIOLATION clientIp={} method={} path={} retryAfterSeconds={}",
                        ip,
                        request.getMethod(),
                        request.getRequestURI(),
                        secondsToWait);

                writeTooManyRequestsResponse(response, secondsToWait);
                return;
            }

            // Successful requests reset this client's consecutive violation history.
            violationTracker.resetViolations(bucketKey);
        }

        filterChain.doFilter(request, response);
    }

    private BucketConfiguration createBucketConfiguration() {
        var bucketConfigurationBuilder = BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(properties.getCapacity())
                        .refillGreedy(
                                properties.getRefillTokens(),
                                properties.getRefillDuration()
                        )
                        .build());

        // Optional short-burst limit protects against rapid spikes inside the normal refill window.
        if (isShortBurstRateLimitEnabled()) {
            bucketConfigurationBuilder.addLimit(Bandwidth.builder()
                    .capacity(properties.getShortBurstCapacity())
                    .refillGreedy(
                            properties.getShortBurstRefillTokens(),
                            properties.getShortBurstRefillDuration()
                    )
                    .build());
        }

        return bucketConfigurationBuilder.build();
    }

    private boolean isShortBurstRateLimitEnabled() {
        return properties.isShortBurstEnabled()
                && properties.getShortBurstCapacity() > 0
                && properties.getShortBurstRefillTokens() > 0
                && properties.getShortBurstRefillDuration() != null;
    }

    private void writeTooManyRequestsResponse(HttpServletResponse response, long secondsToWait)
            throws IOException {

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(secondsToWait));
        response.setContentType("application/json");
        response.getWriter().write("""
                {
                  "error": "Too Many Requests",
                  "message": "Rate limit exceeded. Try again later."
                }
                """);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private Long bucketKey(String ip, HttpServletRequest request) {
        String rawKey = ip + ":" + request.getMethod() + ":" + matchedPathGroup(request.getRequestURI());
        return stableLongHash(rawKey);
    }

    private Long stableLongHash(String rawKey) {
        try {
            byte[] digest = MessageDigest
                    .getInstance("SHA-256")
                    .digest(rawKey.getBytes(StandardCharsets.UTF_8));

            return ByteBuffer.wrap(digest).getLong();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private String matchedPathGroup(String path) {
        List<String> includedPaths = properties.getIncludedPaths();

        return includedPaths.stream()
                .filter(pattern -> pathMatcher.match(pattern, path))
                .findFirst()
                .orElse(path);
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
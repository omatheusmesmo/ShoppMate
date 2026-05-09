package com.omatheusmesmo.shoppmate.auth.configs;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter  extends OncePerRequestFilter {
    private final RateLimitProperties properties;
    public RateLimitFilter(RateLimitProperties properties) {
        this.properties = properties;
    }
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

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

        String path = request.getRequestURI();

        if (path.equals("/auth/login")) {

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
}

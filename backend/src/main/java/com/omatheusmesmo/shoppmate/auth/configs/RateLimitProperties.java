package com.omatheusmesmo.shoppmate.auth.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "security.rate-limit")
public class RateLimitProperties {
    private long capacity;

    private long refillTokens;

    private long refillMinutes;

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getRefillTokens() {
        return refillTokens;
    }

    public void setRefillTokens(long refillTokens) {
        this.refillTokens = refillTokens;
    }

    public long getRefillMinutes() {
        return refillMinutes;
    }

    public void setRefillMinutes(long refillMinutes) {
        this.refillMinutes = refillMinutes;
    }
}

package com.omatheusmesmo.shoppmate.auth.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "security.rate-limit")
public class RateLimitProperties {

    private long capacity;

    private long refillTokens;

    private long refillMinutes;
    private List<String> enabledMethods;
    private List<String> includedPaths;

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
    public List<String> getEnabledMethods() {
        return enabledMethods;
    }

    public void setEnabledMethods(List<String> enabledMethods) {
        this.enabledMethods = enabledMethods;
    }

    public List<String> getIncludedPaths() {
        return includedPaths;
    }

    public void setIncludedPaths(List<String> includedPaths) {
        this.includedPaths = includedPaths;
    }
}

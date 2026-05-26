package com.omatheusmesmo.shoppmate.auth.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "security.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    private int penaltyThreshold = 2;

    private int capacity;

    private int refillTokens;

    private Duration refillDuration;

    private List<String> includedPaths;

    private List<String> enabledMethods;

    private boolean shortBurstEnabled = false;

    private int shortBurstCapacity;

    private int shortBurstRefillTokens;

    private Duration shortBurstRefillDuration;
}
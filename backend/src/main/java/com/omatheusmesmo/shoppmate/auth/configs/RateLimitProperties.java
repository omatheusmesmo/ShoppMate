package com.omatheusmesmo.shoppmate.auth.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    private int penaltyThreshold = 2;

    private int capacity;

    private int refillTokens;

    @DurationUnit(ChronoUnit.SECONDS)
    private Duration refillDuration;

    private List<String> includedPaths;

    private List<String> enabledMethods;

    private boolean shortBurstEnabled = false;

    private int shortBurstCapacity;

    private int shortBurstRefillTokens;

    @DurationUnit(ChronoUnit.MILLIS)
    private Duration shortBurstRefillDuration;
}

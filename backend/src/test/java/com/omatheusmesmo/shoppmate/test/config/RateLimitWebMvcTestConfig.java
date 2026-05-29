package com.omatheusmesmo.shoppmate.test.config;

import java.util.Optional;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.context.annotation.Bean;

import io.github.bucket4j.distributed.proxy.ProxyManager;

import com.omatheusmesmo.shoppmate.auth.configs.RateLimitProperties;
import com.omatheusmesmo.shoppmate.auth.configs.RateLimitViolationTracker;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.mock.mockito.MockReset.withSettings;

@TestConfiguration
public class RateLimitWebMvcTestConfig {

    @Bean
    public RateLimitProperties rateLimitProperties() {

        RateLimitProperties properties = new RateLimitProperties();
        properties.setEnabled(false);
        return properties;
    }

    @Bean
    ProxyManager<Long> proxyManager() {
        return mock(ProxyManager.class, withSettings(MockReset.AFTER));
    }

    @Bean
    RateLimitViolationTracker rateLimitViolationTracker() {
        RateLimitViolationTracker tracker = mock(RateLimitViolationTracker.class, withSettings(MockReset.AFTER));

        when(tracker.getActivePenaltySeconds(anyLong())).thenReturn(Optional.empty());

        return tracker;
    }
}

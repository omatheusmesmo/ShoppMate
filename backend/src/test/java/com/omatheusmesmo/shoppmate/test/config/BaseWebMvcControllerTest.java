package com.omatheusmesmo.shoppmate.test.config;

import com.omatheusmesmo.shoppmate.auth.configs.RateLimitProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

@EnableConfigurationProperties(RateLimitProperties.class)
@Import(RateLimitWebMvcTestConfig.class)
public abstract class BaseWebMvcControllerTest {
}
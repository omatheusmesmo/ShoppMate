package com.omatheusmesmo.shoppmate.test.config;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

@Import(RateLimitWebMvcTestConfig.class)
@AutoConfigureMockMvc
public abstract class BaseWebMvcControllerTest {
}

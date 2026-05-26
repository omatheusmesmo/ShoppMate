package com.omatheusmesmo.shoppmate.auth.configs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.omatheusmesmo.shoppmate.shared.testcontainers.AbstractIntegrationTest;
import java.sql.SQLException;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "security.rate-limit.capacity=5",
        "security.rate-limit.refill-tokens=5",
        "security.rate-limit.refill-duration=PT1M",
        "security.rate-limit.enabled-methods[0]=POST",
        "security.rate-limit.enabled-methods[1]=PUT",
        "security.rate-limit.included-paths[0]=/auth/**",
        "security.rate-limit.included-paths[1]=/category",
        "security.rate-limit.included-paths[2]=/category/**",
        "security.rate-limit.included-paths[3]=/lists",
        "security.rate-limit.included-paths[4]=/lists/**",
        "security.rate-limit.included-paths[5]=/item",
        "security.rate-limit.included-paths[6]=/item/**",
        "security.rate-limit.included-paths[7]=/unit",
        "security.rate-limit.included-paths[8]=/unit/**"
})

class RateLimitFilterIntegrationTest extends AbstractIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearRateLimitBuckets() throws SQLException {
        jdbcTemplate.update("DELETE FROM bucket");
    }

    private void assertRateLimitedOnPost(String endpoint, String payload, String ip) throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post(endpoint)
                            .contentType(APPLICATION_JSON)
                            .content(payload)
                            .with(request -> {
                                request.setRemoteAddr(ip);
                                return request;
                            }))
                    .andExpect(result -> {
                        if (result.getResponse().getStatus() == 429) {
                            throw new AssertionError("Rate limit triggered too early for " + endpoint);
                        }
                    });
        }

        mockMvc.perform(post(endpoint)
                        .contentType(APPLICATION_JSON)
                        .content(payload)
                        .with(request -> {
                            request.setRemoteAddr(ip);
                            return request;
                        }))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));
    }

    private void assertRateLimitedOnPut(String endpoint, String payload, String ip) throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(put(endpoint)
                            .contentType(APPLICATION_JSON)
                            .content(payload)
                            .with(request -> {
                                request.setRemoteAddr(ip);
                                return request;
                            }))
                    .andExpect(result -> {
                        if (result.getResponse().getStatus() == 429) {
                            throw new AssertionError("Rate limit triggered too early for " + endpoint);
                        }
                    });
        }

        mockMvc.perform(put(endpoint)
                        .contentType(APPLICATION_JSON)
                        .content(payload)
                        .with(request -> {
                            request.setRemoteAddr(ip);
                            return request;
                        }))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON));
    }

    @Test
    void shouldReturnTooManyRequestsWhenLoginRateLimitExceeded() throws Exception {
        assertRateLimitedOnPost("/auth/login", """
                {
                    "email": "wrong@test.com",
                    "password": "wrong-password"
                }
                """, "10.0.0.1");
    }

    @Test
    void shouldReturnTooManyRequestsWhenSignRateLimitExceeded() throws Exception {
        assertRateLimitedOnPost("/auth/sign", """
                {
                    "name": "Test User",
                    "email": "test-sign@test.com",
                    "password": "wrong-password"
                }
                """, "10.0.0.2");
    }

    @Test
    void shouldReturnTooManyRequestsWhenCategoryPostRateLimitExceeded() throws Exception {
        assertRateLimitedOnPost("/category", """
                {
                    "name": "Test Category"
                }
                """, "10.0.0.3");
    }

    @Test
    void shouldReturnTooManyRequestsWhenCategoryPutRateLimitExceeded() throws Exception {
        assertRateLimitedOnPut("/category/1", """
                {
                    "name": "Updated Category"
                }
                """, "10.0.0.4");
    }

    @Test
    void shouldReturnTooManyRequestsWhenListPostRateLimitExceeded() throws Exception {
        assertRateLimitedOnPost("/lists", """
                {
                    "name": "Test List"
                }
                """, "10.0.0.5");
    }

    @Test
    void shouldReturnTooManyRequestsWhenListPutRateLimitExceeded() throws Exception {
        assertRateLimitedOnPut("/lists/1", """
                {
                    "name": "Updated List"
                }
                """, "10.0.0.6");
    }

    @Test
    void shouldReturnTooManyRequestsWhenItemPostRateLimitExceeded() throws Exception {
        assertRateLimitedOnPost("/item", """
                {
                    "name": "Test Item"
                }
                """, "10.0.0.7");
    }

    @Test
    void shouldReturnTooManyRequestsWhenItemPutRateLimitExceeded() throws Exception {
        assertRateLimitedOnPut("/item/1", """
                {
                    "name": "Updated Item"
                }
                """, "10.0.0.8");
    }

    @Test
    void shouldReturnTooManyRequestsWhenUnitPostRateLimitExceeded() throws Exception {
        assertRateLimitedOnPost("/unit", """
                {
                    "name": "Test Unit"
                }
                """, "10.0.0.9");
    }

    @Test
    void shouldReturnTooManyRequestsWhenUnitPutRateLimitExceeded() throws Exception {
        assertRateLimitedOnPut("/unit/1", """
                {
                    "name": "Updated Unit"
                }
                """, "10.0.0.10");
    }
}
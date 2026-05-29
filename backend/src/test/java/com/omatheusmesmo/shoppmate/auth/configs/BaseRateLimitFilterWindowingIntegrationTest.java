package com.omatheusmesmo.shoppmate.auth.configs;

import com.omatheusmesmo.shoppmate.shared.testcontainers.AbstractIntegrationTest;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.service.UnitService;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public abstract class BaseRateLimitFilterWindowingIntegrationTest extends AbstractIntegrationTest {

    protected static final String UNIT_ENDPOINT = "/unit";
    protected static final String RATE_LIMIT_TEST_USER_EMAIL = "rate-limit-test-user@test.com";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UnitService unitService;

    @Autowired
    protected UserRepository userRepository;

    protected Unit persistedUnit;

    protected User rateLimitTestUser;

    @BeforeEach
    void baseSetUp() {
        rateLimitTestUser = userRepository.findByEmail(RATE_LIMIT_TEST_USER_EMAIL).orElseGet(() -> {
            User user = new User();
            user.setFullName("Rate Limit Test User");
            user.setEmail(RATE_LIMIT_TEST_USER_EMAIL);
            user.setPassword("password");
            return userRepository.save(user);
        });

        persistedUnit = createPersistedUnit();
    }

    protected Unit createPersistedUnit() {
        Unit unit = new Unit();
        unit.setName("Original Unit " + UUID.randomUUID());
        unit.setSymbol("orig");
        unit.setOwner(rateLimitTestUser);

        return unitService.saveUnit(unit);
    }

    protected void performAllowedPut(String ipAddress) throws Exception {
        mockMvc.perform(put(UNIT_ENDPOINT + "/" + persistedUnit.getId()).with(csrf())
                .with(authentication(authenticationToken())).with(request -> {
                    request.setRemoteAddr(ipAddress);
                    return request;
                }).contentType(MediaType.APPLICATION_JSON).content(unitUpdatePayload())).andExpect(status().isOk());
    }

    protected void performBlockedPut(String ipAddress) throws Exception {
        mockMvc.perform(put(UNIT_ENDPOINT + "/" + persistedUnit.getId()).with(csrf())
                .with(authentication(authenticationToken())).with(request -> {
                    request.setRemoteAddr(ipAddress);
                    return request;
                }).contentType(MediaType.APPLICATION_JSON).content(unitUpdatePayload()))
                .andExpect(status().isTooManyRequests());
    }

    protected UsernamePasswordAuthenticationToken authenticationToken() {
        return new UsernamePasswordAuthenticationToken(rateLimitTestUser, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    protected String unitUpdatePayload() {
        return """
                {
                  "id": %d,
                  "name": "%s",
                  "symbol": "%s"
                }
                """.formatted(persistedUnit.getId(), "Updated Unit " + UUID.randomUUID(), "upd");
    }
}

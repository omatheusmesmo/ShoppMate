package com.omatheusmesmo.shoppmate.auth.configs;

import com.omatheusmesmo.shoppmate.shared.testcontainers.AbstractIntegrationTest;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.service.UnitService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public abstract class BaseRateLimitFilterWindowingIntegrationTest extends AbstractIntegrationTest {

    protected static final String UNIT_ENDPOINT = "/unit";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UnitService unitService;

    protected Unit persistedUnit;

    @BeforeEach
    void baseSetUp() {
        persistedUnit = createPersistedUnit();
    }

    protected Unit createPersistedUnit() {
        Unit unit = new Unit();
        unit.setName("Original Unit " + UUID.randomUUID());
        unit.setSymbol("orig");

        return unitService.saveUnit(unit);
    }

    protected void performAllowedPut(String ipAddress) throws Exception {
        mockMvc.perform(
                        put(UNIT_ENDPOINT)
                                .with(csrf())
                                .with(SecurityMockMvcRequestPostProcessors.user("rate-limit-test-user"))
                                .with(request -> {
                                    request.setRemoteAddr(ipAddress);
                                    return request;
                                })
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(unitUpdatePayload())
                )
                .andExpect(status().isOk());
    }

    protected void performBlockedPut(String ipAddress) throws Exception {
        mockMvc.perform(
                        put(UNIT_ENDPOINT)
                                .with(csrf())
                                .with(SecurityMockMvcRequestPostProcessors.user("rate-limit-test-user"))
                                .with(request -> {
                                    request.setRemoteAddr(ipAddress);
                                    return request;
                                })
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(unitUpdatePayload())
                )
                .andExpect(status().isTooManyRequests());
    }

    protected String unitUpdatePayload() {
        return """
            {
              "id": %d,
              "name": "%s",
              "symbol": "%s"
            }
            """.formatted(
                persistedUnit.getId(),
                "Updated Unit " + UUID.randomUUID(),
                "upd"
        );
    }
}
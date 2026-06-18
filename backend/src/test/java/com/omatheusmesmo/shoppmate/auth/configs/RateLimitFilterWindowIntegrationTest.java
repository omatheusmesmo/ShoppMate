package com.omatheusmesmo.shoppmate.auth.configs;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.omatheusmesmo.shoppmate.auth.repository.RateLimitViolationRepository;
import com.omatheusmesmo.shoppmate.shared.testcontainers.AbstractIntegrationTest;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.service.UnitService;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.repository.UserRepository;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = { "security.rate-limit.capacity=2", "security.rate-limit.refill-tokens=2",
        "security.rate-limit.refill-duration=PT2S", "security.rate-limit.enabled-methods[0]=PUT",
        "security.rate-limit.included-paths[0]=/unit", "security.rate-limit.included-paths[1]=/unit/**" })

class RateLimitFilterWindowingIntegrationTest extends AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilterWindowingIntegrationTest.class);

    private static final String TEST_USER = "test-user@shoppmate.com";
    private static final String ENDPOINT = "/unit";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UnitService unitService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RateLimitViolationRepository rateLimitViolationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Unit persistedUnit;
    private User testUser;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM bucket");
        rateLimitViolationRepository.deleteAll();

        /*
         * Clear dependent rows first so the unique constraint on units.name cannot leak from one test method into the
         * next.
         */
        jdbcTemplate.update("DELETE FROM list_items");
        jdbcTemplate.update("DELETE FROM items");
        jdbcTemplate.update("DELETE FROM units");

        testUser = userRepository.findByEmail(TEST_USER).orElseGet(() -> {
            User user = new User();
            user.setFullName("Rate Limit Window Test User");
            user.setEmail(TEST_USER);
            user.setPassword("password");
            return userRepository.save(user);
        });

        persistedUnit = createAndReloadValidUnit();

        log.info("Test setup complete. bucketRows={}, unitId={}, unitName={}, unitSymbol={}", bucketRowCount(),
                persistedUnit.getId(), persistedUnit.getName(), persistedUnit.getSymbol());
    }

    @Test
    void shouldPersistBucketStateAndBlockRequestBeforeGreedyRefillRestoresFullToken() throws Exception {
        String ip = "10.0.2.1";
        String payload = unitUpdatePayload();

        Instant start = Instant.now();
        long benchmarkStart = System.nanoTime();

        log.info(
                "WINDOW TEST START - ip={}, endpoint={}, capacity=2, refillTokens=2, refillDuration=PT2S, initialBucketRows={}",
                ip, ENDPOINT, bucketRowCount());

        performSuccessfulPut(ENDPOINT, payload, ip, "request-1 consumes token 1 and updates unit", start);
        assertBucketStateWasPersisted("after request-1");

        performSuccessfulPut(ENDPOINT, payload, ip,
                "request-2 consumes token 2 and updates unit; bucket should now be empty", start);
        assertBucketStateWasPersisted("after request-2");

        long beforeSleepNanos = System.nanoTime();

        log.info(
                "Sleeping 500ms - intentionally BEFORE greedy refill restores one full token. bucketRowsBeforeSleep={}",
                bucketRowCount());

        Thread.sleep(500);

        long afterSleepNanos = System.nanoTime();

        log.info(
                "Sleep completed. requestedSleepMs=500, actualSleepMs={}, elapsedSinceTestStartMs={}, bucketRowsAfterSleep={}",
                toMillis(afterSleepNanos - beforeSleepNanos), elapsedMillis(start), bucketRowCount());

        log.info(
                "Sending request-3 at {}ms; expected=429 because greedy refill should not have restored one full token yet",
                elapsedMillis(start));

        long blockedRequestStart = System.nanoTime();

        mockMvc.perform(
                put(ENDPOINT + "/" + persistedUnit.getId()).with(csrf()).with(authentication(authenticationToken()))
                        .contentType(APPLICATION_JSON).content(payload).with(request -> {
                            request.setRemoteAddr(ip);
                            return request;
                        }))
                .andExpect(result -> {
                    long blockedRequestEnd = System.nanoTime();
                    int responseStatus = result.getResponse().getStatus();

                    log.info(
                            "request-3 completed. status={}, requestDurationMs={}, elapsedSinceTestStartMs={}, bucketRows={}",
                            responseStatus, toMillis(blockedRequestEnd - blockedRequestStart), elapsedMillis(start),
                            bucketRowCount());

                    status().isTooManyRequests().match(result);
                });

        assertBucketStateWasPersisted("after blocked request-3");

        long benchmarkEnd = System.nanoTime();

        log.info(
                "SUCCESS - request-3 was blocked before greedy refill restored a full token. totalTestDurationMs={}, elapsedWallClockMs={}, finalBucketRows={}",
                toMillis(benchmarkEnd - benchmarkStart), elapsedMillis(start), bucketRowCount());
    }

    @Test
    void shouldPersistBucketStateAndAllowRequestAfterGreedyRefillRestoresFullToken() throws Exception {
        String ip = "10.0.2.2";
        String payload = unitUpdatePayload();

        Instant start = Instant.now();

        log.info(
                "WINDOW TEST START - ip={}, endpoint={}, capacity=2, refillTokens=2, refillDuration=PT2S, initialBucketRows={}",
                ip, ENDPOINT, bucketRowCount());

        performSuccessfulPut(ENDPOINT, payload, ip, "request-1 consumes token 1 and updates unit", start);
        assertBucketStateWasPersisted("after request-1");

        performSuccessfulPut(ENDPOINT, payload, ip,
                "request-2 consumes token 2 and updates unit; bucket should now be empty", start);
        assertBucketStateWasPersisted("after request-2");

        log.info(
                "Sleeping 1200ms - intentionally AFTER greedy refill should restore one full token. bucketRowsBeforeSleep={}",
                bucketRowCount());

        Thread.sleep(1200);

        log.info(
                "Sending request-3 at {}ms; expected=200 because greedy refill should have restored one token. bucketRowsAfterSleep={}",
                elapsedMillis(start), bucketRowCount());

        mockMvc.perform(
                put(ENDPOINT + "/" + persistedUnit.getId()).with(csrf()).with(authentication(authenticationToken()))
                        .contentType(APPLICATION_JSON).content(payload).with(request -> {
                            request.setRemoteAddr(ip);
                            return request;
                        }))
                .andExpect(result -> {
                    int responseStatus = result.getResponse().getStatus();

                    log.info("request-3 completed at {}ms with status={}, bucketRows={}", elapsedMillis(start),
                            responseStatus, bucketRowCount());
                }).andExpect(status().isOk());

        assertBucketStateWasPersisted("after allowed request-3");

        log.info(
                "SUCCESS - request-3 was allowed after greedy refill restored a full token. elapsed={}ms, finalBucketRows={}",
                elapsedMillis(start), bucketRowCount());
    }

    private Unit createAndReloadValidUnit() {
        Unit unit = new Unit();
        unit.setName("Original Unit");
        unit.setSymbol("orig");
        unit.setOwner(testUser);

        Unit savedUnit = unitService.saveUnit(unit);

        return unitService.findUnitById(savedUnit.getId()).orElseThrow();
    }

    private String unitUpdatePayload() {
        return """
                {
                    "id": %d,
                    "name": "%s",
                    "symbol": "%s"
                }
                """.formatted(persistedUnit.getId(), persistedUnit.getName(), persistedUnit.getSymbol());
    }

    private void performSuccessfulPut(String endpoint, String payload, String ip, String description, Instant start)
            throws Exception {

        log.info("{} at {}ms. bucketRowsBeforeRequest={}", description, elapsedMillis(start), bucketRowCount());

        mockMvc.perform(
                put(endpoint + "/" + persistedUnit.getId()).with(csrf()).with(authentication(authenticationToken()))
                        .contentType(APPLICATION_JSON).content(payload).with(request -> {
                            request.setRemoteAddr(ip);
                            return request;
                        }))
                .andExpect(result -> {
                    int responseStatus = result.getResponse().getStatus();

                    log.info("{} completed at {}ms with status={}. bucketRowsAfterRequest={}", description,
                            elapsedMillis(start), responseStatus, bucketRowCount());

                    if (responseStatus == 429) {
                        throw new AssertionError("Rate limit triggered too early during: " + description);
                    }
                }).andExpect(status().isOk());
    }

    private UsernamePasswordAuthenticationToken authenticationToken() {
        return new UsernamePasswordAuthenticationToken(testUser, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private void assertBucketStateWasPersisted(String checkpoint) {
        Integer rowCount = bucketRowCount();

        log.info("DB CHECK - checkpoint={}, bucketRows={}", checkpoint, rowCount);

        if (rowCount == null || rowCount == 0) {
            throw new AssertionError(
                    "Expected Bucket4j state to be persisted in Postgres at checkpoint: " + checkpoint);
        }
    }

    private Integer bucketRowCount() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM bucket", Integer.class);
    }

    private long elapsedMillis(Instant start) {
        return Duration.between(start, Instant.now()).toMillis();
    }

    private double toMillis(long nanos) {
        return nanos / 1_000_000.0;
    }
}

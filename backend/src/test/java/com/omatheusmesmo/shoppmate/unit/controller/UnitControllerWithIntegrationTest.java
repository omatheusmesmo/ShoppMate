package com.omatheusmesmo.shoppmate.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.item.repository.ItemRepository;
import com.omatheusmesmo.shoppmate.shared.testcontainers.AbstractIntegrationTest;
import com.omatheusmesmo.shoppmate.shared.testcontainers.utils.TestUserFactory;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.repository.UnitRepository;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.repository.UserRepository;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UnitControllerWithIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestUserFactory testUserFactory;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RequestSpecification specification;

    @Autowired
    private ObjectMapper objectMapper;

    private User authenticatedUser;

    @BeforeEach
    void init() {
        jdbcTemplate.update("DELETE FROM bucket");
        jdbcTemplate.update("DELETE FROM rate_limit_violation");

        itemRepository.deleteAll();
        unitRepository.deleteAll();

        String jwtToken = testUserFactory.createTokenForTestUser();

        authenticatedUser = userRepository.findByEmail("user@gmail.com").orElseThrow();

        String csrfToken = given().port(port).header("Authorization", "Bearer " + jwtToken).when().get("/unit").then()
                .statusCode(200).extract().cookie("XSRF-TOKEN");

        specification = new RequestSpecBuilder().setPort(port).setBasePath("/unit")
                .addHeader("Authorization", "Bearer " + jwtToken).addHeader("X-XSRF-TOKEN", csrfToken)
                .addCookie("XSRF-TOKEN", csrfToken).setContentType(ContentType.JSON).log(LogDetail.ALL).build();
    }

    @Test
    void testPutEditUnitWhenUnitExistsShouldReturnOkAndUpdateUnit() throws Exception {
        Unit existingUnit = createUnitToTest("Original Unit", "orig");

        Unit request = new Unit();
        request.setId(existingUnit.getId());
        request.setName("Updated Unit");
        request.setSymbol("upd");

        String content = given(specification).contentType(MediaType.APPLICATION_JSON_VALUE).body(request).when()
                .put("/" + existingUnit.getId()).then().statusCode(200).contentType(MediaType.APPLICATION_JSON_VALUE)
                .extract().body().asString();

        Unit response = objectMapper.readValue(content, Unit.class);

        assertEquals(existingUnit.getId(), response.getId());
        assertEquals("Updated Unit", response.getName());
        assertEquals("upd", response.getSymbol());

        Unit updatedUnit = unitRepository.findById(existingUnit.getId()).orElseThrow();

        assertEquals("Updated Unit", updatedUnit.getName());
        assertEquals("upd", updatedUnit.getSymbol());
        assertFalse(updatedUnit.getDeleted());
        assertNotNull(updatedUnit.getUpdatedAt());
    }

    @Test
    void testPutEditUnitWhenUnitDoesNotExistShouldReturnNotFound() {
        Unit request = new Unit();
        request.setId(999L);
        request.setName("Missing Unit");
        request.setSymbol("missing");

        given(specification).contentType(MediaType.APPLICATION_JSON_VALUE).body(request).when().put("/999").then()
                .statusCode(404);
    }

    @Test
    void testPutEditUnitWhenNameIsBlankShouldReturnBadRequest() {
        Unit existingUnit = createUnitToTest("Original Unit", "orig");

        Unit request = new Unit();
        request.setId(existingUnit.getId());
        request.setName(" ");
        request.setSymbol("upd");

        given(specification).contentType(MediaType.APPLICATION_JSON_VALUE).body(request).when()
                .put("/" + existingUnit.getId()).then().statusCode(400);
    }

    @Test
    void testPutEditUnitWhenSymbolIsBlankShouldReturnBadRequest() {
        Unit existingUnit = createUnitToTest("Original Unit", "orig");

        Unit request = new Unit();
        request.setId(existingUnit.getId());
        request.setName("Updated Unit");
        request.setSymbol(" ");

        given(specification).contentType(MediaType.APPLICATION_JSON_VALUE).body(request).when()
                .put("/" + existingUnit.getId()).then().statusCode(400);
    }

    private Unit createUnitToTest(String name, String symbol) {
        Unit unit = new Unit();
        unit.setName(name);
        unit.setSymbol(symbol);
        unit.setOwner(authenticatedUser);
        return unitRepository.save(unit);
    }
}

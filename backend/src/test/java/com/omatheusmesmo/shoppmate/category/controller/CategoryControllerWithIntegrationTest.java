package com.omatheusmesmo.shoppmate.category.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.shared.testcontainers.AbstractIntegrationTest;
import com.omatheusmesmo.shoppmate.shared.testcontainers.utils.TestUserFactory;
import com.omatheusmesmo.shoppmate.category.dto.CategoryRequestDTO;
import com.omatheusmesmo.shoppmate.category.dto.CategoryResponseDTO;
import com.omatheusmesmo.shoppmate.config.TestConfigs;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CategoryControllerWithIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestUserFactory testUserFactory;

    private static RequestSpecification specification;
    private static ObjectMapper objectMapper;

    static CategoryResponseDTO categoryResponseDTOCreated;
    static CategoryResponseDTO categoryResponseDTOUpdated;

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @BeforeEach
    void init() {
        String jwtToken = testUserFactory.createTokenForTestUser();

        Response response = given()
                .port(TestConfigs.SERVER_PORT)
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/category")
                .then()
                .statusCode(200)
                .extract().response();

        String csrfToken = response.cookie("XSRF-TOKEN");

        specification = new RequestSpecBuilder()
                .setPort(TestConfigs.SERVER_PORT)
                .setBasePath("/category")
                .addHeader("Authorization", "Bearer " + jwtToken)
                .addHeader("X-XSRF-TOKEN", csrfToken)
                .addCookie("XSRF-TOKEN", csrfToken)
                .setContentType(ContentType.JSON)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
    }

    @Test
    @Order(1)
    void testPostAddCategory() throws Exception {
        CategoryRequestDTO request = new CategoryRequestDTO("Book");

        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post()
                .then()
                .statusCode(201)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .extract()
                .body()
                .asString();

        CategoryResponseDTO createdCategory = objectMapper.readValue(content, CategoryResponseDTO.class);
        categoryResponseDTOCreated = createdCategory;

        assertNotNull(createdCategory.id());
        assertTrue(createdCategory.id() > 0);
        assertEquals("Book", createdCategory.name());
    }

    @Test
    @Order(2)
    void testPutEditCategory() throws Exception {
        CategoryRequestDTO categoryResponseDTOToUpdated = new CategoryRequestDTO("Book Putted");

        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", categoryResponseDTOCreated.id())
                .body(categoryResponseDTOToUpdated)
                .when()
                .put("{id}")
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .extract()
                .body()
                .asString();

        CategoryResponseDTO updatedCategory = objectMapper.readValue(content, CategoryResponseDTO.class);
        categoryResponseDTOUpdated = updatedCategory;

        assertNotNull(updatedCategory.id());
        assertTrue(updatedCategory.id() > 0);
        assertEquals("Book Putted", updatedCategory.name());
    }

    @Test
    @Order(3)
    void testGetAllCategories() throws Exception {
        var content = given(specification)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get()
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .extract()
                .body()
                .asString();

        List<CategoryResponseDTO> categories = objectMapper.readValue(content, new TypeReference<List<CategoryResponseDTO>>(){});
        CategoryResponseDTO categoryOne = categories.get(0);

        assertNotNull(categoryOne.id());
        assertTrue(categoryOne.id() > 0);

        assertEquals("Book Putted", categoryOne.name());
    }

    @Test
    @Order(4)
    void testDeleteRemoveCategory() {
        given(specification)
                .pathParam("id", categoryResponseDTOCreated.id())
                .when()
                .delete("{id}")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(5)
    void IntegrationTestPostAddCategory_BadRequest() throws Exception {
        CategoryRequestDTO invalidItem = new CategoryRequestDTO("");

        given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(invalidItem)
                .when()
                .post()
                .then()
                .statusCode(400)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .extract()
                .body()
                .asString();
    }

    @Test
    @Order(6)
    void IntegrationTestPutEditCategory_NotFound() throws Exception {
        CategoryRequestDTO invalidItem = new CategoryRequestDTO("Toy");

        given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", 999L)
                .body(invalidItem)
                .when()
                .put("/{id}")
                .then()
                .statusCode(404)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .extract()
                .body()
                .asString();
    }
}
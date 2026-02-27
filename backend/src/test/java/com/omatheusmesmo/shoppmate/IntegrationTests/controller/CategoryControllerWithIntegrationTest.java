package com.omatheusmesmo.shoppmate.IntegrationTests.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.IntegrationTests.testcontainers.AbstractIntegrationTest;
import com.omatheusmesmo.shoppmate.IntegrationTests.utils.TestUserFactory;
import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import com.omatheusmesmo.shoppmate.category.controller.CategoryController;
import com.omatheusmesmo.shoppmate.category.dto.CategoryRequestDTO;
import com.omatheusmesmo.shoppmate.category.dto.CategoryResponseDTO;
import com.omatheusmesmo.shoppmate.config.TestConfigs;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CategoryControllerWithIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private TestUserFactory testUserFactory;

    private static RequestSpecification specification;
    private static ObjectMapper objectMapper;

    static CategoryResponseDTO categoryResponseDTOCreated;
    static CategoryResponseDTO categoryResponseDTOUpdated;

    private String obtainAccessToken() {
        testUserFactory.createUserIfNotExists();

        var userDetails = userDetailsService
                .loadUserByUsername("user@gmail.com");

        return jwtService.generateToken(userDetails);
    }

    @BeforeEach
    void init() {
        testUserFactory.createUserIfNotExists();
        String token = obtainAccessToken();

        specification = new RequestSpecBuilder()
                .setBasePath("/category")
                .setPort(TestConfigs.SERVER_PORT)
                .addHeader("Authorization", "Bearer " + token)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
    }

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
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
    void testDeleteRemoveCategory() throws Exception {
        given(specification)
                .pathParam("id", categoryResponseDTOCreated.id())
                .when()
                .delete("{id}")
                .then()
                .statusCode(204);
    }
}
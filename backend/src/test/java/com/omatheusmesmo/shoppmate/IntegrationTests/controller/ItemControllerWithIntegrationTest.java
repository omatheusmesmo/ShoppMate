package com.omatheusmesmo.shoppmate.IntegrationTests.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.IntegrationTests.testcontainers.AbstractIntegrationTest;
import com.omatheusmesmo.shoppmate.IntegrationTests.utils.TestUserFactory;
import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import com.omatheusmesmo.shoppmate.config.TestConfigs;
import com.omatheusmesmo.shoppmate.item.dto.ItemRequestDTO;
import com.omatheusmesmo.shoppmate.item.dto.ItemResponseDTO;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.repository.UnitRepository;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ItemControllerWithIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private TestUserFactory testUserFactory;

    @Autowired
    private UnitRepository unitRepository;

    private static RequestSpecification specification;
    private static ObjectMapper objectMapper;

    static ItemResponseDTO itemResponseDTOCreated;
    static ItemResponseDTO itemResponseDTOUpdated;

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
                .setBasePath("/item")
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
    void testPostAddItem() throws Exception {
        Unit unitEntity = new Unit();
        unitEntity.setSymbol("KG");
        unitEntity.setName("Kilogram");
        unitEntity = unitRepository.save(unitEntity);

        ItemRequestDTO requestDTO = new ItemRequestDTO("Feijão", 1L, unitEntity.getId());

        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(requestDTO)
                .when()
                .post()
                .then()
                .statusCode(201)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .extract()
                .body()
                .asString();

        ItemResponseDTO createdItem = objectMapper.readValue(content, ItemResponseDTO.class);
        itemResponseDTOCreated = createdItem;

        assertNotNull(createdItem.id());
        assertTrue(createdItem.id() > 0);

        assertEquals("Feijão", createdItem.name());
        assertEquals(1L, createdItem.category().id());
        assertEquals(1L, createdItem.unit().id());
    }

    @Test
    @Order(2)
    void testPutEditItem() throws Exception {
        ItemRequestDTO ItemRequestDTOToUpdated = new ItemRequestDTO("Arroz", 1L, 1L);

        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", itemResponseDTOCreated.id())
                .body(ItemRequestDTOToUpdated)
                .when()
                .put("{id}")
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .extract()
                .body()
                .asString();

        ItemResponseDTO updatedItem = objectMapper.readValue(content, ItemResponseDTO.class);
        itemResponseDTOUpdated = updatedItem;

        assertNotNull(updatedItem.id());
        assertTrue(updatedItem.id() > 0);

        assertEquals("Arroz", updatedItem.name());
        assertEquals(1L, updatedItem.category().id());
        assertEquals(1L, updatedItem.unit().id());
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

        List<ItemResponseDTO> itens = objectMapper.readValue(content, new TypeReference<List<ItemResponseDTO>>(){});
        ItemResponseDTO itemOne = itens.get(0);

        assertNotNull(itemOne.id());
        assertTrue(itemOne.id() > 0);

        assertEquals("Arroz", itemOne.name());
    }

    @Test
    @WithMockUser
    @Order(4)
    void testDeleteRemoveCategory() throws Exception {
        given(specification)
                .pathParam("id", itemResponseDTOCreated.id())
                .when()
                .delete("{id}")
                .then()
                .statusCode(204);
    }
}

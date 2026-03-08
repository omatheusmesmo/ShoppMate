package com.omatheusmesmo.shoppmate.item.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.category.repository.CategoryRepository;
import com.omatheusmesmo.shoppmate.item.entity.Item;
import com.omatheusmesmo.shoppmate.item.repository.ItemRepository;
import com.omatheusmesmo.shoppmate.shared.testcontainers.AbstractIntegrationTest;
import com.omatheusmesmo.shoppmate.shared.testcontainers.utils.TestUserFactory;
import com.omatheusmesmo.shoppmate.config.TestConfigs;
import com.omatheusmesmo.shoppmate.item.dto.ItemRequestDTO;
import com.omatheusmesmo.shoppmate.item.dto.ItemResponseDTO;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.repository.UnitRepository;
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
class ItemControllerWithIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestUserFactory testUserFactory;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ItemRepository itemRepository;

    private static RequestSpecification specification;
    private static ObjectMapper objectMapper;

    static ItemResponseDTO itemResponseDTOCreated;
    static ItemResponseDTO itemResponseDTOUpdated;

    @BeforeEach
    void init() {
        itemRepository.deleteAll();
        unitRepository.deleteAll();
        categoryRepository.deleteAll();

        String jwtToken = testUserFactory.createTokenForTestUser();

        Response response = given()
                .port(TestConfigs.SERVER_PORT)
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/item")
                .then()
                .statusCode(200)
                .extract().response();

        String csrfToken = response.cookie("XSRF-TOKEN");

        specification = new RequestSpecBuilder()
                .setPort(TestConfigs.SERVER_PORT)
                .setBasePath("/item")
                .addHeader("Authorization", "Bearer " + jwtToken)
                .addHeader("X-XSRF-TOKEN", csrfToken)
                .addCookie("XSRF-TOKEN", csrfToken)
                .setContentType(ContentType.JSON)
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
    void testPostAddItem() throws Exception {
        Category categoryEntity = createCategoryToTest();
        Unit unitEntity = createUnitToTest();

        ItemRequestDTO requestDTO = new ItemRequestDTO("Feijão", categoryEntity.getId(), unitEntity.getId());

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
        assertEquals(categoryEntity.getId(), createdItem.category().id());
        assertEquals(unitEntity.getId(), createdItem.unit().id());
    }

    @Test
    void testPutEditItem() throws Exception {
        Item itemEntity = createItemToTest();

        ItemRequestDTO requestDTO = new ItemRequestDTO("Arroz", itemEntity.getCategory().getId(), itemEntity.getUnit().getId());


        var content = given(specification)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", itemEntity.getId())
                .body(requestDTO)
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
        assertEquals(requestDTO.idCategory(), updatedItem.category().id());
        assertEquals(requestDTO.idUnit(), updatedItem.unit().id());
    }

    @Test
    void testGetAllItems() throws Exception {
        createItemToTest();

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
    void testDeleteRemoveCategory() throws Exception {
        Item itemEntity = createItemToTest();

        given(specification)
                .pathParam("id", itemEntity.getId())
                .when()
                .delete("{id}")
                .then()
                .statusCode(204);
    }

    @Test
    void IntegrationTestPostAddItem_BadRequest() throws Exception {
        Category categoryEntity = createCategoryToTest();
        Unit unitEntity = createUnitToTest();

        ItemRequestDTO invalidItem = new ItemRequestDTO("", categoryEntity.getId(), unitEntity.getId());

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
    void IntegrationTestPutEditItem_NotFound() throws Exception {
        Category categoryEntity = createCategoryToTest();
        Unit unitEntity = createUnitToTest();
        ItemRequestDTO invalidItem = new ItemRequestDTO("Feijão", categoryEntity.getId(), unitEntity.getId());

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

    Category createCategoryToTest() {
        Category categoryEntity = new Category();
        categoryEntity.setName("Food");
        categoryEntity = categoryRepository.save(categoryEntity);
        return categoryEntity;
    }

    Unit createUnitToTest() {
        Unit unitEntity = new Unit();
        unitEntity.setSymbol("KG");
        unitEntity.setName("Kilogram");
        unitEntity = unitRepository.save(unitEntity);
        return unitEntity;
    }

    Item createItemToTest() {
        Item itemEntity = new Item();
        itemEntity.setName("Arroz");
        itemEntity.setCategory(createCategoryToTest());
        itemEntity.setUnit(createUnitToTest());
        itemRepository.save(itemEntity);
        return itemEntity;
    }
}

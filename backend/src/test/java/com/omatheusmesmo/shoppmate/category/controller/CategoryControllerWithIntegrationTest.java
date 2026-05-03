package com.omatheusmesmo.shoppmate.category.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.category.repository.CategoryRepository;
import com.omatheusmesmo.shoppmate.item.repository.ItemRepository;
import com.omatheusmesmo.shoppmate.shared.testcontainers.AbstractIntegrationTest;
import com.omatheusmesmo.shoppmate.shared.testcontainers.utils.TestUserFactory;
import com.omatheusmesmo.shoppmate.category.dto.CategoryRequestDTO;
import com.omatheusmesmo.shoppmate.category.dto.CategoryResponseDTO;
import com.omatheusmesmo.shoppmate.shared.testutils.CategoryTestFactory;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.repository.UserRepository;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CategoryControllerWithIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestUserFactory testUserFactory;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private static RequestSpecification specification;
    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @BeforeEach
    void init() {
        itemRepository.deleteAll();
        categoryRepository.deleteAll();
        String jwtToken = testUserFactory.createTokenForTestUser();

        Response response = given().port(port).header("Authorization", "Bearer " + jwtToken).when().get("/category")
                .then().statusCode(200).extract().response();

        String csrfToken = response.cookie("XSRF-TOKEN");

        specification = new RequestSpecBuilder().setPort(port).setBasePath("/category")
                .addHeader("Authorization", "Bearer " + jwtToken).addHeader("X-XSRF-TOKEN", csrfToken)
                .addCookie("XSRF-TOKEN", csrfToken).setContentType(ContentType.JSON).build();
    }

    @Test
    void testPostAddCategory() throws Exception {
        CategoryRequestDTO request = CategoryTestFactory.createValidCategoryRequestDTO();

        var content = given(specification).contentType(MediaType.APPLICATION_JSON_VALUE).body(request).when().post()
                .then().statusCode(201).contentType(MediaType.APPLICATION_JSON_VALUE).extract().body().asString();

        CategoryResponseDTO createdCategory = objectMapper.readValue(content, CategoryResponseDTO.class);

        assertNotNull(createdCategory.id());
        assertTrue(createdCategory.id() > 0);
        assertEquals(request.name(), createdCategory.name());
        assertFalse(createdCategory.isSystemStandard());
    }

    @Test
    void testPutEditCategory() throws Exception {
        Category categoryEntity = createCategoryToTest();
        CategoryRequestDTO updateRequest = new CategoryRequestDTO(categoryEntity.getName() + " Updated");

        var content = given(specification).contentType(MediaType.APPLICATION_JSON_VALUE)
                .pathParam("id", categoryEntity.getId()).body(updateRequest).when().put("{id}").then().statusCode(200)
                .contentType(MediaType.APPLICATION_JSON_VALUE).extract().body().asString();

        CategoryResponseDTO updatedCategory = objectMapper.readValue(content, CategoryResponseDTO.class);

        assertNotNull(updatedCategory.id());
        assertEquals(categoryEntity.getId(), updatedCategory.id());
        assertEquals(updateRequest.name(), updatedCategory.name());
    }

    @Test
    void testGetAllCategories() throws Exception {
        Category categoryEntity = createCategoryToTest();

        var content = given(specification).accept(MediaType.APPLICATION_JSON_VALUE).when().get().then().statusCode(200)
                .contentType(MediaType.APPLICATION_JSON_VALUE).extract().body().asString();

        List<CategoryResponseDTO> categories = objectMapper.readValue(content,
                new TypeReference<List<CategoryResponseDTO>>() {
                });

        assertFalse(categories.isEmpty());
        CategoryResponseDTO categoryOne = categories.get(0);

        assertNotNull(categoryOne.id());
        assertEquals(categoryEntity.getName(), categoryOne.name());
    }

    @Test
    void testDeleteRemoveCategory() {
        Category categoryEntity = createCategoryToTest();

        given(specification).pathParam("id", categoryEntity.getId()).when().delete("{id}").then().statusCode(204);
    }

    @Test
    void IntegrationTestPostAddCategory_BadRequest() throws Exception {
        CategoryRequestDTO invalidItem = new CategoryRequestDTO("");

        given(specification).contentType(MediaType.APPLICATION_JSON_VALUE).body(invalidItem).when().post().then()
                .statusCode(400);
    }

    @Test
    void IntegrationTestPutEditCategory_NotFound() throws Exception {
        CategoryRequestDTO request = CategoryTestFactory.createValidCategoryRequestDTO();

        given(specification).contentType(MediaType.APPLICATION_JSON_VALUE).pathParam("id", 999L).body(request).when()
                .put("/{id}").then().statusCode(404);
    }

    private Category createCategoryToTest() {
        User testUser = userRepository.findByEmail(TestUserFactory.TEST_USER_EMAIL).orElseThrow();
        Category categoryEntity = CategoryTestFactory.createValidCategory();
        categoryEntity.setId(null);
        categoryEntity.setOwner(testUser);
        categoryEntity.setSystemStandard(false);
        return categoryRepository.save(categoryEntity);
    }

    private Category createSystemCategoryToTest() {
        Category categoryEntity = CategoryTestFactory.createValidCategory();
        categoryEntity.setId(null);
        categoryEntity.setOwner(null);
        categoryEntity.setSystemStandard(true);
        return categoryRepository.save(categoryEntity);
    }

    private Category createOtherUserCategoryToTest() {
        User otherUser = new User();
        otherUser.setEmail("other-" + System.currentTimeMillis() + "@example.com");
        otherUser.setFullName("Other User");
        otherUser.setPassword("$2a$10$encoded");
        otherUser.setRole("USER");
        otherUser = userRepository.save(otherUser);

        Category categoryEntity = CategoryTestFactory.createValidCategory();
        categoryEntity.setId(null);
        categoryEntity.setOwner(otherUser);
        categoryEntity.setSystemStandard(false);
        return categoryRepository.save(categoryEntity);
    }

    @Test
    void testDeleteSystemCategory_Forbid() {
        Category systemCategory = createSystemCategoryToTest();

        given(specification).pathParam("id", systemCategory.getId()).when().delete("{id}").then().statusCode(403);
    }

    @Test
    void testPutSystemCategory_Forbid() throws Exception {
        Category systemCategory = createSystemCategoryToTest();
        CategoryRequestDTO updateRequest = new CategoryRequestDTO(systemCategory.getName() + " Updated");

        given(specification).contentType(MediaType.APPLICATION_JSON_VALUE).pathParam("id", systemCategory.getId())
                .body(updateRequest).when().put("{id}").then().statusCode(403);
    }

    @Test
    void testDeleteOtherUserCategory_Forbid() {
        Category otherUserCategory = createOtherUserCategoryToTest();

        given(specification).pathParam("id", otherUserCategory.getId()).when().delete("{id}").then().statusCode(403);
    }

    @Test
    void testPutOtherUserCategory_Forbid() throws Exception {
        Category otherUserCategory = createOtherUserCategoryToTest();
        CategoryRequestDTO updateRequest = new CategoryRequestDTO(otherUserCategory.getName() + " Updated");

        given(specification).contentType(MediaType.APPLICATION_JSON_VALUE).pathParam("id", otherUserCategory.getId())
                .body(updateRequest).when().put("{id}").then().statusCode(403);
    }
}

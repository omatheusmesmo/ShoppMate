package com.omatheusmesmo.shoppmate.list.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListResponseDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.repository.ListItemRepository;
import com.omatheusmesmo.shoppmate.list.repository.ListPermissionRepository;
import com.omatheusmesmo.shoppmate.list.repository.ShoppingListRepository;
import com.omatheusmesmo.shoppmate.shared.testcontainers.AbstractIntegrationTest;
import com.omatheusmesmo.shoppmate.shared.testcontainers.utils.TestUserFactory;
import com.omatheusmesmo.shoppmate.shared.testutils.ListTestFactory;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.repository.UserRepository;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingListControllerIntegrationTest extends AbstractIntegrationTest {

    private static ObjectMapper objectMapper;
    @LocalServerPort
    private int port;
    @Autowired
    private TestUserFactory testUserFactory;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ShoppingListRepository shoppingListRepository;
    @Autowired
    private ListItemRepository listItemRepository;
    @Autowired
    private ListPermissionRepository listPermissionRepository;
    private RequestSpecification specification;

    private User testUser;

    @BeforeAll
    static void setUpObjectMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @BeforeEach
    void setUp() {
        cleanDatabase();

        String jwtToken = testUserFactory.createTokenForTestUser();

        testUser = userRepository.findByEmail(TestUserFactory.TEST_USER_EMAIL)
            .orElseThrow();

        Response response = given()
            .port(port)
            .header("Authorization", "Bearer " + jwtToken)
            .when()
            .get("/lists")
            .then()
            .statusCode(200)
            .extract()
            .response();

        String csrfToken = response.cookie("XSRF-TOKEN");

        specification = new RequestSpecBuilder()
            .setPort(port)
            .setBasePath("/lists")
            .addHeader("Authorization", "Bearer " + jwtToken)
            .addHeader("X-XSRF-TOKEN", csrfToken)
            .addCookie("XSRF-TOKEN", csrfToken)
            .setContentType(ContentType.JSON)
            .build();
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void testPutEditShoppingList_WithCurrentVersion_ReturnsOkAndIncrementsVersion() throws Exception {
        ShoppingList shoppingList = createShoppingListToTest();

        ShoppingList listBeforeUpdate = reloadShoppingList(shoppingList.getId());

        Long listId = listBeforeUpdate.getId();
        Long originalVersion = listBeforeUpdate.getVersion();

        ShoppingListUpdateRequestDTO requestDTO = new ShoppingListUpdateRequestDTO(listId,
            "Updated Grocery List",
            originalVersion
        );

        String content = given(specification)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("id", listId)
            .body(requestDTO)
            .when()
            .put("/{id}")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .extract()
            .body()
            .asString();

        ShoppingListResponseDTO responseDTO = objectMapper.readValue(content, ShoppingListResponseDTO.class);

        assertEquals(listId, responseDTO.idList());
        assertEquals("Updated Grocery List", responseDTO.listName());
        assertNotNull(responseDTO.version());
        assertEquals(originalVersion + 1, responseDTO.version());

        ShoppingList updatedShoppingList = reloadShoppingList(listId);

        assertEquals("Updated Grocery List", updatedShoppingList.getName());
        assertEquals(originalVersion + 1, updatedShoppingList.getVersion());
    }

    @Test
    void testPutEditShoppingList_WithStaleVersion_ReturnsConflict() {
        ShoppingList shoppingList = createShoppingListToTest();

        // Fetch latest DB state BEFORE the first call
        ShoppingList latestBeforeFirstUpdate = shoppingListRepository.findById(shoppingList.getId())
            .orElseThrow();

        Long listId = latestBeforeFirstUpdate.getId();
        Long currentVersion = latestBeforeFirstUpdate.getVersion();

        ShoppingListUpdateRequestDTO firstUpdate = new ShoppingListUpdateRequestDTO(listId,
            "First Update",
            currentVersion
        );

        // This should pass because it uses the latest version
        given(specification)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("id", listId)
            .body(firstUpdate)
            .when()
            .put("/{id}")
            .then()
            .statusCode(200);

        // Fetch latest after the first update only to verify the DB moved forward
        ShoppingList latestAfterFirstUpdate = shoppingListRepository.findById(listId)
            .orElseThrow();

        assertEquals("First Update", latestAfterFirstUpdate.getName());
        assertEquals(currentVersion + 1, latestAfterFirstUpdate.getVersion());

        ShoppingListUpdateRequestDTO staleUpdate = new ShoppingListUpdateRequestDTO(listId,
            "Stale Update",
            currentVersion
        );

        // This should fail because it intentionally reuses the old version
        given(specification)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("id", listId)
            .body(staleUpdate)
            .when()
            .put("/{id}")
            .then()
            .statusCode(409)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("status", equalTo(409))
            .body("message", equalTo("Update Conflict"))
            .body("details", equalTo("This resource was updated by someone else. Please refresh and try again."));
    }

    @Test
    void testPutEditShoppingList_WithoutVersion_ReturnsBadRequest() {
        ShoppingList shoppingList = createShoppingListToTest();

        String requestBody = """
            {
              "name": "Updated Grocery List"
            }
            """;

        given(specification)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("id", shoppingList.getId())
            .body(requestBody)
            .when()
            .put("/{id}")
            .then()
            .statusCode(400)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("message", equalTo("Validation Failed"));
    }

    @Test
    void testGetShoppingListById_ReturnsVersion() throws Exception {
        ShoppingList shoppingList = createShoppingListToTest();

        ShoppingList listBeforeUpdate = reloadShoppingList(shoppingList.getId());

        String content = given(specification)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("id", listBeforeUpdate.getId())
            .when()
            .get("/{id}")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .extract()
            .body()
            .asString();

        ShoppingListResponseDTO responseDTO = objectMapper.readValue(content, ShoppingListResponseDTO.class);

        assertEquals(listBeforeUpdate.getId(), responseDTO.idList());
        assertNotNull(responseDTO.version());
        assertEquals(listBeforeUpdate.getVersion(), responseDTO.version());
    }

    private ShoppingList createShoppingListToTest() {
        ShoppingList shoppingList = ListTestFactory.createValidShoppingList();

        shoppingList.setId(null);
        shoppingList.setVersion(null);
        shoppingList.setOwner(testUser);
        shoppingList.setCreatedAt(null);
        shoppingList.setUpdatedAt(null);
        shoppingList.setDeleted(false);

        ShoppingList savedShoppingList = shoppingListRepository.saveAndFlush(shoppingList);

        return reloadShoppingList(savedShoppingList.getId());
    }

    private ShoppingList reloadShoppingList(Long listId) {
        return shoppingListRepository.findById(listId)
            .orElseThrow();
    }

    private void cleanDatabase() {
        listPermissionRepository.deleteAllInBatch();
        listItemRepository.deleteAllInBatch();
        shoppingListRepository.deleteAllInBatch();
    }
}

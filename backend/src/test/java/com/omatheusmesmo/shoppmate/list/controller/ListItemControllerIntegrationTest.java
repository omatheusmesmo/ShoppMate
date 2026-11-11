package com.omatheusmesmo.shoppmate.list.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.category.repository.CategoryRepository;
import com.omatheusmesmo.shoppmate.item.entity.Item;
import com.omatheusmesmo.shoppmate.item.repository.ItemRepository;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemResponseDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.ListItem;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.repository.ListItemRepository;
import com.omatheusmesmo.shoppmate.list.repository.ShoppingListRepository;
import com.omatheusmesmo.shoppmate.shared.testcontainers.AbstractIntegrationTest;
import com.omatheusmesmo.shoppmate.shared.testcontainers.utils.TestUserFactory;
import com.omatheusmesmo.shoppmate.shared.testutils.CategoryTestFactory;
import com.omatheusmesmo.shoppmate.shared.testutils.ItemTestFactory;
import com.omatheusmesmo.shoppmate.shared.testutils.ListTestFactory;
import com.omatheusmesmo.shoppmate.shared.testutils.UnitTestFactory;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.repository.UnitRepository;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.repository.UserRepository;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ListItemControllerIntegrationTest extends AbstractIntegrationTest {

    private static ObjectMapper objectMapper;
    @LocalServerPort
    private int port;
    @Autowired
    private TestUserFactory testUserFactory;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ShoppingListRepository shoppingListRepository;
    @Autowired
    private ListItemRepository listItemRepository;
    private RequestSpecification specification;

    private User testUser;

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @BeforeEach
    void init() {
        listItemRepository.deleteAll();
        shoppingListRepository.deleteAll();
        itemRepository.deleteAll();
        unitRepository.deleteAll();
        categoryRepository.deleteAll();

        String jwtToken = testUserFactory.createTokenForTestUser();
        testUser = userRepository.findByEmail(TestUserFactory.TEST_USER_EMAIL).orElseThrow();

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

    @Test
    void testPutEditListItem_WithCurrentVersion_ReturnsOkAndIncrementsVersion() throws Exception {
        ListItem listItem = createListItemToTest();

        Long listId = listItem.getShoppList().getId();
        Long listItemId = listItem.getId();
        Long originalVersion = listItem.getVersion();

        ListItemUpdateRequestDTO requestDTO = new ListItemUpdateRequestDTO(
            listId,
            listItem.getItem().getId(),
            2,
            false,
            BigDecimal.valueOf(4.99),
            originalVersion
        );

        String content = given(specification)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("listId", listId)
            .pathParam("id", listItemId)
            .body(requestDTO)
            .when()
            .put("/{listId}/items/{id}")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .extract()
            .body()
            .asString();

        ListItemResponseDTO responseDTO = objectMapper.readValue(content, ListItemResponseDTO.class);

        assertEquals(listItemId, responseDTO.idListItem());
        assertEquals(2, responseDTO.quantity());
        assertEquals(false, responseDTO.purchased());
        assertEquals(originalVersion + 1, responseDTO.version());
    }

    @Test
    void testPutEditListItem_WithStaleVersion_ReturnsConflict() {
        ListItem listItem = createListItemToTest();

        Long listId = listItem.getShoppList().getId();
        Long listItemId = listItem.getId();
        Long originalVersion = listItem.getVersion();

        ListItemUpdateRequestDTO firstUpdate = new ListItemUpdateRequestDTO(
            listId,
            listItem.getItem().getId(),
            2,
            false,
            BigDecimal.valueOf(4.99),
            originalVersion
        );

        given(specification)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("listId", listId)
            .pathParam("id", listItemId)
            .body(firstUpdate)
            .when()
            .put("/{listId}/items/{id}")
            .then()
            .statusCode(200);

        ListItemUpdateRequestDTO staleUpdate = new ListItemUpdateRequestDTO(
            listId,
            listItem.getItem().getId(),
            3,
            true,
            BigDecimal.valueOf(5.99),
            originalVersion
        );

        given(specification)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("listId", listId)
            .pathParam("id", listItemId)
            .body(staleUpdate)
            .when()
            .put("/{listId}/items/{id}")
            .then()
            .statusCode(409)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("status", equalTo(409))
            .body("message", equalTo("Update Conflict"))
            .body("details", equalTo("This resource was updated by someone else. Please refresh and try again."));
    }

    @Test
    void testPutEditListItem_WithoutVersion_ReturnsBadRequest() {
        ListItem listItem = createListItemToTest();

        String requestBody = """
            {
              "listId": %d,
              "itemId": %d,
              "quantity": 2,
              "purchased": false,
              "unitPrice": 4.99
            }
            """.formatted(listItem.getShoppList().getId(), listItem.getItem().getId());

        given(specification)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("listId", listItem.getShoppList().getId())
            .pathParam("id", listItem.getId())
            .body(requestBody)
            .when()
            .put("/{listId}/items/{id}")
            .then()
            .statusCode(400)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("message", equalTo("Validation Failed"));
    }

    @Test
    void testGetListItemById_ReturnsVersion() throws Exception {
        ListItem listItem = createListItemToTest();

        String content = given(specification)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("listId", listItem.getShoppList().getId())
            .pathParam("id", listItem.getId())
            .when()
            .get("/{listId}/items/{id}")
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .extract()
            .body()
            .asString();

        ListItemResponseDTO responseDTO = objectMapper.readValue(content, ListItemResponseDTO.class);

        assertEquals(listItem.getId(), responseDTO.idListItem());
        assertNotNull(responseDTO.version());
        assertEquals(listItem.getVersion(), responseDTO.version());
    }

    private Category createCategoryToTest() {
        Category category = CategoryTestFactory.createValidCategory();
        category.setId(null);
        category.setOwner(testUser);
        category.setSystemStandard(false);
        return categoryRepository.save(category);
    }

    private Unit createUnitToTest() {
        Unit unit = UnitTestFactory.createValidUnit();
        unit.setId(null);
        unit.setOwner(testUser);
        unit.setSystemStandard(false);
        return unitRepository.save(unit);
    }

    private Item createItemToTest() {
        Item item = ItemTestFactory.createValidItem();
        item.setId(null);
        item.setCategory(createCategoryToTest());
        item.setUnit(createUnitToTest());
        return itemRepository.save(item);
    }

    private ShoppingList createShoppingListToTest() {
        ShoppingList shoppingList = ListTestFactory.createValidShoppingList();
        shoppingList.setId(null);
        shoppingList.setOwner(testUser);
        return shoppingListRepository.save(shoppingList);
    }

    private ListItem createListItemToTest() {
        ListItem listItem = new ListItem();
        listItem.setId(null);
        listItem.setShoppList(createShoppingListToTest());
        listItem.setItem(createItemToTest());
        listItem.setQuantity(1);
        listItem.setPurchased(false);
        listItem.setUnitPrice(BigDecimal.valueOf(3.99));

        return listItemRepository.saveAndFlush(listItem);
    }
}

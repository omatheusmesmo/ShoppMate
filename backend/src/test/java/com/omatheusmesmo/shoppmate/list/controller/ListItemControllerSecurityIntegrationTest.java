package com.omatheusmesmo.shoppmate.list.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.category.repository.CategoryRepository;
import com.omatheusmesmo.shoppmate.item.entity.Item;
import com.omatheusmesmo.shoppmate.item.repository.ItemRepository;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.ListItem;
import com.omatheusmesmo.shoppmate.list.entity.ListPermission;
import com.omatheusmesmo.shoppmate.list.entity.Permission;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.mapper.ListMapper;
import com.omatheusmesmo.shoppmate.list.repository.ListItemRepository;
import com.omatheusmesmo.shoppmate.list.repository.ListPermissionRepository;
import com.omatheusmesmo.shoppmate.list.repository.ShoppingListRepository;
import com.omatheusmesmo.shoppmate.list.service.ListItemService;
import com.omatheusmesmo.shoppmate.list.service.ShoppingListService;
import com.omatheusmesmo.shoppmate.shared.testcontainers.AbstractIntegrationTest;
import com.omatheusmesmo.shoppmate.shared.testutils.ListTestFactory;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.repository.UnitRepository;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ListItemControllerSecurityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ListItemRepository listItemRepository;

    @Autowired
    private ShoppingListRepository shoppingListRepository;

    @Autowired
    private ShoppingListService shoppingListService;

    @Autowired
    private ListItemService listItemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ListMapper listMapper;

    @Autowired
    private ListPermissionRepository listPermissionRepository;

    private User userA;
    private User userB;

    private ShoppingList userAList;
    private ShoppingList userAList2;
    private ShoppingList userBList;

    private Item item;

    private ListItem userAListItem;
    private ListItem userBListItem;

    private String tokenUserA;
    private String tokenUserB;

    @BeforeEach
    void setUp() {
        listPermissionRepository.deleteAll();
        listItemRepository.deleteAll();
        shoppingListRepository.deleteAll();
        itemRepository.deleteAll();
        categoryRepository.deleteAll();
        unitRepository.deleteAll();
        userRepository.deleteAll();

        userA = new User();
        userA.setEmail("usera@test.com");
        userA.setFullName("User A");
        userA.setPassword(passwordEncoder.encode("password123"));
        userA.setRole("USER");
        userA = userRepository.save(userA);

        userB = new User();
        userB.setEmail("userb@test.com");
        userB.setFullName("User B");
        userB.setPassword(passwordEncoder.encode("password123"));
        userB.setRole("USER");
        userB = userRepository.save(userB);

        tokenUserA = jwtService.generateToken(userA);
        tokenUserB = jwtService.generateToken(userB);

        ShoppingListRequestDTO dtoA = new ShoppingListRequestDTO("User A's Shopping List");

        ShoppingList entityA = listMapper.toEntity(dtoA, userA);

        userAList = shoppingListService.saveList(entityA);

        ShoppingListRequestDTO dtoA2 = new ShoppingListRequestDTO("User A's Second Shopping List");

        ShoppingList entityA2 = listMapper.toEntity(dtoA2, userA);

        userAList2 = shoppingListService.saveList(entityA2);

        ShoppingListRequestDTO dtoB = new ShoppingListRequestDTO("User B's Shopping List");

        ShoppingList entityB = listMapper.toEntity(dtoB, userB);

        userBList = shoppingListService.saveList(entityB);

        Category category = new Category();
        category.setName("Food");
        category = categoryRepository.save(category);

        Unit unit = new Unit();
        unit.setName("Kilogram");
        unit.setSymbol("kg");
        unit = unitRepository.save(unit);

        item = new Item();
        item.setName("Milk");
        item.setCategory(category);
        item.setUnit(unit);
        item = itemRepository.save(item);

        ListItemRequestDTO itemDTOA = new ListItemRequestDTO(userAList.getId(), item.getId(), 2, null);

        userAListItem = listItemService.addShoppItemList(itemDTOA, userA);

        ListItemRequestDTO itemDTOB = new ListItemRequestDTO(userBList.getId(), item.getId(), 1, null);

        userBListItem = listItemService.addShoppItemList(itemDTOB, userB);
    }

    @AfterEach
    void tearDown() {
        listPermissionRepository.deleteAll();
        listItemRepository.deleteAll();
        shoppingListRepository.deleteAll();
        itemRepository.deleteAll();
        categoryRepository.deleteAll();
        unitRepository.deleteAll();
        userRepository.deleteAll();
    }

    private ListPermission grantPermission(ShoppingList shoppingList, User user, Permission permission) {

        ListPermission listPermission = ListTestFactory.createValidListPermission(shoppingList);

        listPermission.setShoppingList(shoppingList);
        listPermission.setUser(user);
        listPermission.setPermission(permission);

        return listPermissionRepository.save(listPermission);
    }

    @Test
    void testUserCannotGetAnotherUsersListItem() throws Exception {

        mockMvc.perform(get("/lists/" + userBList.getId() + "/items/" + userBListItem.getId()).header("Authorization",
                "Bearer " + tokenUserA)).andExpect(status().isForbidden());
    }

    @Test
    void testUserCanGetOwnListItem() throws Exception {

        mockMvc.perform(get("/lists/" + userAList.getId() + "/items/" + userAListItem.getId()).header("Authorization",
                "Bearer " + tokenUserA)).andExpect(status().isOk()).andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void testReadUserCanGetSharedListItem() throws Exception {

        grantPermission(userBList, userA, Permission.READ);

        mockMvc.perform(get("/lists/" + userBList.getId() + "/items/" + userBListItem.getId()).header("Authorization",
                "Bearer " + tokenUserA)).andExpect(status().isOk()).andExpect(jsonPath("$.quantity").value(1));
    }

    @Test
    void testWriteUserCanGetSharedListItem() throws Exception {

        grantPermission(userBList, userA, Permission.WRITE);

        mockMvc.perform(get("/lists/" + userBList.getId() + "/items/" + userBListItem.getId()).header("Authorization",
                "Bearer " + tokenUserA)).andExpect(status().isOk()).andExpect(jsonPath("$.quantity").value(1));
    }

    @Test
    void testUserCannotGetAllItemsFromAnotherUsersList() throws Exception {

        mockMvc.perform(get("/lists/" + userBList.getId() + "/items").header("Authorization", "Bearer " + tokenUserA))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testUserCanGetAllItemsFromOwnList() throws Exception {

        mockMvc.perform(get("/lists/" + userAList.getId() + "/items").header("Authorization", "Bearer " + tokenUserA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].quantity").value(2));
    }

    @Test
    void testReadUserCanGetAllItemsFromSharedList() throws Exception {

        grantPermission(userBList, userA, Permission.READ);

        mockMvc.perform(get("/lists/" + userBList.getId() + "/items").header("Authorization", "Bearer " + tokenUserA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].quantity").value(1));
    }

    @Test
    void testWriteUserCanGetAllItemsFromSharedList() throws Exception {

        grantPermission(userBList, userA, Permission.WRITE);

        mockMvc.perform(get("/lists/" + userBList.getId() + "/items").header("Authorization", "Bearer " + tokenUserA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].quantity").value(1));
    }

    @Test
    void testUserCannotAddItemToAnotherUsersList() throws Exception {

        ListItemRequestDTO maliciousDTO = new ListItemRequestDTO(userBList.getId(), item.getId(), 5, null);

        mockMvc.perform(post("/lists/" + userBList.getId() + "/items").with(csrf())
                .header("Authorization", "Bearer " + tokenUserA).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousDTO))).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());

        assertEquals(1, countItemsForList(userBList.getId()));
    }

    @Test
    void testReadUserCannotAddItemToSharedList() throws Exception {

        grantPermission(userBList, userA, Permission.READ);

        ListItemRequestDTO requestDTO = new ListItemRequestDTO(userBList.getId(), item.getId(), 5, null);

        mockMvc.perform(post("/lists/" + userBList.getId() + "/items").with(csrf())
                .header("Authorization", "Bearer " + tokenUserA).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isForbidden());

        assertEquals(1, countItemsForList(userBList.getId()));
    }

    @Test
    void testWriteUserCanAddItemToSharedList() throws Exception {

        grantPermission(userBList, userA, Permission.WRITE);

        Category category = new Category();
        category.setName("Shared Bakery");
        category = categoryRepository.save(category);

        Unit unit = new Unit();
        unit.setName("Shared Pieces");
        unit.setSymbol("sp");
        unit = unitRepository.save(unit);

        Item newItem = new Item();
        newItem.setName("Shared Bread");
        newItem.setCategory(category);
        newItem.setUnit(unit);
        newItem = itemRepository.save(newItem);

        ListItemRequestDTO requestDTO = new ListItemRequestDTO(userBList.getId(), newItem.getId(), 5, null);

        mockMvc.perform(post("/lists/" + userBList.getId() + "/items").with(csrf())
                .header("Authorization", "Bearer " + tokenUserA).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isCreated());

        assertEquals(2, countItemsForList(userBList.getId()));
    }

    @Test
    void testUserCanAddItemToOwnList() throws Exception {

        Category category = new Category();
        category.setName("Bakery");
        category = categoryRepository.save(category);

        Unit unit = new Unit();
        unit.setName("Pieces");
        unit.setSymbol("pcs");
        unit = unitRepository.save(unit);

        Item newItem = new Item();
        newItem.setName("Bread");
        newItem.setCategory(category);
        newItem.setUnit(unit);
        newItem = itemRepository.save(newItem);

        ListItemRequestDTO validDTO = new ListItemRequestDTO(userAList.getId(), newItem.getId(), 3, null);

        mockMvc.perform(post("/lists/" + userAList.getId() + "/items").with(csrf())
                .header("Authorization", "Bearer " + tokenUserA).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDTO))).andExpect(status().isCreated());

        assertEquals(2, countItemsForList(userAList.getId()));
    }

    @Test
    void testUserCannotDeleteAnotherUsersListItem() throws Exception {

        mockMvc.perform(delete("/lists/" + userBList.getId() + "/items/" + userBListItem.getId()).with(csrf())
                .header("Authorization", "Bearer " + tokenUserA)).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());

        assertFalse(listItemRepository.findById(userBListItem.getId()).orElseThrow().getDeleted());
    }

    @Test
    void testReadUserCannotDeleteSharedListItem() throws Exception {

        grantPermission(userBList, userA, Permission.READ);

        mockMvc.perform(delete("/lists/" + userBList.getId() + "/items/" + userBListItem.getId()).with(csrf())
                .header("Authorization", "Bearer " + tokenUserA)).andExpect(status().isForbidden());

        assertFalse(listItemRepository.findById(userBListItem.getId()).orElseThrow().getDeleted());
    }

    @Test
    void testWriteUserCanDeleteSharedListItem() throws Exception {

        grantPermission(userBList, userA, Permission.WRITE);

        mockMvc.perform(delete("/lists/" + userBList.getId() + "/items/" + userBListItem.getId()).with(csrf())
                .header("Authorization", "Bearer " + tokenUserA)).andExpect(status().isNoContent());

        assertTrue(listItemRepository.findById(userBListItem.getId()).orElseThrow().getDeleted());
    }

    @Test
    void testUserCanDeleteOwnListItem() throws Exception {

        mockMvc.perform(delete("/lists/" + userAList.getId() + "/items/" + userAListItem.getId()).with(csrf())
                .header("Authorization", "Bearer " + tokenUserA)).andExpect(status().isNoContent());

        assertTrue(listItemRepository.findById(userAListItem.getId()).isPresent());

        assertTrue(listItemRepository.findById(userAListItem.getId()).orElseThrow().getDeleted());
    }

    @Test
    void testUserCannotEditAnotherUsersListItem() throws Exception {

        ListItemUpdateRequestDTO maliciousUpdate = new ListItemUpdateRequestDTO(userBList.getId(), item.getId(), 99,
                false, null);

        mockMvc.perform(put("/lists/" + userBList.getId() + "/items/" + userBListItem.getId()).with(csrf())
                .header("Authorization", "Bearer " + tokenUserA).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousUpdate))).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());

        assertEquals(1, listItemRepository.findById(userBListItem.getId()).orElseThrow().getQuantity());
    }

    @Test
    void testReadUserCannotEditSharedListItem() throws Exception {

        grantPermission(userBList, userA, Permission.READ);

        ListItemUpdateRequestDTO updateDTO = new ListItemUpdateRequestDTO(userBList.getId(), item.getId(), 99, false,
                null);

        mockMvc.perform(put("/lists/" + userBList.getId() + "/items/" + userBListItem.getId()).with(csrf())
                .header("Authorization", "Bearer " + tokenUserA).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO))).andExpect(status().isForbidden());

        assertEquals(1, listItemRepository.findById(userBListItem.getId()).orElseThrow().getQuantity());
    }

    @Test
    void testWriteUserCanEditSharedListItem() throws Exception {

        grantPermission(userBList, userA, Permission.WRITE);

        ListItemUpdateRequestDTO updateDTO = new ListItemUpdateRequestDTO(userBList.getId(), item.getId(), 10, true,
                null);

        mockMvc.perform(put("/lists/" + userBList.getId() + "/items/" + userBListItem.getId()).with(csrf())
                .header("Authorization", "Bearer " + tokenUserA).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO))).andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(10));

        assertEquals(10, listItemRepository.findById(userBListItem.getId()).orElseThrow().getQuantity());
    }

    @Test
    void testUserCanEditOwnListItem() throws Exception {

        ListItemUpdateRequestDTO validUpdate = new ListItemUpdateRequestDTO(userAList.getId(), item.getId(), 10, true,
                null);

        mockMvc.perform(put("/lists/" + userAList.getId() + "/items/" + userAListItem.getId()).with(csrf())
                .header("Authorization", "Bearer " + tokenUserA).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdate))).andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(10));

        assertEquals(10, listItemRepository.findById(userAListItem.getId()).orElseThrow().getQuantity());
    }

    @Test
    void testUserCannotAccessItemsFromNonExistentList() throws Exception {

        mockMvc.perform(get("/lists/99999/items").header("Authorization", "Bearer " + tokenUserA))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUnauthorizedRequestIsRejected() throws Exception {

        mockMvc.perform(get("/lists/" + userAList.getId() + "/items")).andExpect(status().isForbidden());
    }

    @Test
    void testInvalidTokenIsRejected() throws Exception {

        mockMvc.perform(
                get("/lists/" + userAList.getId() + "/items").header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetListItemWithMismatchedListIdIsRejected() throws Exception {

        mockMvc.perform(get("/lists/" + userAList2.getId() + "/items/" + userAListItem.getId()).header("Authorization",
                "Bearer " + tokenUserA)).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testPostListItemWithMismatchedPathListIdIsRejected() throws Exception {

        ListItemRequestDTO maliciousDTO = new ListItemRequestDTO(userAList2.getId(), item.getId(), 5, null);

        mockMvc.perform(post("/lists/" + userAList.getId() + "/items").with(csrf())
                .header("Authorization", "Bearer " + tokenUserA).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousDTO))).andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalArgumentException));

        assertEquals(0, countItemsForList(userAList2.getId()));
    }

    @Test
    void testDeleteListItemWithMismatchedListIdIsRejected() throws Exception {

        mockMvc.perform(delete("/lists/" + userAList2.getId() + "/items/" + userAListItem.getId()).with(csrf())
                .header("Authorization", "Bearer " + tokenUserA)).andExpect(status().isNotFound());

        assertFalse(listItemRepository.findById(userAListItem.getId()).orElseThrow().getDeleted());
    }

    @Test
    void testUpdateListItemWithMismatchedPathListIdIsRejected() throws Exception {

        ListItemUpdateRequestDTO maliciousUpdate = new ListItemUpdateRequestDTO(userAList2.getId(), item.getId(), 99,
                false, null);

        mockMvc.perform(put("/lists/" + userAList.getId() + "/items/" + userAListItem.getId()).with(csrf())
                .header("Authorization", "Bearer " + tokenUserA).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousUpdate))).andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalArgumentException));

        assertEquals(2, listItemRepository.findById(userAListItem.getId()).orElseThrow().getQuantity());
    }

    private long countItemsForList(Long listId) {
        long count = 0;

        for (ListItem listItem : listItemRepository.findAll()) {
            if (listItem.getShoppList().getId().equals(listId)) {
                count++;
            }
        }

        return count;
    }
}

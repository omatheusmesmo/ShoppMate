package com.omatheusmesmo.shoppmate.list.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.category.repository.CategoryRepository;
import com.omatheusmesmo.shoppmate.item.entity.Item;
import com.omatheusmesmo.shoppmate.item.repository.ItemRepository;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.mapper.ListMapper;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.repository.UnitRepository;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.ListItem;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.repository.ListItemRepository;
import com.omatheusmesmo.shoppmate.list.service.ListItemService;
import com.omatheusmesmo.shoppmate.list.service.ShoppingListService;
import com.omatheusmesmo.shoppmate.shared.testcontainers.AbstractIntegrationTest;
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
import com.omatheusmesmo.shoppmate.list.repository.ShoppingListRepository;
import com.omatheusmesmo.shoppmate.list.repository.ListPermissionRepository;

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
    void testUserCannotAddItemToAnotherUsersList() throws Exception {
        ListItemRequestDTO maliciousDTO = new ListItemRequestDTO(userBList.getId(), item.getId(), 5, null);

        mockMvc.perform(post("/lists/" + userBList.getId() + "/items").with(csrf())
                .header("Authorization", "Bearer " + tokenUserA).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousDTO))).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());

        Iterable<ListItem> userBItems = listItemRepository.findAll();
        long userBItemCount = 0;
        for (ListItem li : userBItems) {
            if (li.getShoppList().getId().equals(userBList.getId())) {
                userBItemCount++;
            }
        }
        assertEquals(1, userBItemCount, "User B's list should still have only 1 item");
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

        Iterable<ListItem> allItems = listItemRepository.findAll();
        long userAItemCount = 0;
        for (ListItem li : allItems) {
            if (li.getShoppList().getId().equals(userAList.getId())) {
                userAItemCount++;
            }
        }
        assertEquals(2, userAItemCount, "User A's list should now have 2 items");
    }

    @Test
    void testUserCannotDeleteAnotherUsersListItem() throws Exception {
        mockMvc.perform(delete("/lists/" + userBList.getId() + "/items/" + userBListItem.getId()).with(csrf())
                .header("Authorization", "Bearer " + tokenUserA)).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());

        assertTrue(listItemRepository.findById(userBListItem.getId()).isPresent(),
                "User B's list item should still exist");
    }

    @Test
    void testUserCanDeleteOwnListItem() throws Exception {
        mockMvc.perform(delete("/lists/" + userAList.getId() + "/items/" + userAListItem.getId()).with(csrf())
                .header("Authorization", "Bearer " + tokenUserA)).andExpect(status().isNoContent());

        assertTrue(listItemRepository.findById(userAListItem.getId()).isPresent(),
                "Item should still exist in database (soft delete)");
        assertTrue(listItemRepository.findById(userAListItem.getId()).get().getDeleted(),
                "Item should be marked as deleted");
    }

    @Test
    void testUserCannotEditAnotherUsersListItem() throws Exception {
        ListItemUpdateRequestDTO maliciousUpdate = new ListItemUpdateRequestDTO(userBList.getId(), item.getId(), 99,
                false, null);

        mockMvc.perform(put("/lists/" + userBList.getId() + "/items/" + userBListItem.getId()).with(csrf())
                .header("Authorization", "Bearer " + tokenUserA).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousUpdate))).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());

        ListItem unchangedItem = listItemRepository.findById(userBListItem.getId()).orElseThrow();
        assertEquals(1, unchangedItem.getQuantity(), "User B's item quantity should remain unchanged");
    }

    @Test
    void testUserCanEditOwnListItem() throws Exception {
        ListItemUpdateRequestDTO validUpdate = new ListItemUpdateRequestDTO(userAList.getId(), item.getId(), 10, true,
                null);

        mockMvc.perform(put("/lists/" + userAList.getId() + "/items/" + userAListItem.getId()).with(csrf())
                .header("Authorization", "Bearer " + tokenUserA).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdate))).andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(10));

        ListItem updatedItem = listItemRepository.findById(userAListItem.getId()).orElseThrow();
        assertEquals(10, updatedItem.getQuantity(), "User A's item quantity should be updated");
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

        Iterable<ListItem> userAList2Items = listItemRepository.findAll();
        long userAList2ItemCount = 0;
        for (ListItem li : userAList2Items) {
            if (li.getShoppList().getId().equals(userAList2.getId())) {
                userAList2ItemCount++;
            }
        }
        assertEquals(0, userAList2ItemCount, "User A's second list should have no items");
    }

    @Test
    void testDeleteListItemWithMismatchedListIdIsRejected() throws Exception {
        mockMvc.perform(delete("/lists/" + userAList2.getId() + "/items/" + userAListItem.getId()).with(csrf())
                .header("Authorization", "Bearer " + tokenUserA)).andExpect(status().isNotFound());

        assertTrue(listItemRepository.findById(userAListItem.getId()).isPresent(),
                "User A's list item should still exist");
        assertFalse(listItemRepository.findById(userAListItem.getId()).get().getDeleted(),
                "Item should not be marked as deleted");
    }

    @Test
    void testUpdateListItemWithMismatchedPathListIdIsRejected() throws Exception {
        ListItemUpdateRequestDTO maliciousUpdate = new ListItemUpdateRequestDTO(userAList2.getId(), item.getId(), 99,
                false, null);

        mockMvc.perform(put("/lists/" + userAList.getId() + "/items/" + userAListItem.getId()).with(csrf())
                .header("Authorization", "Bearer " + tokenUserA).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousUpdate))).andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalArgumentException));

        ListItem unchangedItem = listItemRepository.findById(userAListItem.getId()).orElseThrow();
        assertEquals(2, unchangedItem.getQuantity(), "User A's item quantity should remain unchanged");
    }
}

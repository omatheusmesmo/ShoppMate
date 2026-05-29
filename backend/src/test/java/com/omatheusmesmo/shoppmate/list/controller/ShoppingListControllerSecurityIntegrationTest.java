package com.omatheusmesmo.shoppmate.list.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListResponseDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.mapper.ListMapper;
import com.omatheusmesmo.shoppmate.list.repository.ListPermissionRepository;
import com.omatheusmesmo.shoppmate.list.repository.ShoppingListRepository;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ShoppingListControllerSecurityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShoppingListRepository shoppingListRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ListMapper listMapper;

    @Autowired
    private ShoppingListService shoppingListService;

    @Autowired
    private ListPermissionRepository listPermissionRepository;

    private User userA;
    private User userB;
    private ShoppingList userAList;
    private ShoppingList userBList;

    private String tokenUserA;
    private String tokenUserB;

    @BeforeEach
    void setUp() {
        listPermissionRepository.deleteAll();
        shoppingListRepository.deleteAll();
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

        ShoppingListRequestDTO dtoB = new ShoppingListRequestDTO("User B's Shopping List");
        ShoppingList entityB = listMapper.toEntity(dtoB, userB);
        userBList = shoppingListService.saveList(entityB);
    }

    @AfterEach
    void tearDown() {
        listPermissionRepository.deleteAll();
        shoppingListRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testUserCannotGetAnotherUsersShoppingList() throws Exception {
        mockMvc.perform(get("/lists/" + userBList.getId()).header("Authorization", "Bearer " + tokenUserA))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUserCanGetOwnShoppingList() throws Exception {
        mockMvc.perform(get("/lists/" + userAList.getId()).header("Authorization", "Bearer " + tokenUserA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.listName").value("User A's Shopping List"));
    }

    @Test
    void testUserCannotDeleteAnotherUsersShoppingList() throws Exception {
        mockMvc.perform(
                delete("/lists/" + userBList.getId()).with(csrf()).header("Authorization", "Bearer " + tokenUserA))
                .andExpect(status().isForbidden());

        assertTrue(shoppingListRepository.findById(userBList.getId()).isPresent());
    }

    @Test
    void testUserCanDeleteOwnShoppingList() throws Exception {
        mockMvc.perform(
                delete("/lists/" + userAList.getId()).with(csrf()).header("Authorization", "Bearer " + tokenUserA))
                .andExpect(status().isNoContent());

        assertFalse(shoppingListRepository.findById(userAList.getId()).isPresent());
    }

    @Test
    void testUserCannotEditAnotherUsersShoppingList() throws Exception {
        ShoppingListUpdateRequestDTO updateDTO = new ShoppingListUpdateRequestDTO("Malicious Update");

        mockMvc.perform(put("/lists/" + userBList.getId()).with(csrf()).header("Authorization", "Bearer " + tokenUserA)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());

        ShoppingList unchangedList = shoppingListRepository.findById(userBList.getId()).orElseThrow();
        assertEquals("User B's Shopping List", unchangedList.getName());
    }

    @Test
    void testUserCanEditOwnShoppingList() throws Exception {
        ShoppingListUpdateRequestDTO updateDTO = new ShoppingListUpdateRequestDTO("Updated List Name");

        mockMvc.perform(put("/lists/" + userAList.getId()).with(csrf()).header("Authorization", "Bearer " + tokenUserA)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.listName").value("Updated List Name"));

        ShoppingList updatedList = shoppingListRepository.findById(userAList.getId()).orElseThrow();
        assertEquals("Updated List Name", updatedList.getName());
    }

    @Test
    void testUserCannotCreateListWithDifferentOwnerId() throws Exception {
        ShoppingListRequestDTO createDTO = new ShoppingListRequestDTO("New List");

        MvcResult result = mockMvc
                .perform(post("/lists").with(csrf()).header("Authorization", "Bearer " + tokenUserA)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated()).andReturn();

        String response = result.getResponse().getContentAsString();
        ShoppingListResponseDTO responseDTO = objectMapper.readValue(response, ShoppingListResponseDTO.class);

        ShoppingList createdList = shoppingListRepository.findById(responseDTO.idList()).orElseThrow();
        assertEquals(userA.getId(), createdList.getOwner().getId(),
                "List should be owned by authenticated user (User A), not a different user");
        assertNotEquals(userB.getId(), createdList.getOwner().getId(), "List should NOT be owned by User B");
    }

    @Test
    void testGetAllListsOnlyReturnsUsersOwnLists() throws Exception {
        ShoppingListRequestDTO dtoA2 = new ShoppingListRequestDTO("User A's Second List");
        ShoppingList entityA2 = listMapper.toEntity(dtoA2, userA);
        shoppingListService.saveList(entityA2);

        ShoppingListRequestDTO dtoB2 = new ShoppingListRequestDTO("User B's Second List");
        ShoppingList entityB2 = listMapper.toEntity(dtoB2, userB);
        shoppingListService.saveList(entityB2);

        mockMvc.perform(get("/lists").header("Authorization", "Bearer " + tokenUserA)).andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[?(@.listName == 'User A\\'s Shopping List')]").exists())
                .andExpect(jsonPath("$[?(@.listName == 'User A\\'s Second List')]").exists())
                .andExpect(jsonPath("$[?(@.listName == 'User B\\'s Shopping List')]").doesNotExist())
                .andExpect(jsonPath("$[?(@.listName == 'User B\\'s Second List')]").doesNotExist());
    }

    @Test
    void testUserCannotAccessNonExistentList() throws Exception {
        mockMvc.perform(get("/lists/99999").header("Authorization", "Bearer " + tokenUserA))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUnauthorizedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/lists")).andExpect(status().isForbidden());
    }

    @Test
    void testInvalidTokenIsRejected() throws Exception {
        mockMvc.perform(get("/lists").header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());
    }
}

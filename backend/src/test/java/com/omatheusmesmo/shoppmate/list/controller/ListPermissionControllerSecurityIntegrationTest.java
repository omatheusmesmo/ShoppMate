package com.omatheusmesmo.shoppmate.list.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.Permission;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.mapper.ListMapper;
import com.omatheusmesmo.shoppmate.list.repository.ListPermissionRepository;
import com.omatheusmesmo.shoppmate.list.repository.ShoppingListRepository;
import com.omatheusmesmo.shoppmate.list.service.ListPermissionService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ListPermissionControllerSecurityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ShoppingListService shoppingListService;

    @Autowired
    private ListPermissionService listPermissionService;

    @Autowired
    private ListMapper listMapper;

    @Autowired
    private ShoppingListRepository shoppingListRepository;

    @Autowired
    private ListPermissionRepository listPermissionRepository;

    private User owner;
    private User userA;
    private User userB;
    private ShoppingList ownerList;
    private ShoppingList userAList;

    private String tokenOwner;
    private String tokenUserA;
    private String tokenUserB;

    @BeforeEach
    void setUp() {
        listPermissionRepository.deleteAll();
        shoppingListRepository.deleteAll();
        userRepository.deleteAll();

        owner = new User();
        owner.setEmail("owner@test.com");
        owner.setFullName("Owner User");
        owner.setPassword(passwordEncoder.encode("password123"));
        owner.setRole("USER");
        owner = userRepository.save(owner);

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

        tokenOwner = jwtService.generateToken(owner);
        tokenUserA = jwtService.generateToken(userA);
        tokenUserB = jwtService.generateToken(userB);

        ShoppingListRequestDTO ownerListDTO = new ShoppingListRequestDTO("Owner's List");
        ShoppingList ownerListEntity = listMapper.toEntity(ownerListDTO, owner);
        ownerList = shoppingListService.saveList(ownerListEntity);

        ShoppingListRequestDTO userAListDTO = new ShoppingListRequestDTO("User A's List");
        ShoppingList userAListEntity = listMapper.toEntity(userAListDTO, userA);
        userAList = shoppingListService.saveList(userAListEntity);
    }

    @AfterEach
    void tearDown() {
        listPermissionRepository.deleteAll();
        shoppingListRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testOwnerCanGrantPermissionToAnotherUser() throws Exception {
        ListPermissionRequestDTO permissionDTO = new ListPermissionRequestDTO(ownerList.getId(), userA.getId(),
                Permission.WRITE);

        mockMvc.perform(post("/lists/" + ownerList.getId() + "/permissions").with(csrf())
                .header("Authorization", "Bearer " + tokenOwner).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(permissionDTO))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.permission").value("WRITE"));
    }

    @Test
    void testNonOwnerCannotGrantPermissionToAnotherUsersList() throws Exception {
        ListPermissionRequestDTO maliciousDTO = new ListPermissionRequestDTO(ownerList.getId(), userB.getId(),
                Permission.WRITE);

        mockMvc.perform(post("/lists/" + ownerList.getId() + "/permissions").with(csrf())
                .header("Authorization", "Bearer " + tokenUserA).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousDTO))).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testNonOwnerCannotGrantPermissionOnOwnListToAnotherUser() throws Exception {
        ListPermissionRequestDTO maliciousDTO = new ListPermissionRequestDTO(ownerList.getId(), userB.getId(),
                Permission.WRITE);

        mockMvc.perform(post("/lists/" + ownerList.getId() + "/permissions").with(csrf())
                .header("Authorization", "Bearer " + tokenUserA).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousDTO))).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testOwnerCanRevokePermission() throws Exception {
        ListPermissionRequestDTO permissionDTO = new ListPermissionRequestDTO(ownerList.getId(), userA.getId(),
                Permission.WRITE);
        listPermissionService.addListPermission(permissionDTO, owner);

        var permissions = listPermissionService.findAllPermissionsByListId(ownerList.getId(), owner);
        Long permissionId = permissions.get(0).getId();

        mockMvc.perform(delete("/lists/" + ownerList.getId() + "/permissions/" + permissionId).with(csrf())
                .header("Authorization", "Bearer " + tokenOwner)).andExpect(status().isNoContent());
    }

    @Test
    void testNonOwnerCannotRevokePermission() throws Exception {
        ListPermissionRequestDTO permissionDTO = new ListPermissionRequestDTO(ownerList.getId(), userA.getId(),
                Permission.WRITE);
        listPermissionService.addListPermission(permissionDTO, owner);

        var permissions = listPermissionService.findAllPermissionsByListId(ownerList.getId(), owner);
        Long permissionId = permissions.get(0).getId();

        mockMvc.perform(delete("/lists/" + ownerList.getId() + "/permissions/" + permissionId).with(csrf())
                .header("Authorization", "Bearer " + tokenUserB)).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());

        var remainingPermissions = listPermissionService.findAllPermissionsByListId(ownerList.getId(), owner);
        assertEquals(1, remainingPermissions.size(), "Permission should still exist");
    }

    @Test
    void testOwnerCanUpdatePermission() throws Exception {
        ListPermissionRequestDTO permissionDTO = new ListPermissionRequestDTO(ownerList.getId(), userA.getId(),
                Permission.READ);
        listPermissionService.addListPermission(permissionDTO, owner);

        var permissions = listPermissionService.findAllPermissionsByListId(ownerList.getId(), owner);
        Long permissionId = permissions.get(0).getId();

        ListPermissionRequestDTO updateDTO = new ListPermissionRequestDTO(ownerList.getId(), userA.getId(),
                Permission.WRITE);

        mockMvc.perform(put("/lists/" + ownerList.getId() + "/permissions/" + permissionId).with(csrf())
                .header("Authorization", "Bearer " + tokenOwner).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO))).andExpect(status().isOk())
                .andExpect(jsonPath("$.permission").value("WRITE"));
    }

    @Test
    void testNonOwnerCannotUpdatePermission() throws Exception {
        ListPermissionRequestDTO permissionDTO = new ListPermissionRequestDTO(ownerList.getId(), userA.getId(),
                Permission.READ);
        listPermissionService.addListPermission(permissionDTO, owner);

        var permissions = listPermissionService.findAllPermissionsByListId(ownerList.getId(), owner);
        Long permissionId = permissions.get(0).getId();

        ListPermissionRequestDTO maliciousUpdate = new ListPermissionRequestDTO(ownerList.getId(), userA.getId(),
                Permission.WRITE);

        mockMvc.perform(put("/lists/" + ownerList.getId() + "/permissions/" + permissionId).with(csrf())
                .header("Authorization", "Bearer " + tokenUserB).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousUpdate))).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());

        var unchangedPermissions = listPermissionService.findAllPermissionsByListId(ownerList.getId(), owner);
        assertEquals(Permission.READ, unchangedPermissions.get(0).getPermission(), "Permission should remain as READ");
    }

    @Test
    void testUserCanViewPermissionsOnOwnList() throws Exception {
        ListPermissionRequestDTO permissionDTO = new ListPermissionRequestDTO(ownerList.getId(), userA.getId(),
                Permission.WRITE);
        listPermissionService.addListPermission(permissionDTO, owner);

        mockMvc.perform(
                get("/lists/" + ownerList.getId() + "/permissions").header("Authorization", "Bearer " + tokenOwner))
                .andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].permission").value("WRITE"));
    }

    @Test
    void testUserCannotViewPermissionsOnAnotherUsersList() throws Exception {
        ListPermissionRequestDTO permissionDTO = new ListPermissionRequestDTO(ownerList.getId(), userA.getId(),
                Permission.WRITE);
        listPermissionService.addListPermission(permissionDTO, owner);

        mockMvc.perform(
                get("/lists/" + ownerList.getId() + "/permissions").header("Authorization", "Bearer " + tokenUserB))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testUserWithReadPermissionCannotViewPermissions() throws Exception {
        ListPermissionRequestDTO permissionDTO = new ListPermissionRequestDTO(ownerList.getId(), userA.getId(),
                Permission.READ);
        listPermissionService.addListPermission(permissionDTO, owner);

        mockMvc.perform(
                get("/lists/" + ownerList.getId() + "/permissions").header("Authorization", "Bearer " + tokenUserA))
                .andExpect(status().isForbidden());
    }

    @Test
    void testNonOwnerCannotGrantPermissionWithIdSpoofing() throws Exception {
        ListPermissionRequestDTO maliciousDTO = new ListPermissionRequestDTO(ownerList.getId(), userA.getId(),
                Permission.WRITE);

        mockMvc.perform(post("/lists/" + ownerList.getId() + "/permissions").with(csrf())
                .header("Authorization", "Bearer " + tokenUserA).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousDTO))).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());

        var permissions = listPermissionService.findAllPermissionsByListId(ownerList.getId(), owner);
        assertTrue(permissions.isEmpty(), "No permissions should be granted");
    }

    @Test
    void testOwnerCanGrantPermissionToAnyUser() throws Exception {
        ListPermissionRequestDTO dtoA = new ListPermissionRequestDTO(ownerList.getId(), userA.getId(),
                Permission.WRITE);

        mockMvc.perform(post("/lists/" + ownerList.getId() + "/permissions").with(csrf())
                .header("Authorization", "Bearer " + tokenOwner).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoA))).andExpect(status().isCreated());

        ListPermissionRequestDTO dtoB = new ListPermissionRequestDTO(ownerList.getId(), userB.getId(), Permission.READ);

        mockMvc.perform(post("/lists/" + ownerList.getId() + "/permissions").with(csrf())
                .header("Authorization", "Bearer " + tokenOwner).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoB))).andExpect(status().isCreated());

        var permissions = listPermissionService.findAllPermissionsByListId(ownerList.getId(), owner);
        assertEquals(2, permissions.size(), "Owner should be able to grant permissions to multiple users");
    }

    @Test
    void testUnauthorizedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/lists/" + ownerList.getId() + "/permissions")).andExpect(status().isForbidden());
    }

    @Test
    void testInvalidTokenIsRejected() throws Exception {
        mockMvc.perform(get("/lists/" + ownerList.getId() + "/permissions").header("Authorization",
                "Bearer invalid.token.here")).andExpect(status().isForbidden());
    }
}

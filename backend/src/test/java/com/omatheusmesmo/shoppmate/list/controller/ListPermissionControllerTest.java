package com.omatheusmesmo.shoppmate.list.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.auth.service.CustomUserDetailsService;
import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionResponseDTO;
import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionSummaryDTO;
import com.omatheusmesmo.shoppmate.list.entity.ListPermission;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.mapper.ListPermissionMapper;
import com.omatheusmesmo.shoppmate.list.service.ListPermissionService;
import com.omatheusmesmo.shoppmate.shared.test.annotation.WithMockCustomUser;
import com.omatheusmesmo.shoppmate.shared.testutils.ListTestFactory;
import com.omatheusmesmo.shoppmate.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ListPermissionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ListPermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListPermissionService listPermissionService;

    @MockBean
    private ListPermissionMapper listPermissionMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private ShoppingList shoppingList;
    private ListPermission listPermission;
    private ListPermissionSummaryDTO summaryDTO;
    private ListPermissionResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        shoppingList = ListTestFactory.createValidShoppingList();
        listPermission = ListTestFactory.createValidListPermission(shoppingList);

        summaryDTO = new ListPermissionSummaryDTO(listPermission.getId(), listPermission.getUser().getFullName(),
                listPermission.getUser().getEmail(), listPermission.getPermission());

        responseDTO = new ListPermissionResponseDTO(listPermission.getId(), null, null, listPermission.getPermission());
    }

    @Test
    @WithMockCustomUser
    void getAllListPermissions_ExistingListId_ReturnsOkWithPermissions() throws Exception {
        // Arrange
        when(listPermissionService.findAllPermissionsByListId(eq(shoppingList.getId()), any(User.class)))
                .thenReturn(List.of(listPermission));
        when(listPermissionMapper.toSummaryDTO(any(ListPermission.class))).thenReturn(summaryDTO);

        // Act & Assert
        mockMvc.perform(get("/lists/" + shoppingList.getId() + "/permissions")).andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(summaryDTO))));
    }

    @Test
    @WithMockCustomUser
    void addListPermission_ValidRequest_ReturnsCreatedWithPermission() throws Exception {
        // Arrange
        ListPermissionRequestDTO requestDTO = ListTestFactory.createValidListPermissionRequestDTO(shoppingList.getId(),
                listPermission.getUser().getId());
        when(listPermissionService.addListPermission(any(ListPermissionRequestDTO.class), any(User.class)))
                .thenReturn(listPermission);
        when(listPermissionMapper.toResponseDTO(any(ListPermission.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/lists/" + shoppingList.getId() + "/permissions").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDTO)));
    }
}

package com.omatheusmesmo.shoppmate.list.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.auth.service.CustomUserDetailsService;
import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListResponseDTO;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.mapper.ListMapper;
import com.omatheusmesmo.shoppmate.list.service.ShoppingListService;
import com.omatheusmesmo.shoppmate.shared.test.annotation.WithMockCustomUser;
import com.omatheusmesmo.shoppmate.shared.testutils.ListTestFactory;
import com.omatheusmesmo.shoppmate.user.dtos.UserResponseDTO;
import com.omatheusmesmo.shoppmate.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ShoppingListController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShoppingListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShoppingListService shoppingListService;

    @MockBean
    private ListMapper listMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private ShoppingList shoppingList;
    private ShoppingListResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        shoppingList = ListTestFactory.createValidShoppingList();

        responseDTO = new ShoppingListResponseDTO(
                shoppingList.getId(), shoppingList.getName(), new UserResponseDTO(shoppingList.getOwner().getId(),
                        shoppingList.getOwner().getFullName(), shoppingList.getOwner().getEmail()),
                BigDecimal.valueOf(1.0));
    }

    @Test
    @WithMockCustomUser
    void getAllShoppingLists_ExistingLists_ReturnsOkWithShoppingLists() throws Exception {
        // Arrange
        when(shoppingListService.findAllByUser(any(User.class))).thenReturn(List.of(shoppingList));
        when(listMapper.toResponseDTO(any(ShoppingList.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/lists")).andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(responseDTO))));
    }

    @Test
    @WithMockCustomUser
    void addShoppingList_ValidRequest_ReturnsCreatedWithShoppingList() throws Exception {
        // Arrange
        ShoppingListRequestDTO requestDTO = ListTestFactory.createValidShoppingListRequestDTO();
        ShoppingList entityFromRequest = new ShoppingList();
        entityFromRequest.setId(shoppingList.getId());
        entityFromRequest.setName(requestDTO.name());
        entityFromRequest.setOwner(shoppingList.getOwner());

        ShoppingListResponseDTO expectedResponseDTO = new ShoppingListResponseDTO(entityFromRequest.getId(),
                entityFromRequest.getName(), new UserResponseDTO(entityFromRequest.getOwner().getId(),
                        entityFromRequest.getOwner().getFullName(), entityFromRequest.getOwner().getEmail()),
                BigDecimal.valueOf(1.0));

        when(listMapper.toEntity(eq(requestDTO), any(User.class))).thenReturn(entityFromRequest);
        when(shoppingListService.saveList(any(ShoppingList.class))).thenReturn(entityFromRequest);
        when(listMapper.toResponseDTO(entityFromRequest)).thenReturn(expectedResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/lists").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResponseDTO)));
    }
}

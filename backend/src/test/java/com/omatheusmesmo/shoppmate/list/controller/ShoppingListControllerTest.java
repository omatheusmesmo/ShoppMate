package com.omatheusmesmo.shoppmate.list.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListResponseDTO;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.mapper.ListMapper;
import com.omatheusmesmo.shoppmate.list.service.ShoppingListService;
import com.omatheusmesmo.shoppmate.user.dtos.UserResponseDTO;
import com.omatheusmesmo.shoppmate.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private ShoppingList shoppingList;
    private ShoppingListResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@example.com");
        owner.setFullName("Owner");
        owner.setPassword("pass");

        shoppingList = new ShoppingList();
        shoppingList.setId(1L);
        shoppingList.setName("Weekly");
        shoppingList.setOwner(owner);

        responseDTO = new ShoppingListResponseDTO(1L, "Weekly", new UserResponseDTO(1L, "Owner", "owner@example.com"),
                BigDecimal.valueOf(1.0));
    }

    @Test
    @WithMockUser
    void getAllShoppingLists() throws Exception {
        when(shoppingListService.findAll()).thenReturn(List.of(shoppingList));
        when(listMapper.toResponseDTO(any(ShoppingList.class))).thenReturn(responseDTO);

        mockMvc.perform(get("/lists")).andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(responseDTO))));
    }

    @Test
    @WithMockUser
    void addShoppingList() throws Exception {
        ShoppingListRequestDTO requestDTO = new ShoppingListRequestDTO("Weekly", 1L);
        when(listMapper.toEntity(any(ShoppingListRequestDTO.class))).thenReturn(shoppingList);
        when(shoppingListService.saveList(any(ShoppingList.class))).thenReturn(shoppingList);
        when(listMapper.toResponseDTO(any(ShoppingList.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/lists").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDTO)));
    }
}

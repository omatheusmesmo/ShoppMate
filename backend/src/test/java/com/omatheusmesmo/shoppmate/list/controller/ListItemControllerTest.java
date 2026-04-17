package com.omatheusmesmo.shoppmate.list.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.auth.service.CustomUserDetailsService;
import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemResponseDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemSummaryDTO;
import com.omatheusmesmo.shoppmate.list.entity.ListItem;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.mapper.ListItemMapper;
import com.omatheusmesmo.shoppmate.list.service.ListItemService;
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

@WebMvcTest(controllers = ListItemController.class)
@AutoConfigureMockMvc(addFilters = false)
class ListItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListItemService listItemService;

    @MockBean
    private ListItemMapper listItemMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private ShoppingList shoppingList;
    private ListItem listItem;
    private ListItemSummaryDTO summaryDTO;
    private ListItemResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        shoppingList = ListTestFactory.createValidShoppingList();
        listItem = ListTestFactory.createValidListItem(shoppingList);

        summaryDTO = new ListItemSummaryDTO(listItem.getId(), listItem.getItem().getId(), listItem.getItem().getName(),
                listItem.getQuantity(), listItem.getPurchased());

        responseDTO = new ListItemResponseDTO(null, null, listItem.getItem().getId(), listItem.getQuantity(),
                listItem.getPurchased(), listItem.getUnitPrice(), listItem.getUnitPrice());
    }

    @Test
    @WithMockCustomUser
    void getAllListItemsByListId_ExistingListId_ReturnsOkWithListItems() throws Exception {
        // Arrange
        when(listItemService.findAll(eq(shoppingList.getId()), any(User.class))).thenReturn(List.of(listItem));
        when(listItemMapper.toSummaryDTO(any(ListItem.class))).thenReturn(summaryDTO);

        // Act & Assert
        mockMvc.perform(get("/lists/" + shoppingList.getId() + "/items")).andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(summaryDTO))));
    }

    @Test
    @WithMockCustomUser
    void addListItem_ValidRequest_ReturnsCreatedWithListItem() throws Exception {
        // Arrange
        ListItemRequestDTO requestDTO = ListTestFactory.createValidListItemRequestDTO(shoppingList.getId(),
                listItem.getItem().getId());
        when(listItemService.addShoppItemList(any(ListItemRequestDTO.class), any(User.class))).thenReturn(listItem);
        when(listItemMapper.toResponseDTO(any(ListItem.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/lists/" + shoppingList.getId() + "/items").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDTO)));
    }
}

package com.omatheusmesmo.shoppmate.list.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemResponseDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemSummaryDTO;
import com.omatheusmesmo.shoppmate.list.entity.ListItem;
import com.omatheusmesmo.shoppmate.list.mapper.ListItemMapper;
import com.omatheusmesmo.shoppmate.list.service.ListItemService;
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
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private ListItem listItem;
    private ListItemSummaryDTO summaryDTO;
    private ListItemResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        listItem = new ListItem();
        listItem.setId(1L);
        listItem.setQuantity(2);
        listItem.setPurchased(false);
        summaryDTO = new ListItemSummaryDTO(1L, 1L, "Rice", 2, false);
        responseDTO = new ListItemResponseDTO(null, null, 1L, 2, false, BigDecimal.valueOf(1.0),
                BigDecimal.valueOf(1.0));
    }

    @Test
    @WithMockUser
    void getAllListItemsByListId() throws Exception {
        when(listItemService.findAll(1L)).thenReturn(List.of(listItem));
        when(listItemMapper.toSummaryDTO(any(ListItem.class))).thenReturn(summaryDTO);

        mockMvc.perform(get("/lists/1/items")).andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(summaryDTO))));
    }

    @Test
    @WithMockUser
    void addListItem() throws Exception {
        ListItemRequestDTO requestDTO = new ListItemRequestDTO(1L, 1L, 2, BigDecimal.valueOf(1.0));
        when(listItemService.addShoppItemList(any(ListItemRequestDTO.class))).thenReturn(listItem);
        when(listItemMapper.toResponseDTO(any(ListItem.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/lists/1/items").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDTO)));
    }
}

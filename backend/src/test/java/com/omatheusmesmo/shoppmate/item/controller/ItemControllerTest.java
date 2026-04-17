package com.omatheusmesmo.shoppmate.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.category.dto.CategoryResponseDTO;
import com.omatheusmesmo.shoppmate.item.dto.ItemRequestDTO;
import com.omatheusmesmo.shoppmate.item.dto.ItemResponseDTO;
import com.omatheusmesmo.shoppmate.item.entity.Item;
import com.omatheusmesmo.shoppmate.item.mapper.ItemMapper;
import com.omatheusmesmo.shoppmate.shared.testutils.ItemTestFactory;
import com.omatheusmesmo.shoppmate.unit.dto.UnitResponseDTO;
import com.omatheusmesmo.shoppmate.item.service.ItemService;
import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@AutoConfigureMockMvc(addFilters = false)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @MockBean
    private ItemMapper itemMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private Item item1;
    private Item item2;
    private ItemResponseDTO itemResponseDTO1;
    private ItemResponseDTO itemResponseDTO2;

    @BeforeEach
    void setUp() {
        item1 = ItemTestFactory.createValidItem();
        item2 = ItemTestFactory.createValidItem();
        // Ensure unique IDs
        if (item1.getId().equals(item2.getId())) {
            item2.setId(item1.getId() + 1);
        }

        CategoryResponseDTO categoryDTO1 = new CategoryResponseDTO(item1.getCategory().getId(),
                item1.getCategory().getName());
        UnitResponseDTO unitDTO1 = new UnitResponseDTO(item1.getUnit().getId(), item1.getUnit().getName());
        itemResponseDTO1 = new ItemResponseDTO(item1.getId(), item1.getName(), categoryDTO1, unitDTO1);

        CategoryResponseDTO categoryDTO2 = new CategoryResponseDTO(item2.getCategory().getId(),
                item2.getCategory().getName());
        UnitResponseDTO unitDTO2 = new UnitResponseDTO(item2.getUnit().getId(), item2.getUnit().getName());
        itemResponseDTO2 = new ItemResponseDTO(item2.getId(), item2.getName(), categoryDTO2, unitDTO2);
    }

    @Test
    @WithMockUser
    void testGetAllItems() throws Exception {
        // Arrange
        List<Item> allItems = Arrays.asList(item1, item2);
        List<ItemResponseDTO> allItemDTOs = Arrays.asList(itemResponseDTO1, itemResponseDTO2);

        when(itemService.findAll()).thenReturn(allItems);
        when(itemMapper.toResponseDTO(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            if (item.getId().equals(item1.getId()))
                return itemResponseDTO1;
            return itemResponseDTO2;
        });

        // Act & Assert
        mockMvc.perform(get("/item")).andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(allItemDTOs)));
    }

    @Test
    @WithMockUser
    void testPostAddItem() throws Exception {
        // Arrange
        ItemRequestDTO requestDTO = new ItemRequestDTO(item1.getName(), item1.getCategory().getId(),
                item1.getUnit().getId());

        when(itemMapper.toEntity(any(ItemRequestDTO.class))).thenReturn(item1);
        when(itemService.addItem(any(Item.class))).thenReturn(item1);
        when(itemMapper.toResponseDTO(any(Item.class))).thenReturn(itemResponseDTO1);

        // Act & Assert
        mockMvc.perform(post("/item").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(itemResponseDTO1)));
    }

    @Test
    @WithMockUser
    void testDeleteRemoveItem() throws Exception {
        // Arrange
        Long id = item1.getId();
        Mockito.doNothing().when(itemService).removeItem(id);

        // Act & Assert
        mockMvc.perform(delete("/item/" + id)).andExpect(status().isNoContent());
        verify(itemService, times(1)).removeItem(id);
    }

    @Test
    @WithMockUser
    void testPutEditItem() throws Exception {
        // Arrange
        ItemRequestDTO requestDTO = new ItemRequestDTO(item1.getName(), item1.getCategory().getId(),
                item1.getUnit().getId());

        when(itemMapper.toEntity(any(ItemRequestDTO.class))).thenReturn(item1);
        when(itemService.editItem(any(Item.class))).thenReturn(item1);
        when(itemMapper.toResponseDTO(any(Item.class))).thenReturn(itemResponseDTO1);

        // Act & Assert
        mockMvc.perform(put("/item/" + item1.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(itemResponseDTO1)));
    }

    @Test
    @WithMockUser
    void testPostAddItem_BadRequest() throws Exception {
        // Arrange
        when(itemMapper.toEntity(any(ItemRequestDTO.class))).thenThrow(new IllegalArgumentException("Invalid item"));

        ItemRequestDTO invalidItem = new ItemRequestDTO("", item1.getCategory().getId(), item1.getUnit().getId());

        // Act & Assert
        mockMvc.perform(post("/item").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidItem))).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testPutEditItem_NotFound() throws Exception {
        // Arrange
        ItemRequestDTO requestDTO = new ItemRequestDTO(item1.getName(), item1.getCategory().getId(),
                item1.getUnit().getId());

        when(itemMapper.toEntity(any(ItemRequestDTO.class))).thenReturn(item1);
        doThrow(new NoSuchElementException()).when(itemService).editItem(any(Item.class));

        // Act & Assert
        mockMvc.perform(put("/item/" + item1.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isNotFound());
    }
}

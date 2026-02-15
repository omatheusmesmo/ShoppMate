package com.omatheusmesmo.shoppmate.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.category.dto.CategoryResponseDTO;
import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.item.dto.ItemRequestDTO;
import com.omatheusmesmo.shoppmate.item.dto.ItemResponseDTO;
import com.omatheusmesmo.shoppmate.item.entity.Item;
import com.omatheusmesmo.shoppmate.item.mapper.ItemMapper;
import com.omatheusmesmo.shoppmate.unit.dto.UnitResponseDTO;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
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
    private Category category;
    private Unit unit;
    private ItemResponseDTO itemResponseDTO1;
    private ItemResponseDTO itemResponseDTO2;

    @BeforeEach
    void setUp() {
        unit = new Unit();
        unit.setId(1L);
        unit.setName("kg");
        unit.setSymbol("kg");

        category = new Category();
        category.setId(1L);
        category.setName("Food");

        item1 = new Item();
        item1.setId(1L);
        item1.setName("Feijão");
        item1.setUnit(unit);
        item1.setCategory(category);

        item2 = new Item();
        item2.setId(2L);
        item2.setName("Arroz");
        item2.setUnit(unit);
        item2.setCategory(category);

        CategoryResponseDTO categoryResponseDTO = new CategoryResponseDTO(1L, "Food");
        UnitResponseDTO unitResponseDTO = new UnitResponseDTO(1L, "kg");

        itemResponseDTO1 = new ItemResponseDTO(1L, "Feijão", categoryResponseDTO, unitResponseDTO);
        itemResponseDTO2 = new ItemResponseDTO(2L, "Arroz", categoryResponseDTO, unitResponseDTO);
    }

    @Test
    @WithMockUser
    void testGetAllItems() throws Exception {
        List<Item> allItems = Arrays.asList(item1, item2);
        List<ItemResponseDTO> allItemDTOs = Arrays.asList(itemResponseDTO1, itemResponseDTO2);

        when(itemService.findAll()).thenReturn(allItems);
        when(itemMapper.toResponseDTO(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            if (item.getId().equals(1L))
                return itemResponseDTO1;
            return itemResponseDTO2;
        });

        mockMvc.perform(get("/item")).andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(allItemDTOs)));
    }

    @Test
    @WithMockUser
    void testPostAddItem() throws Exception {
        ItemRequestDTO requestDTO = new ItemRequestDTO("Feijão", 1L, 1L);

        when(itemMapper.toEntity(any(ItemRequestDTO.class))).thenReturn(item1);
        when(itemService.addItem(any(Item.class))).thenReturn(item1);
        when(itemMapper.toResponseDTO(any(Item.class))).thenReturn(itemResponseDTO1);

        mockMvc.perform(post("/item").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(itemResponseDTO1)));
    }

    @Test
    @WithMockUser
    void testDeleteRemoveItem() throws Exception {
        Long id = 1L;

        Mockito.doNothing().when(itemService).removeItem(id);

        mockMvc.perform(delete("/item/" + id)).andExpect(status().isNoContent());

        verify(itemService, times(1)).removeItem(id);
    }

    @Test
    @WithMockUser
    void testPutEditItem() throws Exception {
        ItemRequestDTO requestDTO = new ItemRequestDTO("Feijão", 1L, 1L);

        when(itemMapper.toEntity(any(ItemRequestDTO.class))).thenReturn(item1);
        when(itemService.editItem(any(Item.class))).thenReturn(item1);
        when(itemMapper.toResponseDTO(any(Item.class))).thenReturn(itemResponseDTO1);

        mockMvc.perform(put("/item/1").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(itemResponseDTO1)));
    }

    @Test
    @WithMockUser
    void testPostAddItem_BadRequest() throws Exception {
        when(itemMapper.toEntity(any(ItemRequestDTO.class))).thenThrow(new IllegalArgumentException("Invalid item"));
        ;

        ItemRequestDTO invalidItem = new ItemRequestDTO("", 1L, 1L);

        mockMvc.perform(post("/item").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidItem))).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testPutEditItem_NotFound() throws Exception {
        ItemRequestDTO requestDTO = new ItemRequestDTO("Feijão", 1L, 1L);

        when(itemMapper.toEntity(any(ItemRequestDTO.class))).thenReturn(item1);
        doThrow(new NoSuchElementException()).when(itemService).editItem(any(Item.class));

        mockMvc.perform(put("/item/1").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isNotFound());
    }
}

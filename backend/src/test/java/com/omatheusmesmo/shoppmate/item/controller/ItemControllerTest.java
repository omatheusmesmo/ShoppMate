package com.omatheusmesmo.shoppmate.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.item.entity.Item;
import com.omatheusmesmo.shoppmate.item.mapper.ItemMapper;
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

    @BeforeEach
    void setUp() {
        unit = new Unit();
        unit.setId(1L);
        unit.setName("kg");

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
    }

    @Test
    @WithMockUser
    void testGetAllItems() throws Exception {
        List<Item> allItems = Arrays.asList(item1, item2);

        when(itemService.findAll()).thenReturn(allItems);

        mockMvc.perform(get("/item"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(allItems)));
    }

    @Test
    @WithMockUser
    void testPostAddItem() throws Exception {
        Item newItem = new Item();
        newItem.setId(1L);
        newItem.setName("Feijão");
        newItem.setUnit(unit);
        newItem.setCategory(category);

        when(itemService.addItem(any(Item.class))).thenReturn(newItem);

        mockMvc.perform(post("/item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(newItem)));
    }

    @Test
    @WithMockUser
    void testDeleteRemoveItem() throws Exception {
        Long id = 1L;

        Mockito.doNothing().when(itemService).removeItem(id);

        mockMvc.perform(delete("/item/" + id))
                .andExpect(status().isNoContent());

        verify(itemService, times(1)).removeItem(id);
    }

    @Test
    @WithMockUser
    void testPutEditItem() throws Exception {
        Item editedItem = new Item();
        editedItem.setId(1L);
        editedItem.setName("Feijão");
        editedItem.setUnit(unit);
        editedItem.setCategory(category);

        when(itemService.editItem(any(Item.class))).thenReturn(editedItem);

        mockMvc.perform(put("/item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editedItem)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(editedItem)));
    }

    @Test
    @WithMockUser
    void testPostAddItem_BadRequest() throws Exception {
        when(itemService.addItem(any(Item.class))).thenThrow(new IllegalArgumentException());

        Item invalidItem = new Item();
        invalidItem.setName("");
        invalidItem.setUnit(unit);
        invalidItem.setCategory(category);

        mockMvc.perform(post("/item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testPutEditItem_NotFound() throws Exception {
        doThrow(new NoSuchElementException()).when(itemService).editItem(any(Item.class));

        Item item = new Item();
        item.setName("Feijão");
        item.setUnit(unit);
        item.setCategory(category);

        mockMvc.perform(put("/item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isNotFound());
    }
}
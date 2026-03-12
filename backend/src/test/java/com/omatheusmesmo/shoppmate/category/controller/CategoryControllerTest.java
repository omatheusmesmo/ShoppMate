package com.omatheusmesmo.shoppmate.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import com.omatheusmesmo.shoppmate.category.dto.CategoryRequestDTO;
import com.omatheusmesmo.shoppmate.category.dto.CategoryResponseDTO;
import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.category.mapper.CategoryMapper;
import com.omatheusmesmo.shoppmate.category.service.CategoryService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private CategoryMapper categoryMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private Category category1;
    private Category category2;

    CategoryResponseDTO category1ResponseDTO;
    CategoryResponseDTO category2ResponseDTO;

    @BeforeEach
    void setUp() {
        category1 = new Category();
        category1.setId(1L);
        category1.setName("Food");

        category2 = new Category();
        category2.setId(2L);
        category2.setName("Toy");

        category1ResponseDTO = new CategoryResponseDTO(1L, "Food");
        category2ResponseDTO = new CategoryResponseDTO(2L, "Toy");
    }

    @Test
    @WithMockUser
    void testGetAllCategories() throws Exception {
        List<Category> allCategories = Arrays.asList(category1, category2);
        List<CategoryResponseDTO> allCategoriesDTOs = Arrays.asList(category1ResponseDTO, category2ResponseDTO);

        when(categoryService.findAll()).thenReturn(allCategories);
        when(categoryMapper.toResponseDTO(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            if (category.getId().equals(1L))
                return category1ResponseDTO;
            return category2ResponseDTO;
        });

        mockMvc.perform(get("/category")).andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(allCategoriesDTOs)));
    }

    @Test
    @WithMockUser
    void testPostAddCategory() throws Exception {
        CategoryRequestDTO requestDTO = new CategoryRequestDTO("Food");

        when(categoryMapper.toEntity(any(CategoryRequestDTO.class))).thenReturn(category1);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category1);
        when(categoryMapper.toResponseDTO(any(Category.class))).thenReturn(category1ResponseDTO);

        mockMvc.perform(post("/category").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(category1ResponseDTO)));
    }

    @Test
    @WithMockUser
    void testDeleteRemoveCategory() throws Exception {
        Long id = 1L;

        Mockito.doNothing().when(categoryService).removeCategory(id);

        mockMvc.perform(delete("/category/" + id)).andExpect(status().isNoContent());

        verify(categoryService, times(1)).removeCategory(id);

    }

    @Test
    @WithMockUser
    void testPutEditCategory() throws Exception {
        Long id = 1L;

        CategoryRequestDTO requestDTO = new CategoryRequestDTO("Updated Food");

        Category existingCategory = new Category();
        existingCategory.setId(id);
        existingCategory.setName("Old Name");

        Category updatedCategory = new Category();
        updatedCategory.setId(id);
        updatedCategory.setName("Updated Food");

        CategoryResponseDTO responseDTO = new CategoryResponseDTO(id, "Updated Food");

        when(categoryService.findCategoryById(id)).thenReturn(existingCategory);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(updatedCategory);
        when(categoryMapper.toResponseDTO(updatedCategory)).thenReturn(responseDTO);

        mockMvc.perform(put("/category/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDTO)));
    }
}

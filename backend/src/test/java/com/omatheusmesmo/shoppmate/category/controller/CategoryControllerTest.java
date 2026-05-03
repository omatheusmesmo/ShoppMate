package com.omatheusmesmo.shoppmate.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omatheusmesmo.shoppmate.auth.service.CustomUserDetailsService;
import com.omatheusmesmo.shoppmate.auth.service.JwtService;
import com.omatheusmesmo.shoppmate.category.dto.CategoryRequestDTO;
import com.omatheusmesmo.shoppmate.category.dto.CategoryResponseDTO;
import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.category.mapper.CategoryMapper;
import com.omatheusmesmo.shoppmate.category.service.CategoryService;
import com.omatheusmesmo.shoppmate.shared.test.annotation.WithMockCustomUser;
import com.omatheusmesmo.shoppmate.shared.testutils.CategoryTestFactory;
import com.omatheusmesmo.shoppmate.shared.testutils.UserTestFactory;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private Category category1;
    private Category category2;
    private User user;

    private CategoryResponseDTO category1ResponseDTO;
    private CategoryResponseDTO category2ResponseDTO;

    @BeforeEach
    void setUp() {
        user = UserTestFactory.createValidUser();
        category1 = CategoryTestFactory.createValidCategory();
        category2 = CategoryTestFactory.createValidCategory();

        UserResponseDTO ownerDTO = new UserResponseDTO(user.getId(), user.getFullName(), user.getEmail());
        category1ResponseDTO = new CategoryResponseDTO(category1.getId(), category1.getName(), false, ownerDTO);
        category2ResponseDTO = new CategoryResponseDTO(category2.getId(), category2.getName(), false, ownerDTO);
    }

    @Test
    @WithMockCustomUser
    void testGetAllCategories() throws Exception {
        // Arrange
        List<Category> allCategories = Arrays.asList(category1, category2);
        List<CategoryResponseDTO> allCategoriesDTOs = Arrays.asList(category1ResponseDTO, category2ResponseDTO);

        when(categoryService.findAllAccessibleByUser(any())).thenReturn(allCategories);
        when(categoryMapper.toResponseDTO(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            if (category.getId().equals(category1.getId()))
                return category1ResponseDTO;
            return category2ResponseDTO;
        });

        // Act & Assert
        mockMvc.perform(get("/category")).andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(allCategoriesDTOs)));
    }

    @Test
    @WithMockCustomUser
    void testPostAddCategory() throws Exception {
        // Arrange
        CategoryRequestDTO requestDTO = new CategoryRequestDTO(category1.getName());

        when(categoryMapper.toEntity(any(CategoryRequestDTO.class), any(User.class))).thenReturn(category1);
        when(categoryService.saveCategory(any(Category.class))).thenReturn(category1);
        when(categoryMapper.toResponseDTO(any(Category.class))).thenReturn(category1ResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/category").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(category1ResponseDTO)));
    }

    @Test
    @WithMockCustomUser
    void testDeleteRemoveCategory() throws Exception {
        // Arrange
        Long id = category1.getId();

        // Act & Assert
        mockMvc.perform(delete("/category/" + id)).andExpect(status().isNoContent());
        verify(categoryService, times(1)).removeCategory(eq(id), any(User.class));
    }

    @Test
    @WithMockCustomUser
    void testPutEditCategory() throws Exception {
        // Arrange
        Long id = category1.getId();
        String updatedName = category1.getName() + " Updated";
        CategoryRequestDTO requestDTO = new CategoryRequestDTO(updatedName);

        Category updatedCategory = new Category();
        updatedCategory.setId(id);
        updatedCategory.setName(updatedName);

        CategoryResponseDTO responseDTO = new CategoryResponseDTO(id, updatedName, false, null);

        when(categoryMapper.toEntity(any(CategoryRequestDTO.class), any(User.class))).thenReturn(category1);
        doNothing().when(categoryService).editCategory(eq(id), any(Category.class), any(User.class));
        when(categoryService.findCategoryById(id)).thenReturn(updatedCategory);
        when(categoryMapper.toResponseDTO(updatedCategory)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put("/category/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDTO)));
    }
}

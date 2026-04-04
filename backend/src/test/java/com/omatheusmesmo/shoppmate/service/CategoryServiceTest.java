package com.omatheusmesmo.shoppmate.service;

import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.category.repository.CategoryRepository;
import com.omatheusmesmo.shoppmate.category.service.CategoryService;
import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Food");
    }

    @Test
    void saveCategory() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category result = categoryService.saveCategory(category);

        assertNotNull(result);
        assertEquals("Food", result.getName());
        verify(auditService, times(1)).setAuditData(category, true);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void findCategoryById() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category found = categoryService.findCategoryById(1L);

        assertEquals(category.getId(), found.getId());
    }

    @Test
    void findCategoryById_NotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> categoryService.findCategoryById(1L));
    }

    @Test
    void findAll() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<Category> result = categoryService.findAll();

        assertEquals(1, result.size());
        verify(categoryRepository, times(1)).findAll();
    }
}

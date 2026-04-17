package com.omatheusmesmo.shoppmate.category.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.category.repository.CategoryRepository;
import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import com.omatheusmesmo.shoppmate.shared.testutils.CategoryTestFactory;

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
        category = CategoryTestFactory.createValidCategory();
    }

    @Test
    void saveCategory_ValidCategory_ReturnsSavedCategory() {
        // Arrange
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // Act
        Category result = categoryService.saveCategory(category);

        // Assert
        assertNotNull(result);
        assertEquals(category.getName(), result.getName());
        verify(auditService, times(1)).setAuditData(category, true);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void findCategoryById_ExistingId_ReturnsCategory() {
        // Arrange
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));

        // Act
        Category found = categoryService.findCategoryById(category.getId());

        // Assert
        assertEquals(category.getId(), found.getId());
        assertEquals(category.getName(), found.getName());
    }

    @Test
    void findCategoryById_MissingId_ThrowsNoSuchElementException() {
        // Arrange
        Long missingId = category.getId() + 1000;
        when(categoryRepository.findById(missingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> categoryService.findCategoryById(missingId));
    }

    @Test
    void findAll_ExistingCategories_ReturnsCategoryList() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        // Act
        List<Category> result = categoryService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(category, result.get(0));
        verify(categoryRepository, times(1)).findAll();
    }
}

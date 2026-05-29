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
import com.omatheusmesmo.shoppmate.shared.testutils.UserTestFactory;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.utils.exception.ResourceOwnershipException;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private User user;

    @BeforeEach
    void setUp() {
        user = UserTestFactory.createValidUser();
        category = CategoryTestFactory.createValidCategory();
        category.setOwner(user);
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
        when(categoryRepository.findByIdAndDeletedFalse(category.getId())).thenReturn(Optional.of(category));

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
        when(categoryRepository.findByIdAndDeletedFalse(missingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> categoryService.findCategoryById(missingId));
    }

    @Test
    void findAllAccessibleByUser_ReturnsAccessibleCategories() {
        // Arrange
        when(categoryRepository.findAllAccessibleByUserId(user.getId())).thenReturn(List.of(category));

        // Act
        List<Category> result = categoryService.findAllAccessibleByUser(user.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(category, result.get(0));
        verify(categoryRepository, times(1)).findAllAccessibleByUserId(user.getId());
    }

    @Test
    void removeCategory_NotFound_ThrowsNoSuchElementException() {
        // Arrange
        when(categoryRepository.findByIdAndDeletedFalse(category.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> categoryService.removeCategory(category.getId(), user));
    }

    @Test
    void removeCategory_UserOwner_Succeeds() {
        // Arrange
        when(categoryRepository.findByIdAndDeletedFalse(category.getId())).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // Act
        categoryService.removeCategory(category.getId(), user);

        // Assert
        verify(auditService, times(1)).softDelete(category);
        verify(auditService, times(1)).setAuditData(category, false);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void removeCategory_SystemStandard_ThrowsResourceOwnershipException() {
        // Arrange
        Category systemCategory = CategoryTestFactory.createValidSystemCategory();
        when(categoryRepository.findByIdAndDeletedFalse(systemCategory.getId()))
                .thenReturn(Optional.of(systemCategory));

        // Act & Assert
        assertThrows(ResourceOwnershipException.class,
                () -> categoryService.removeCategory(systemCategory.getId(), user));
    }

    @Test
    void removeCategory_OtherUserOwner_ThrowsResourceOwnershipException() {
        // Arrange
        User otherOwner = UserTestFactory.createValidUser();
        category.setOwner(otherOwner);
        when(categoryRepository.findByIdAndDeletedFalse(category.getId())).thenReturn(Optional.of(category));

        // Act & Assert
        assertThrows(ResourceOwnershipException.class, () -> categoryService.removeCategory(category.getId(), user));
    }

    @Test
    void verifyOwnershipOrSystem_SystemStandard_ThrowsResourceOwnershipException() {
        // Arrange
        Category systemCategory = CategoryTestFactory.createValidSystemCategory();

        // Act & Assert
        assertThrows(ResourceOwnershipException.class,
                () -> categoryService.verifyOwnershipOrSystem(systemCategory, user));
    }

    @Test
    void verifyOwnershipOrSystem_OtherOwner_ThrowsResourceOwnershipException() {
        // Arrange
        User otherOwner = UserTestFactory.createValidUser();
        category.setOwner(otherOwner);

        // Act & Assert
        assertThrows(ResourceOwnershipException.class, () -> categoryService.verifyOwnershipOrSystem(category, user));
    }

    @Test
    void verifyOwnershipOrSystem_CorrectOwner_Succeeds() {
        // Act & Assert - no exception expected
        categoryService.verifyOwnershipOrSystem(category, user);
    }
}

package com.omatheusmesmo.shoppmate.list.service;

import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.repository.ShoppingListRepository;
import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import com.omatheusmesmo.shoppmate.shared.testutils.ListTestFactory;
import com.omatheusmesmo.shoppmate.shared.testutils.UserTestFactory;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ShoppingListService shoppingListService;

    private ShoppingList testList;
    private User testUser;

    @BeforeEach
    void setUp() {
        testList = ListTestFactory.createValidShoppingList();
        testUser = testList.getOwner();
    }

    @Test
    void saveList_ValidList_ReturnsSavedList() {
        // Act
        ShoppingList result = shoppingListService.saveList(testList);

        // Assert
        assertNotNull(result);
        assertEquals(testList.getName(), result.getName());
        verify(auditService, times(1)).setAuditData(testList, true);
        verify(shoppingListRepository, times(1)).save(testList);
    }

    @Test
    void findListById_ExistingId_ReturnsList() {
        // Arrange
        when(shoppingListRepository.findByIdAndDeletedFalse(testList.getId())).thenReturn(Optional.of(testList));

        // Act
        ShoppingList result = shoppingListService.findListById(testList.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testList.getId(), result.getId());
        assertEquals(testList.getName(), result.getName());
        verify(shoppingListRepository, times(1)).findByIdAndDeletedFalse(testList.getId());
    }

    @Test
    void findListById_NonExistingId_ThrowsNoSuchElementException() {
        // Arrange
        Long nonExistingId = testList.getId() + 1000;
        when(shoppingListRepository.findByIdAndDeletedFalse(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> shoppingListService.findListById(nonExistingId));
        verify(shoppingListRepository, times(1)).findByIdAndDeletedFalse(nonExistingId);
    }

    @Test
    void removeList_ExistingId_DeletesList() {
        // Arrange
        when(shoppingListRepository.findByIdAndDeletedFalse(testList.getId())).thenReturn(Optional.of(testList));

        // Act & Assert
        assertDoesNotThrow(() -> shoppingListService.removeList(testList.getId(), testUser));

        // Assert
        verify(shoppingListRepository, times(1)).deleteById(testList.getId());
    }

    @Test
    void removeList_NonExistingId_ThrowsNoSuchElementException() {
        // Arrange
        Long nonExistingId = testList.getId() + 1000;
        when(shoppingListRepository.findByIdAndDeletedFalse(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> shoppingListService.removeList(nonExistingId, testUser));
        verify(shoppingListRepository, never()).deleteById(anyLong());
    }

    @Test
    void saveList_NameIsNull_ThrowsIllegalArgumentException() {
        // Arrange
        testList.setName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> shoppingListService.saveList(testList));
        verify(shoppingListRepository, never()).save(any());
    }

    @Test
    void editList_ExistingList_ReturnsUpdatedList() {
        // Arrange
        String updatedName = testList.getName() + " Updated";
        testList.setName(updatedName);
        when(shoppingListRepository.findByIdAndDeletedFalse(testList.getId())).thenReturn(Optional.of(testList));

        // Act
        ShoppingList result = shoppingListService.editList(testList, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(updatedName, result.getName());
        verify(auditService, times(1)).setAuditData(testList, false);
        verify(shoppingListRepository, times(1)).save(testList);
    }
}

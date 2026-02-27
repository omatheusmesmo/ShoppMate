package com.omatheusmesmo.shoppmate.list.service;

import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.repository.ShoppingListRepository;
import com.omatheusmesmo.shoppmate.shared.service.AuditService;
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
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("João");
        testUser.setEmail("joao@email.com");

        testList = new ShoppingList();
        testList.setId(1L);
        testList.setName("Supermercado");
        testList.setOwner(testUser);
    }

    @Test
    void testSaveListSuccess() {
        ShoppingList result = shoppingListService.saveList(testList);

        assertNotNull(result);
        assertEquals("Supermercado", result.getName());

        verify(auditService, times(1)).setAuditData(testList, true);
        verify(shoppingListRepository, times(1)).save(testList);
    }

    @Test
    void testFindListByIdSuccess() {
        when(shoppingListRepository.findById(1L)).thenReturn(Optional.of(testList));

        ShoppingList result = shoppingListService.findListById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Supermercado", result.getName());

        verify(shoppingListRepository, times(1)).findById(1L);
    }

    @Test
    void testFindListByIdThrowsException() {
        when(shoppingListRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
            NoSuchElementException.class,
            () -> shoppingListService.findListById(999L)
        );

        verify(shoppingListRepository, times(1)).findById(999L);
    }

    @Test
    void testRemoveListSuccess() {
        when(shoppingListRepository.findById(1L)).thenReturn(Optional.of(testList));

        assertDoesNotThrow(() -> shoppingListService.removeList(1L));

        verify(shoppingListRepository, times(1)).deleteById(1L);
    }

    @Test
    void testRemoveListThrowsExceptionWhenNotFound() {
        when(shoppingListRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
            NoSuchElementException.class,
            () -> shoppingListService.removeList(999L)
        );

        verify(shoppingListRepository, never()).deleteById(999L);
    }

    @Test
    void testSaveListThrowsExceptionWhenNameIsNull() {
        testList.setName(null);

        assertThrows(
            IllegalArgumentException.class,
            () -> shoppingListService.saveList(testList)
        );
    }

    @Test
    void testEditListSuccess() {
        testList.setName("Supermercado Atualizado");
        when(shoppingListRepository.findById(1L)).thenReturn(Optional.of(testList));

        ShoppingList result = shoppingListService.editList(testList);

        assertNotNull(result);
        assertEquals("Supermercado Atualizado", result.getName());
        verify(auditService, times(1)).setAuditData(testList, false);
        verify(shoppingListRepository, times(1)).save(testList);
    }
}

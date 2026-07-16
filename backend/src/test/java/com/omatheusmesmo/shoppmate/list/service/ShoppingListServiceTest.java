package com.omatheusmesmo.shoppmate.list.service;

import com.omatheusmesmo.shoppmate.list.entity.ListPermission;
import com.omatheusmesmo.shoppmate.list.entity.Permission;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.mapper.ListMapper;
import com.omatheusmesmo.shoppmate.list.repository.ListPermissionRepository;
import com.omatheusmesmo.shoppmate.list.repository.ShoppingListRepository;
import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import com.omatheusmesmo.shoppmate.shared.testutils.ListTestFactory;
import com.omatheusmesmo.shoppmate.shared.testutils.UserTestFactory;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.service.UserService;
import com.omatheusmesmo.shoppmate.utils.exception.ResourceOwnershipException;
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
    private ListPermissionRepository listPermissionRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private UserService userService;

    @Mock
    private ListMapper listMapper;

    @InjectMocks
    private ShoppingListService shoppingListService;

    private ShoppingList testList;
    private User testUser;
    private User sharedUser;
    private User unrelatedUser;
    private ListPermission readPermission;
    private ListPermission writePermission;

    @BeforeEach
    void setUp() {
        testList = ListTestFactory.createValidShoppingList();
        testUser = testList.getOwner();

        sharedUser = UserTestFactory.createValidUser();
        unrelatedUser = UserTestFactory.createValidUser();

        readPermission = new ListPermission();
        readPermission.setShoppingList(testList);
        readPermission.setUser(sharedUser);
        readPermission.setPermission(Permission.READ);

        writePermission = new ListPermission();
        writePermission.setShoppingList(testList);
        writePermission.setUser(sharedUser);
        writePermission.setPermission(Permission.WRITE);
    }

    @Test
    void saveList_ValidList_ReturnsSavedList() {
        ShoppingList result = shoppingListService.saveList(testList);

        assertNotNull(result);
        assertEquals(testList.getName(), result.getName());

        verify(auditService).setAuditData(testList, true);
        verify(shoppingListRepository).save(testList);
    }

    @Test
    void findListById_ExistingId_ReturnsList() {
        when(shoppingListRepository.findByIdAndDeletedFalse(testList.getId())).thenReturn(Optional.of(testList));

        ShoppingList result = shoppingListService.findListById(testList.getId());

        assertNotNull(result);
        assertEquals(testList.getId(), result.getId());
        assertEquals(testList.getName(), result.getName());

        verify(shoppingListRepository).findByIdAndDeletedFalse(testList.getId());
    }

    @Test
    void findListById_NonExistingId_ThrowsNoSuchElementException() {
        Long nonExistingId = testList.getId() + 1000;

        when(shoppingListRepository.findByIdAndDeletedFalse(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> shoppingListService.findListById(nonExistingId));

        verify(shoppingListRepository).findByIdAndDeletedFalse(nonExistingId);
    }

    @Test
    void removeList_ExistingId_DeletesList() {
        when(shoppingListRepository.findByIdAndDeletedFalse(testList.getId())).thenReturn(Optional.of(testList));

        assertDoesNotThrow(() -> shoppingListService.removeList(testList.getId(), testUser));

        verify(shoppingListRepository).deleteById(testList.getId());
    }

    @Test
    void removeList_NonExistingId_ThrowsNoSuchElementException() {
        Long nonExistingId = testList.getId() + 1000;

        when(shoppingListRepository.findByIdAndDeletedFalse(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> shoppingListService.removeList(nonExistingId, testUser));

        verify(shoppingListRepository, never()).deleteById(anyLong());
    }

    @Test
    void saveList_NameIsNull_ThrowsIllegalArgumentException() {
        testList.setName(null);

        assertThrows(IllegalArgumentException.class, () -> shoppingListService.saveList(testList));

        verify(shoppingListRepository, never()).save(any());
    }

    @Test
    void editList_ExistingList_ReturnsUpdatedList() {
        String updatedName = testList.getName() + " Updated";
        testList.setName(updatedName);

        when(shoppingListRepository.findByIdAndDeletedFalse(testList.getId())).thenReturn(Optional.of(testList));

        ShoppingList result = shoppingListService.editList(testList, testUser);

        assertNotNull(result);
        assertEquals(updatedName, result.getName());

        verify(auditService).setAuditData(testList, false);
        verify(shoppingListRepository).save(testList);
    }

    @Test
    void findAndVerifyReadAccess_Owner_ReturnsList() {
        when(shoppingListRepository.findByIdAndDeletedFalse(testList.getId())).thenReturn(Optional.of(testList));

        ShoppingList result = shoppingListService.findAndVerifyReadAccess(testList.getId(), testUser);

        assertSame(testList, result);

        verify(listPermissionRepository, never()).findByShoppingListIdAndUserIdAndDeletedFalse(anyLong(), anyLong());
    }

    @Test
    void findAndVerifyReadAccess_ReadPermission_ReturnsList() {
        when(shoppingListRepository.findByIdAndDeletedFalse(testList.getId())).thenReturn(Optional.of(testList));

        when(listPermissionRepository.findByShoppingListIdAndUserIdAndDeletedFalse(testList.getId(),
                sharedUser.getId())).thenReturn(Optional.of(readPermission));

        ShoppingList result = shoppingListService.findAndVerifyReadAccess(testList.getId(), sharedUser);

        assertSame(testList, result);
    }

    @Test
    void findAndVerifyReadAccess_WritePermission_ReturnsList() {
        when(shoppingListRepository.findByIdAndDeletedFalse(testList.getId())).thenReturn(Optional.of(testList));

        when(listPermissionRepository.findByShoppingListIdAndUserIdAndDeletedFalse(testList.getId(),
                sharedUser.getId())).thenReturn(Optional.of(writePermission));

        ShoppingList result = shoppingListService.findAndVerifyReadAccess(testList.getId(), sharedUser);

        assertSame(testList, result);
    }

    @Test
    void findAndVerifyReadAccess_UnrelatedUser_ThrowsResourceOwnershipException() {
        when(shoppingListRepository.findByIdAndDeletedFalse(testList.getId())).thenReturn(Optional.of(testList));

        when(listPermissionRepository.findByShoppingListIdAndUserIdAndDeletedFalse(testList.getId(),
                unrelatedUser.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceOwnershipException.class,
                () -> shoppingListService.findAndVerifyReadAccess(testList.getId(), unrelatedUser));
    }

    @Test
    void findAndVerifyReadAccess_DeletedPermission_ThrowsResourceOwnershipException() {
        when(shoppingListRepository.findByIdAndDeletedFalse(testList.getId())).thenReturn(Optional.of(testList));

        when(listPermissionRepository.findByShoppingListIdAndUserIdAndDeletedFalse(testList.getId(),
                sharedUser.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceOwnershipException.class,
                () -> shoppingListService.findAndVerifyReadAccess(testList.getId(), sharedUser));
    }

    @Test
    void findAndVerifyWriteAccess_Owner_ReturnsList() {
        when(shoppingListRepository.findByIdAndDeletedFalse(testList.getId())).thenReturn(Optional.of(testList));

        ShoppingList result = shoppingListService.findAndVerifyWriteAccess(testList.getId(), testUser);

        assertSame(testList, result);

        verify(listPermissionRepository, never()).findByShoppingListIdAndUserIdAndDeletedFalse(anyLong(), anyLong());
    }

    @Test
    void findAndVerifyWriteAccess_WritePermission_ReturnsList() {
        when(shoppingListRepository.findByIdAndDeletedFalse(testList.getId())).thenReturn(Optional.of(testList));

        when(listPermissionRepository.findByShoppingListIdAndUserIdAndDeletedFalse(testList.getId(),
                sharedUser.getId())).thenReturn(Optional.of(writePermission));

        ShoppingList result = shoppingListService.findAndVerifyWriteAccess(testList.getId(), sharedUser);

        assertSame(testList, result);
    }

    @Test
    void findAndVerifyWriteAccess_ReadPermission_ThrowsResourceOwnershipException() {
        when(shoppingListRepository.findByIdAndDeletedFalse(testList.getId())).thenReturn(Optional.of(testList));

        when(listPermissionRepository.findByShoppingListIdAndUserIdAndDeletedFalse(testList.getId(),
                sharedUser.getId())).thenReturn(Optional.of(readPermission));

        assertThrows(ResourceOwnershipException.class,
                () -> shoppingListService.findAndVerifyWriteAccess(testList.getId(), sharedUser));
    }

    @Test
    void findAndVerifyWriteAccess_UnrelatedUser_ThrowsResourceOwnershipException() {
        when(shoppingListRepository.findByIdAndDeletedFalse(testList.getId())).thenReturn(Optional.of(testList));

        when(listPermissionRepository.findByShoppingListIdAndUserIdAndDeletedFalse(testList.getId(),
                unrelatedUser.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceOwnershipException.class,
                () -> shoppingListService.findAndVerifyWriteAccess(testList.getId(), unrelatedUser));
    }

    @Test
    void findAndVerifyOwnership_Owner_ReturnsList() {
        when(shoppingListRepository.findByIdAndDeletedFalse(testList.getId())).thenReturn(Optional.of(testList));

        ShoppingList result = shoppingListService.findAndVerifyOwnership(testList.getId(), testUser);

        assertSame(testList, result);
    }

    @Test
    void findAndVerifyOwnership_NonOwnerUser_ThrowsResourceOwnershipException() {
        when(shoppingListRepository.findByIdAndDeletedFalse(testList.getId())).thenReturn(Optional.of(testList));

        assertThrows(ResourceOwnershipException.class,
                () -> shoppingListService.findAndVerifyOwnership(testList.getId(), sharedUser));

        verifyNoInteractions(listPermissionRepository);
    }
}

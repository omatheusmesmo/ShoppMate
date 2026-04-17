package com.omatheusmesmo.shoppmate.list.service;

import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.ListPermission;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.mapper.ListPermissionMapper;
import com.omatheusmesmo.shoppmate.list.repository.ListPermissionRepository;
import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import com.omatheusmesmo.shoppmate.shared.testutils.ListTestFactory;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.service.UserService;
import com.omatheusmesmo.shoppmate.utils.exception.ResourceOwnershipException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListPermissionServiceTest {

    @Mock
    private ListPermissionRepository listPermissionRepository;

    @Mock
    private ShoppingListService shoppingListService;

    @Mock
    private UserService userService;

    @Mock
    private AuditService auditService;

    @Mock
    private ListPermissionMapper listPermissionMapper;

    @InjectMocks
    private ListPermissionService listPermissionService;

    private ShoppingList shoppingList;
    private User owner;
    private User targetUser;
    private ListPermission listPermission;

    @BeforeEach
    void setUp() {
        shoppingList = ListTestFactory.createValidShoppingList();
        owner = shoppingList.getOwner();
        listPermission = ListTestFactory.createValidListPermission(shoppingList);
        targetUser = listPermission.getUser();
    }

    @Test
    void addListPermission_ValidDTO_ReturnsSavedPermission() {
        // Arrange
        ListPermissionRequestDTO requestDTO = ListTestFactory.createValidListPermissionRequestDTO(shoppingList.getId(),
                targetUser.getId());
        when(shoppingListService.findListById(shoppingList.getId())).thenReturn(shoppingList);
        when(userService.findUserById(targetUser.getId())).thenReturn(targetUser);
        when(listPermissionMapper.toEntity(requestDTO, shoppingList, targetUser)).thenReturn(listPermission);
        when(listPermissionRepository.save(any(ListPermission.class))).thenReturn(listPermission);

        // Act
        ListPermission result = listPermissionService.addListPermission(requestDTO, owner);

        // Assert
        assertNotNull(result);
        assertEquals(listPermission, result);
        verify(auditService, times(1)).setAuditData(listPermission, true);
        verify(listPermissionRepository, times(1)).save(listPermission);
    }

    @Test
    void addListPermission_RequesterNotOwner_ThrowsResourceOwnershipException() {
        // Arrange
        ListPermissionRequestDTO requestDTO = ListTestFactory.createValidListPermissionRequestDTO(shoppingList.getId(),
                targetUser.getId());
        User nonOwner = new User();
        nonOwner.setId(owner.getId() + 1000);
        when(shoppingListService.findListById(shoppingList.getId())).thenReturn(shoppingList);

        // Act & Assert
        ResourceOwnershipException exception = assertThrows(ResourceOwnershipException.class,
                () -> listPermissionService.addListPermission(requestDTO, nonOwner));
        assertTrue(exception.getMessage().contains("Only the list owner can grant permissions"));
        verify(listPermissionRepository, never()).save(any());
    }

    @Test
    void editList_ExistingId_ReturnsUpdatedPermission() {
        // Arrange
        ListPermissionUpdateRequestDTO updateDTO = ListTestFactory.createValidListPermissionUpdateRequestDTO();
        when(listPermissionRepository.findByIdAndDeletedFalse(listPermission.getId()))
                .thenReturn(Optional.of(listPermission));
        when(listPermissionRepository.save(any(ListPermission.class))).thenReturn(listPermission);

        // Act
        ListPermission result = listPermissionService.editList(listPermission.getId(), updateDTO, owner);

        // Assert
        assertNotNull(result);
        assertEquals(updateDTO.permission(), result.getPermission());
        verify(auditService, times(1)).setAuditData(listPermission, false);
        verify(listPermissionRepository, times(1)).save(listPermission);
    }

    @Test
    void editList_NonExistingId_ThrowsNoSuchElementException() {
        // Arrange
        Long nonExistingId = listPermission.getId() + 1000;
        ListPermissionUpdateRequestDTO updateDTO = ListTestFactory.createValidListPermissionUpdateRequestDTO();
        when(listPermissionRepository.findByIdAndDeletedFalse(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class,
                () -> listPermissionService.editList(nonExistingId, updateDTO, owner));
    }

    @Test
    void findListUserPermissionById_ExistingId_ReturnsPermission() {
        // Arrange
        when(listPermissionRepository.findByIdAndDeletedFalse(listPermission.getId()))
                .thenReturn(Optional.of(listPermission));

        // Act
        ListPermission result = listPermissionService.findListUserPermissionById(listPermission.getId(), owner);

        // Assert
        assertNotNull(result);
        assertEquals(listPermission, result);
        verify(listPermissionRepository, times(1)).findByIdAndDeletedFalse(listPermission.getId());
    }

    @Test
    void findListUserPermissionById_NonExistingId_ThrowsNoSuchElementException() {
        // Arrange
        Long nonExistingId = listPermission.getId() + 1000;
        when(listPermissionRepository.findByIdAndDeletedFalse(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class,
                () -> listPermissionService.findListUserPermissionById(nonExistingId, owner));
    }

    @Test
    void removeList_ExistingId_SoftDeletesPermission() {
        // Arrange
        when(listPermissionRepository.findByIdAndDeletedFalse(listPermission.getId()))
                .thenReturn(Optional.of(listPermission));

        // Act
        assertDoesNotThrow(() -> listPermissionService.removeList(listPermission.getId(), owner));

        // Assert
        verify(auditService, times(1)).softDelete(listPermission);
        verify(listPermissionRepository, times(1)).save(listPermission);
    }

    @Test
    void removeList_NonExistingId_ThrowsNoSuchElementException() {
        // Arrange
        Long nonExistingId = listPermission.getId() + 1000;
        when(listPermissionRepository.findByIdAndDeletedFalse(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> listPermissionService.removeList(nonExistingId, owner));
    }

    @Test
    void findAllPermissionsByListId_ExistingListId_ReturnsPermissions() {
        // Arrange
        when(shoppingListService.findAndVerifyAccess(shoppingList.getId(), owner)).thenReturn(shoppingList);
        when(listPermissionRepository.findByShoppingListIdAndDeletedFalse(shoppingList.getId()))
                .thenReturn(List.of(listPermission));

        // Act
        List<ListPermission> result = listPermissionService.findAllPermissionsByListId(shoppingList.getId(), owner);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(listPermission, result.get(0));
        verify(listPermissionRepository, times(1)).findByShoppingListIdAndDeletedFalse(shoppingList.getId());
    }
}

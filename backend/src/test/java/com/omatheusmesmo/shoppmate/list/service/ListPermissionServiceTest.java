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
    private User nonOwner;
    private ListPermission listPermission;

    @BeforeEach
    void setUp() {
        shoppingList = ListTestFactory.createValidShoppingList();
        owner = shoppingList.getOwner();

        listPermission = ListTestFactory.createValidListPermission(shoppingList);

        targetUser = listPermission.getUser();

        nonOwner = new User();
        nonOwner.setId(owner.getId() + 1000);
    }

    @Test
    void addListPermission_Owner_ReturnsSavedPermission() {
        ListPermissionRequestDTO requestDTO = ListTestFactory.createValidListPermissionRequestDTO(shoppingList.getId(),
                targetUser.getId());

        when(shoppingListService.findAndVerifyOwnership(shoppingList.getId(), owner)).thenReturn(shoppingList);

        when(userService.findUserById(targetUser.getId())).thenReturn(targetUser);

        when(listPermissionMapper.toEntity(requestDTO, shoppingList, targetUser)).thenReturn(listPermission);

        when(listPermissionRepository.save(listPermission)).thenReturn(listPermission);

        ListPermission result = listPermissionService.addListPermission(requestDTO, owner);

        assertNotNull(result);
        assertEquals(listPermission, result);

        verify(shoppingListService).findAndVerifyOwnership(shoppingList.getId(), owner);

        verify(userService).findUserById(targetUser.getId());

        verify(auditService).setAuditData(listPermission, true);

        verify(listPermissionRepository).save(listPermission);
    }

    @Test
    void addListPermission_NonOwner_ThrowsResourceOwnershipException() {
        ListPermissionRequestDTO requestDTO = ListTestFactory.createValidListPermissionRequestDTO(shoppingList.getId(),
                targetUser.getId());

        when(shoppingListService.findAndVerifyOwnership(shoppingList.getId(), nonOwner))
                .thenThrow(new ResourceOwnershipException("Only the list owner can manage permissions"));

        assertThrows(ResourceOwnershipException.class,
                () -> listPermissionService.addListPermission(requestDTO, nonOwner));

        verify(userService, never()).findUserById(anyLong());
        verify(listPermissionMapper, never()).toEntity(any(), any(), any());

        verify(listPermissionRepository, never()).save(any());
    }

    @Test
    void editList_Owner_ReturnsUpdatedPermission() {
        ListPermissionUpdateRequestDTO updateDTO = ListTestFactory.createValidListPermissionUpdateRequestDTO();

        when(listPermissionRepository.findByIdAndDeletedFalse(listPermission.getId()))
                .thenReturn(Optional.of(listPermission));

        when(shoppingListService.findAndVerifyOwnership(shoppingList.getId(), owner)).thenReturn(shoppingList);

        when(listPermissionRepository.save(listPermission)).thenReturn(listPermission);

        ListPermission result = listPermissionService.editList(listPermission.getId(), updateDTO, owner);

        assertNotNull(result);
        assertEquals(updateDTO.permission(), result.getPermission());

        verify(shoppingListService).findAndVerifyOwnership(shoppingList.getId(), owner);

        verify(auditService).setAuditData(listPermission, false);

        verify(listPermissionRepository).save(listPermission);
    }

    @Test
    void editList_NonOwner_ThrowsResourceOwnershipException() {
        ListPermissionUpdateRequestDTO updateDTO = ListTestFactory.createValidListPermissionUpdateRequestDTO();

        when(listPermissionRepository.findByIdAndDeletedFalse(listPermission.getId()))
                .thenReturn(Optional.of(listPermission));

        when(shoppingListService.findAndVerifyOwnership(shoppingList.getId(), nonOwner))
                .thenThrow(new ResourceOwnershipException("Only the list owner can manage permissions"));

        assertThrows(ResourceOwnershipException.class,
                () -> listPermissionService.editList(listPermission.getId(), updateDTO, nonOwner));

        verify(auditService, never()).setAuditData(any(), anyBoolean());

        verify(listPermissionRepository, never()).save(any());
    }

    @Test
    void editList_NonExistingId_ThrowsNoSuchElementException() {
        Long nonExistingId = listPermission.getId() + 1000;

        ListPermissionUpdateRequestDTO updateDTO = ListTestFactory.createValidListPermissionUpdateRequestDTO();

        when(listPermissionRepository.findByIdAndDeletedFalse(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> listPermissionService.editList(nonExistingId, updateDTO, owner));

        verifyNoInteractions(shoppingListService);
        verify(listPermissionRepository, never()).save(any());
    }

    @Test
    void findListUserPermissionById_Owner_ReturnsPermission() {
        when(listPermissionRepository.findByIdAndDeletedFalse(listPermission.getId()))
                .thenReturn(Optional.of(listPermission));

        when(shoppingListService.findAndVerifyOwnership(shoppingList.getId(), owner)).thenReturn(shoppingList);

        ListPermission result = listPermissionService.findListUserPermissionById(listPermission.getId(), owner);

        assertNotNull(result);
        assertEquals(listPermission, result);

        verify(listPermissionRepository).findByIdAndDeletedFalse(listPermission.getId());

        verify(shoppingListService).findAndVerifyOwnership(shoppingList.getId(), owner);
    }

    @Test
    void findListUserPermissionById_NonOwner_ThrowsResourceOwnershipException() {
        when(listPermissionRepository.findByIdAndDeletedFalse(listPermission.getId()))
                .thenReturn(Optional.of(listPermission));

        when(shoppingListService.findAndVerifyOwnership(shoppingList.getId(), nonOwner))
                .thenThrow(new ResourceOwnershipException("Only the list owner can manage permissions"));

        assertThrows(ResourceOwnershipException.class,
                () -> listPermissionService.findListUserPermissionById(listPermission.getId(), nonOwner));
    }

    @Test
    void findListUserPermissionById_NonExistingId_ThrowsNoSuchElementException() {
        Long nonExistingId = listPermission.getId() + 1000;

        when(listPermissionRepository.findByIdAndDeletedFalse(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> listPermissionService.findListUserPermissionById(nonExistingId, owner));

        verifyNoInteractions(shoppingListService);
    }

    @Test
    void removeList_Owner_SoftDeletesPermission() {
        when(listPermissionRepository.findByIdAndDeletedFalse(listPermission.getId()))
                .thenReturn(Optional.of(listPermission));

        when(shoppingListService.findAndVerifyOwnership(shoppingList.getId(), owner)).thenReturn(shoppingList);

        assertDoesNotThrow(() -> listPermissionService.removeList(listPermission.getId(), owner));

        verify(shoppingListService).findAndVerifyOwnership(shoppingList.getId(), owner);

        verify(auditService).softDelete(listPermission);
        verify(listPermissionRepository).save(listPermission);
    }

    @Test
    void removeList_NonOwner_ThrowsResourceOwnershipException() {
        when(listPermissionRepository.findByIdAndDeletedFalse(listPermission.getId()))
                .thenReturn(Optional.of(listPermission));

        when(shoppingListService.findAndVerifyOwnership(shoppingList.getId(), nonOwner))
                .thenThrow(new ResourceOwnershipException("Only the list owner can manage permissions"));

        assertThrows(ResourceOwnershipException.class,
                () -> listPermissionService.removeList(listPermission.getId(), nonOwner));

        verify(auditService, never()).softDelete(any());
        verify(listPermissionRepository, never()).save(any());
    }

    @Test
    void removeList_NonExistingId_ThrowsNoSuchElementException() {
        Long nonExistingId = listPermission.getId() + 1000;

        when(listPermissionRepository.findByIdAndDeletedFalse(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> listPermissionService.removeList(nonExistingId, owner));

        verifyNoInteractions(shoppingListService);
        verify(auditService, never()).softDelete(any());
        verify(listPermissionRepository, never()).save(any());
    }

    @Test
    void findAllPermissionsByListId_Owner_ReturnsPermissions() {
        when(shoppingListService.findAndVerifyOwnership(shoppingList.getId(), owner)).thenReturn(shoppingList);

        when(listPermissionRepository.findByShoppingListIdAndDeletedFalse(shoppingList.getId()))
                .thenReturn(List.of(listPermission));

        List<ListPermission> result = listPermissionService.findAllPermissionsByListId(shoppingList.getId(), owner);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(listPermission, result.get(0));

        verify(shoppingListService).findAndVerifyOwnership(shoppingList.getId(), owner);

        verify(listPermissionRepository).findByShoppingListIdAndDeletedFalse(shoppingList.getId());
    }

    @Test
    void findAllPermissionsByListId_NonOwner_ThrowsResourceOwnershipException() {
        when(shoppingListService.findAndVerifyOwnership(shoppingList.getId(), nonOwner))
                .thenThrow(new ResourceOwnershipException("Only the list owner can manage permissions"));

        assertThrows(ResourceOwnershipException.class,
                () -> listPermissionService.findAllPermissionsByListId(shoppingList.getId(), nonOwner));

        verify(listPermissionRepository, never()).findByShoppingListIdAndDeletedFalse(anyLong());
    }
}

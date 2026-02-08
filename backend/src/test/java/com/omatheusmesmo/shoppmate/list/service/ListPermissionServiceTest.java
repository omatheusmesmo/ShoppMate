package com.omatheusmesmo.shoppmate.list.service;

import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.Permission;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.entity.ListPermission;
import com.omatheusmesmo.shoppmate.list.mapper.ListPermissionMapper;
import com.omatheusmesmo.shoppmate.list.repository.ListPermissionRepository;
import com.omatheusmesmo.shoppmate.user.service.UserService;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListPermissionServiceTest {

    @Mock
    private ListPermissionRepository ListPermissionRepository;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addListPermission_ValidPermission_ReturnsSavedPermission() {
        ListPermissionRequestDTO requestDTO = createSamplePermissionRequest();
        ListPermission permission = createSamplePermission();

        User user = new User();
        user.setId(1L);
        ShoppingList shoppingList = new ShoppingList();
        shoppingList.setId(1L);

        when(userService.findUserById(requestDTO.idUser())).thenReturn(user);
        when(shoppingListService.findListById(requestDTO.idList())).thenReturn(shoppingList);
        when(listPermissionMapper.toEntity(requestDTO, shoppingList, user)).thenReturn(permission);
        when(ListPermissionRepository.save(any(ListPermission.class))).thenReturn(permission);

        ListPermission result = listPermissionService.addListPermission(requestDTO);

        assertNotNull(result);
        assertEquals(permission.getId(), result.getId());
        verify(userService, times(1)).isUserValid(any(User.class));
        verify(shoppingListService, times(1)).isListValid(any(ShoppingList.class));
        verify(auditService, times(1)).setAuditData(any(ListPermission.class), eq(true));
        verify(ListPermissionRepository, times(1)).save(any(ListPermission.class));
    }


    @Test
    void isListValid_ValidPermission_NoExceptionThrown() {
        ListPermission permission = createSamplePermission();

        assertDoesNotThrow(() -> listPermissionService.isListValid(permission));
        verify(userService, times(1)).isUserValid(permission.getUser());
        verify(shoppingListService, times(1)).isListValid(permission.getShoppingList());
    }

    @Test
    void findListItem_ExistingPermission_ReturnsPermission() {
        ListPermission permission = createSamplePermission();
        when(ListPermissionRepository.findById(permission.getId())).thenReturn(Optional.of(permission));

        Optional<ListPermission> result = listPermissionService.findListItem(permission);

        assertTrue(result.isPresent());
        assertEquals(permission, result.get());
        verify(ListPermissionRepository, times(1)).findById(permission.getId());
    }

    @Test
    void findListItem_NonExistingPermission_ThrowsNoSuchElementException() {
        ListPermission permission = createSamplePermission();
        when(ListPermissionRepository.findById(permission.getId())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> listPermissionService.findListItem(permission));
        verify(ListPermissionRepository, times(1)).findById(permission.getId());
    }

    @Test
    void findListUserPermissionById_ExistingId_ReturnsPermission() {
        Long id = 1L;
        ListPermission permission = createSamplePermission();
        permission.setId(id);
        when(ListPermissionRepository.findById(id)).thenReturn(Optional.of(permission));

        ListPermission result = listPermissionService.findListUserPermissionById(id);

        assertNotNull(result);
        assertEquals(permission, result);
        verify(ListPermissionRepository, times(1)).findById(id);
    }

    @Test
    void findListUserPermissionById_NonExistingId_ThrowsNoSuchElementException() {
        Long id = 1L;
        when(ListPermissionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> listPermissionService.findListUserPermissionById(id));
        verify(ListPermissionRepository, times(1)).findById(id);
    }

    @Test
    void removeList_ExistingId_DeletesPermission() {
        Long id = 1L;
        ListPermission permission = createSamplePermission();
        permission.setId(id);
        when(ListPermissionRepository.findById(id)).thenReturn(Optional.of(permission));

        listPermissionService.removeList(id);

        verify(ListPermissionRepository, times(1)).findById(id);
        verify(ListPermissionRepository, times(1)).save(permission);
    }

    @Test
    void removeList_NonExistingId_ThrowsNoSuchElementException() {
        Long id = 1L;
        when(ListPermissionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> listPermissionService.removeList(id));
        verify(ListPermissionRepository, times(1)).findById(id);
        verify(ListPermissionRepository, never()).deleteById(any());
    }

    @Test
    void editList_ExistingPermission_ReturnsEditedPermission() {
        Long id = 1L;
        ListPermission permission = createSamplePermission();
        permission.setId(id);
        ListPermissionUpdateRequestDTO updateDTO = new ListPermissionUpdateRequestDTO(Permission.WRITE);

        when(ListPermissionRepository.findById(id)).thenReturn(Optional.of(permission));
        when(ListPermissionRepository.save(permission)).thenReturn(permission);

        ListPermission result = listPermissionService.editList(id, updateDTO);

        assertNotNull(result);
        assertEquals(permission, result);
        assertEquals(Permission.WRITE, result.getPermission());
        verify(ListPermissionRepository, times(1)).findById(id);
        verify(userService, times(1)).isUserValid(permission.getUser());
        verify(shoppingListService, times(1)).isListValid(permission.getShoppingList());
        verify(auditService, times(1)).setAuditData(permission, false);
        verify(ListPermissionRepository, times(1)).save(permission);
    }


    @Test
    void editList_NonExistingPermission_ThrowsNoSuchElementException() {
        Long id = 1L;
        when(ListPermissionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> listPermissionService.editList(id, null));
        verify(ListPermissionRepository, times(1)).findById(id);
        verify(ListPermissionRepository, never()).save(any());
    }

    @Test
    void findAll_MultiplePermissions_ReturnsAllPermissions() {
        ListPermission permission1 = createSamplePermission();
        ListPermission permission2 = createSamplePermission();
        List<ListPermission> permissions = Arrays.asList(permission1, permission2);
        when(ListPermissionRepository.findByShoppingListIdAndDeletedFalse(1L)).thenReturn(permissions);

        List<ListPermission> result = listPermissionService.findAllPermissionsByListId(1L);

        assertEquals(2, result.size());
        assertTrue(result.contains(permission1));
        assertTrue(result.contains(permission2));
        verify(ListPermissionRepository, times(1)).findByShoppingListIdAndDeletedFalse(1L);
    }

    private ListPermission createSamplePermission() {
        ListPermission permission = new ListPermission();
        permission.setId(1L);
        User user = new User();
        user.setId(1L);
        permission.setUser(user);
        ShoppingList list = new ShoppingList();
        list.setId(1L);
        permission.setShoppingList(list);
        permission.setCreatedAt(LocalDateTime.now());
        permission.setUpdatedAt(LocalDateTime.now());
        return permission;
    }

    private ListPermissionRequestDTO createSamplePermissionRequest(){
        return new ListPermissionRequestDTO(1L, 1L, Permission.READ);
    }
}
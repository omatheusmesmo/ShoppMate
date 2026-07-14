package com.omatheusmesmo.shoppmate.list.service;

import com.omatheusmesmo.shoppmate.item.entity.Item;
import com.omatheusmesmo.shoppmate.item.service.ItemService;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.ListItem;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.mapper.ListItemMapper;
import com.omatheusmesmo.shoppmate.list.repository.ListItemRepository;
import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import com.omatheusmesmo.shoppmate.shared.testutils.ListTestFactory;
import com.omatheusmesmo.shoppmate.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListItemServiceTest {

    @Mock
    private ListItemRepository listItemRepository;

    @Mock
    private ShoppingListService shoppingListService;

    @Mock
    private ItemService itemService;

    @Mock
    private AuditService auditService;

    @Mock
    private ListItemMapper listItemMapper;

    @InjectMocks
    private ListItemService service;

    private ListItemRequestDTO listItemRequestDTO;
    private Item item;
    private ShoppingList shoppingList;
    private ListItem listItem;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        shoppingList = ListTestFactory.createValidShoppingList();
        user = shoppingList.getOwner();
        listItem = ListTestFactory.createValidListItem(shoppingList);
        item = listItem.getItem();

        listItemRequestDTO = ListTestFactory.createValidListItemRequestDTO(shoppingList.getId(), item.getId());
    }

    @Test
    void addShoppItemList_ValidDTO_ReturnsSavedListItem() {
        when(itemService.findById(item.getId())).thenReturn(item);

        when(shoppingListService.findAndVerifyWriteAccess(shoppingList.getId(), user)).thenReturn(shoppingList);

        when(listItemMapper.toEntity(listItemRequestDTO, item, shoppingList)).thenReturn(listItem);

        when(listItemRepository.save(listItem)).thenReturn(listItem);

        ListItem savedItem = service.addShoppItemList(listItemRequestDTO, user);

        assertNotNull(savedItem);
        assertEquals(listItem, savedItem);

        verify(itemService).findById(item.getId());

        verify(shoppingListService).findAndVerifyWriteAccess(shoppingList.getId(), user);

        verify(itemService).isItemValid(item);
        verify(shoppingListService).isListValid(shoppingList);
        verify(auditService).setAuditData(listItem, true);
        verify(listItemRepository).save(listItem);
    }

    @Test
    void isListItemValid_ValidListItem_NoExceptionThrown() {
        assertDoesNotThrow(() -> service.isListItemValid(listItem));

        verify(itemService).isItemValid(item);
        verify(shoppingListService).isListValid(shoppingList);
    }

    @Test
    void isListItemValid_NullQuantity_ThrowsIllegalArgumentException() {
        listItem.setQuantity(null);

        assertThrows(IllegalArgumentException.class, () -> service.isListItemValid(listItem));
    }

    @Test
    void findListItemByIdForRead_ExistingId_ReturnsListItem() {
        when(shoppingListService.findAndVerifyReadAccess(shoppingList.getId(), user)).thenReturn(shoppingList);

        when(listItemRepository.findByIdAndShoppListIdAndDeletedFalseFetchShoppList(listItem.getId(),
                shoppingList.getId())).thenReturn(Optional.of(listItem));

        ListItem result = service.findListItemByIdForRead(shoppingList.getId(), listItem.getId(), user);

        assertNotNull(result);
        assertEquals(listItem, result);

        verify(shoppingListService).findAndVerifyReadAccess(shoppingList.getId(), user);

        verify(listItemRepository).findByIdAndShoppListIdAndDeletedFalseFetchShoppList(listItem.getId(),
                shoppingList.getId());
    }

    @Test
    void findListItemByIdForWrite_ExistingId_ReturnsListItem() {
        when(shoppingListService.findAndVerifyWriteAccess(shoppingList.getId(), user)).thenReturn(shoppingList);

        when(listItemRepository.findByIdAndShoppListIdAndDeletedFalseFetchShoppList(listItem.getId(),
                shoppingList.getId())).thenReturn(Optional.of(listItem));

        ListItem result = service.findListItemByIdForWrite(shoppingList.getId(), listItem.getId(), user);

        assertNotNull(result);
        assertEquals(listItem, result);

        verify(shoppingListService).findAndVerifyWriteAccess(shoppingList.getId(), user);

        verify(listItemRepository).findByIdAndShoppListIdAndDeletedFalseFetchShoppList(listItem.getId(),
                shoppingList.getId());
    }

    @Test
    void findListItemByIdForWrite_NonExistingId_ThrowsNoSuchElementException() {
        when(shoppingListService.findAndVerifyWriteAccess(shoppingList.getId(), user)).thenReturn(shoppingList);

        when(listItemRepository.findByIdAndShoppListIdAndDeletedFalseFetchShoppList(999L, shoppingList.getId()))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.findListItemByIdForWrite(shoppingList.getId(), 999L, user));

        verify(listItemRepository, never()).save(any());
    }

    @Test
    void removeList_ExistingId_SoftDeletesListItem() {
        when(shoppingListService.findAndVerifyWriteAccess(shoppingList.getId(), user)).thenReturn(shoppingList);

        when(listItemRepository.findByIdAndShoppListIdAndDeletedFalseFetchShoppList(listItem.getId(),
                shoppingList.getId())).thenReturn(Optional.of(listItem));

        assertDoesNotThrow(() -> service.removeList(shoppingList.getId(), listItem.getId(), user));

        verify(shoppingListService).findAndVerifyWriteAccess(shoppingList.getId(), user);

        verify(auditService).softDelete(listItem);
        verify(listItemRepository).save(listItem);
    }

    @Test
    void removeList_NonExistingId_ThrowsNoSuchElementException() {
        when(shoppingListService.findAndVerifyWriteAccess(shoppingList.getId(), user)).thenReturn(shoppingList);

        when(listItemRepository.findByIdAndShoppListIdAndDeletedFalseFetchShoppList(999L, shoppingList.getId()))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.removeList(shoppingList.getId(), 999L, user));

        verify(auditService, never()).softDelete(any());
        verify(listItemRepository, never()).save(any());
    }

    @Test
    void editList_ValidUpdate_ReturnsUpdatedListItem() {
        ListItemUpdateRequestDTO updateDTO = ListTestFactory.createValidListItemUpdateRequestDTO(shoppingList.getId(),
                item.getId());

        when(shoppingListService.findAndVerifyWriteAccess(shoppingList.getId(), user)).thenReturn(shoppingList);

        when(listItemRepository.findByIdAndShoppListIdAndDeletedFalseFetchShoppList(listItem.getId(),
                shoppingList.getId())).thenReturn(Optional.of(listItem));

        ListItem result = service.editList(shoppingList.getId(), listItem.getId(), updateDTO, user);

        assertNotNull(result);
        assertEquals(updateDTO.quantity(), result.getQuantity());
        assertEquals(updateDTO.purchased(), result.getPurchased());
        assertEquals(updateDTO.unitPrice(), result.getUnitPrice());

        verify(shoppingListService).findAndVerifyWriteAccess(shoppingList.getId(), user);

        verify(auditService).setAuditData(listItem, false);
        verify(listItemRepository).save(listItem);
    }

    @Test
    void editList_NonExistingId_ThrowsNoSuchElementException() {
        ListItemUpdateRequestDTO updateDTO = ListTestFactory.createValidListItemUpdateRequestDTO(shoppingList.getId(),
                item.getId());

        when(shoppingListService.findAndVerifyWriteAccess(shoppingList.getId(), user)).thenReturn(shoppingList);

        when(listItemRepository.findByIdAndShoppListIdAndDeletedFalseFetchShoppList(999L, shoppingList.getId()))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.editList(shoppingList.getId(), 999L, updateDTO, user));
    }

    @Test
    void findAll_ExistingListId_ReturnsItems() {
        when(shoppingListService.findAndVerifyReadAccess(shoppingList.getId(), user)).thenReturn(shoppingList);

        when(listItemRepository.findByShoppListIdAndDeletedFalse(shoppingList.getId())).thenReturn(List.of(listItem));

        List<ListItem> result = service.findAll(shoppingList.getId(), user);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(listItem, result.get(0));

        verify(shoppingListService).findAndVerifyReadAccess(shoppingList.getId(), user);

        verify(listItemRepository).findByShoppListIdAndDeletedFalse(shoppingList.getId());
    }
}

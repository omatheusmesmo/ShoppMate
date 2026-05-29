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
        // Arrange
        when(itemService.findById(item.getId())).thenReturn(item);
        when(shoppingListService.findListById(shoppingList.getId())).thenReturn(shoppingList);
        doNothing().when(shoppingListService).verifyOwnership(shoppingList.getId(), user);
        when(listItemMapper.toEntity(listItemRequestDTO, item, shoppingList)).thenReturn(listItem);
        when(listItemRepository.save(listItem)).thenReturn(listItem);

        // Act
        ListItem savedItem = service.addShoppItemList(listItemRequestDTO, user);

        // Assert
        assertNotNull(savedItem);
        assertEquals(listItem, savedItem);
        verify(itemService, times(1)).isItemValid(item);
        verify(shoppingListService, times(1)).isListValid(shoppingList);
        verify(shoppingListService, times(1)).verifyOwnership(shoppingList.getId(), user);
        verify(auditService, times(1)).setAuditData(listItem, true);
        verify(listItemRepository, times(1)).save(listItem);
    }

    @Test
    void isListItemValid_ValidListItem_NoExceptionThrown() {
        // Act & Assert
        assertDoesNotThrow(() -> service.isListItemValid(listItem));
        verify(itemService, times(1)).isItemValid(item);
        verify(shoppingListService, times(1)).isListValid(shoppingList);
    }

    @Test
    void isListItemValid_NullQuantity_ThrowsIllegalArgumentException() {
        // Arrange
        listItem.setQuantity(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.isListItemValid(listItem));
    }

    @Test
    void findListItemById_ExistingId_ReturnsListItem() {
        // Arrange
        doNothing().when(shoppingListService).verifyOwnership(shoppingList.getId(), user);
        when(listItemRepository.findByIdAndShoppListIdAndDeletedFalseFetchShoppList(listItem.getId(),
                shoppingList.getId())).thenReturn(Optional.of(listItem));

        // Act
        ListItem result = service.findListItemById(shoppingList.getId(), listItem.getId(), user);

        // Assert
        assertNotNull(result);
        assertEquals(listItem, result);
        verify(shoppingListService, times(1)).verifyOwnership(shoppingList.getId(), user);
        verify(listItemRepository, times(1)).findByIdAndShoppListIdAndDeletedFalseFetchShoppList(listItem.getId(),
                shoppingList.getId());
    }

    @Test
    void findListItemById_NonExistingId_ThrowsNoSuchElementException() {
        // Arrange
        doNothing().when(shoppingListService).verifyOwnership(shoppingList.getId(), user);
        when(listItemRepository.findByIdAndShoppListIdAndDeletedFalseFetchShoppList(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> service.findListItemById(shoppingList.getId(), 999L, user));
        verify(listItemRepository, never()).save(any());
    }

    @Test
    void removeList_ExistingId_SoftDeletesListItem() {
        // Arrange
        doNothing().when(shoppingListService).verifyOwnership(shoppingList.getId(), user);
        when(listItemRepository.findByIdAndShoppListIdAndDeletedFalseFetchShoppList(listItem.getId(),
                shoppingList.getId())).thenReturn(Optional.of(listItem));

        // Act
        assertDoesNotThrow(() -> service.removeList(shoppingList.getId(), listItem.getId(), user));

        // Assert
        verify(auditService, times(1)).softDelete(listItem);
        verify(listItemRepository, times(1)).save(listItem);
    }

    @Test
    void removeList_NonExistingId_ThrowsNoSuchElementException() {
        // Arrange
        doNothing().when(shoppingListService).verifyOwnership(shoppingList.getId(), user);
        when(listItemRepository.findByIdAndShoppListIdAndDeletedFalseFetchShoppList(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> service.removeList(shoppingList.getId(), 999L, user));
        verify(auditService, never()).softDelete(any());
        verify(listItemRepository, never()).save(any());
    }

    @Test
    void editList_ValidUpdate_ReturnsUpdatedListItem() {
        // Arrange
        ListItemUpdateRequestDTO updateDTO = ListTestFactory.createValidListItemUpdateRequestDTO(shoppingList.getId(),
                item.getId());
        doNothing().when(shoppingListService).verifyOwnership(shoppingList.getId(), user);
        when(listItemRepository.findByIdAndShoppListIdAndDeletedFalseFetchShoppList(listItem.getId(),
                shoppingList.getId())).thenReturn(Optional.of(listItem));

        // Act
        ListItem result = service.editList(shoppingList.getId(), listItem.getId(), updateDTO, user);

        // Assert
        assertNotNull(result);
        assertEquals(updateDTO.quantity(), result.getQuantity());
        assertEquals(updateDTO.purchased(), result.getPurchased());
        assertEquals(updateDTO.unitPrice(), result.getUnitPrice());
        verify(auditService, times(1)).setAuditData(listItem, false);
        verify(listItemRepository, times(1)).save(listItem);
    }

    @Test
    void editList_NonExistingId_ThrowsNoSuchElementException() {
        // Arrange
        ListItemUpdateRequestDTO updateDTO = ListTestFactory.createValidListItemUpdateRequestDTO(shoppingList.getId(),
                item.getId());
        doNothing().when(shoppingListService).verifyOwnership(shoppingList.getId(), user);
        when(listItemRepository.findByIdAndShoppListIdAndDeletedFalseFetchShoppList(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> service.editList(shoppingList.getId(), 999L, updateDTO, user));
    }

    @Test
    void findAll_ExistingListId_ReturnsItems() {
        // Arrange
        doNothing().when(shoppingListService).verifyOwnership(shoppingList.getId(), user);
        when(listItemRepository.findByShoppListIdAndDeletedFalse(shoppingList.getId())).thenReturn(List.of(listItem));

        // Act
        List<ListItem> result = service.findAll(shoppingList.getId(), user);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(listItem, result.get(0));
        verify(shoppingListService, times(1)).verifyOwnership(shoppingList.getId(), user);
        verify(listItemRepository, times(1)).findByShoppListIdAndDeletedFalse(shoppingList.getId());
    }
}

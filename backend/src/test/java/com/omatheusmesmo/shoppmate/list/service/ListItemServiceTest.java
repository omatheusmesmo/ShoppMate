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
    private ListItemRepository ListItemRepository;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listItemRequestDTO = createSampleItem();

        item = new Item();
        item.setId(1L);

        shoppingList = new ShoppingList();
        shoppingList.setId(1L);

        listItem = new ListItem();
        listItem.setId(1L);
        listItem.setItem(item);
        listItem.setShoppList(shoppingList);
        listItem.setQuantity(2);
        listItem.setPurchased(false);
    }

    @Test
    void addShoppItemList() {
        when(itemService.findById(1L)).thenReturn(item);
        when(shoppingListService.findListById(1L)).thenReturn(shoppingList);
        when(listItemMapper.toEntity(listItemRequestDTO, item, shoppingList)).thenReturn(listItem);
        when(ListItemRepository.save(listItem)).thenReturn(listItem);

        ListItem savedItem = service.addShoppItemList(listItemRequestDTO);

        assertNotNull(savedItem);
        assertEquals(savedItem, listItem);
        verify(itemService, times(1)).isItemValid(listItem.getItem());
        verify(shoppingListService, times(1)).isListValid(listItem.getShoppList());
        verify(auditService, times(1)).setAuditData(listItem, true);
        verify(ListItemRepository, times(1)).save(listItem);
    }

    @Test
    void isListItemValid_NoException() {
        assertDoesNotThrow(() -> service.isListItemValid(listItem));

        verify(itemService, times(1)).isItemValid(listItem.getItem());
        verify(shoppingListService, times(1)).isListValid(listItem.getShoppList());
    }

    @Test
    void isListItemValid_InvalidItemQuantity() {
        listItem.setQuantity(null);

        assertThrows(IllegalArgumentException.class, () -> service.isListItemValid(listItem));

        verify(itemService, times(1)).isItemValid(listItem.getItem());
        verify(shoppingListService, times(1)).isListValid(listItem.getShoppList());
    }

    @Test
    void findListItem() {
        when(ListItemRepository.findById(listItem.getId())).thenReturn(Optional.of(listItem));

        ListItem result = service.findListItemById(listItem.getId());

        assertNotNull(result);

        verify(ListItemRepository, times(1)).findById(listItem.getId());
    }

    @Test
    void findListItemById() {
        when(ListItemRepository.findById(listItem.getId())).thenReturn(Optional.of(listItem));

        ListItem result = service.findListItemById(listItem.getId());

        assertNotNull(result);

        verify(ListItemRepository, times(1)).findById(listItem.getId());
    }

    @Test
    void findListItemById_WhenItemNotFound() {
        when(ListItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.findListItemById(999L));

        verify(ListItemRepository, never()).save(any());
    }

    @Test
    void removeList_Ok() {
        when(ListItemRepository.findById(listItem.getId())).thenReturn(Optional.of(listItem));

        assertDoesNotThrow(() -> service.removeList(listItem.getId()));

        verify(ListItemRepository, times(1)).save(listItem);
        verify(auditService, times(1)).softDelete(listItem);
    }

    @Test
    void removeList_ItemNotFound() {
        when(ListItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.removeList(999L));

        verify(ListItemRepository, never()).save(any());
        verify(auditService, never()).softDelete(any());
    }

    @Test
    void editList_Ok() {
        ListItemUpdateRequestDTO updateDTO = new ListItemUpdateRequestDTO(1L, 1L, 3, true);
        when(ListItemRepository.findById(1L)).thenReturn(Optional.of(listItem));

        ListItem result = service.editList(1L, updateDTO);

        assertNotNull(result);
        assertEquals(3, result.getQuantity());
        assertTrue(result.getPurchased());

        verify(auditService, times(1)).setAuditData(listItem, false);
        verify(ListItemRepository, times(1)).save(listItem);
    }

    @Test
    void editList_WhenListItemNotFound() {
        ListItemUpdateRequestDTO updateDTO = new ListItemUpdateRequestDTO(1L, 1L, 3, true);
        when(ListItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.editList(1L, updateDTO));

        verify(ListItemRepository, times(1)).findById(1L);
    }

    @Test
    void findAll() {
        when(ListItemRepository.findByShoppListId(1L)).thenReturn(List.of(listItem));

        List<ListItem> result = service.findAll(1L);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(ListItemRepository, times(1)).findByShoppListId(1L);
    }

    private ListItemRequestDTO createSampleItem() {
        return new ListItemRequestDTO(1L, 1L, 2);
    }
}
package com.omatheusmesmo.shoppmate.service;

import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.category.service.CategoryService;
import com.omatheusmesmo.shoppmate.item.entity.Item;
import com.omatheusmesmo.shoppmate.item.service.ItemService;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.item.repository.ItemRepository;
import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import com.omatheusmesmo.shoppmate.unit.service.UnitService;
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

class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private UnitService unitService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addItem_ValidItem_ReturnsSavedItem() {
        // Arrange
        Item item = createSampleItem();
        when(itemRepository.save(item)).thenReturn(item);

        // Act
        Item result = itemService.addItem(item);

        // Assert
        assertNotNull(result);
        assertEquals(item, result);
        verify(categoryService, times(1)).isCategoryValid(item.getCategory());
        verify(unitService, times(1)).isUnitValid(item.getUnit());
        verify(auditService, times(1)).setAuditData(item, true);
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void isItemValid_ValidItem_NoExceptionThrown() {
        // Arrange
        Item item = createSampleItem();

        // Act & Assert
        assertDoesNotThrow(() -> itemService.isItemValid(item));
        verify(categoryService, times(1)).isCategoryValid(item.getCategory());
        verify(unitService, times(1)).isUnitValid(item.getUnit());
    }

    @Test
    void findItem_ExistingItem_ReturnsItem() {
        // Arrange
        Item item = createSampleItem();
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        // Act
        Optional<Item> result = itemService.findItem(item);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(item, result.get());
        verify(itemRepository, times(1)).findById(item.getId());
    }

    @Test
    void findItem_NonExistingItem_ThrowsNoSuchElementException() {
        // Arrange
        Item item = createSampleItem();
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> itemService.findItem(item));
        verify(itemRepository, times(1)).findById(item.getId());
    }

    @Test
    void findItemById_ExistingId_ReturnsItem() {
        // Arrange
        Long id = 1L;
        Item item = createSampleItem();
        item.setId(id);
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        // Act
        Item result = itemService.findById(id);

        // Assert
        assertEquals(item, result);
        verify(itemRepository, times(1)).findById(id);
    }

    @Test
    void findItemById_NonExistingId_ThrowsNoSuchElementException() {
        // Arrange
        Long id = 1L;
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> itemService.findById(id));
        verify(itemRepository, times(1)).findById(id);
    }

    @Test
    void removeItem_ExistingId_DeletesItem() {
        // Arrange
        Long id = 1L;
        Item item = createSampleItem();
        item.setId(id);
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        // Act
        itemService.removeItem(id);

        // Assert
        verify(itemRepository, times(1)).findById(id);
        verify(itemRepository, times(1)).deleteById(id);
    }

    @Test
    void removeItem_NonExistingId_ThrowsNoSuchElementException() {
        // Arrange
        Long id = 1L;
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> itemService.removeItem(id));
        verify(itemRepository, times(1)).findById(id);
        verify(itemRepository, never()).deleteById(any());
    }

    @Test
    void editItem_ExistingItem_ReturnsEditedItem() {
        // Arrange
        Item item = createSampleItem();
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        // Act
        Item result = itemService.editItem(item);

        // Assert
        assertNotNull(result);
        assertEquals(item, result);
        verify(itemRepository, times(1)).findById(item.getId());
        verify(categoryService, times(1)).isCategoryValid(item.getCategory());
        verify(unitService, times(1)).isUnitValid(item.getUnit());
        verify(auditService, times(1)).setAuditData(item, false);
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void editItem_NonExistingItem_ThrowsNoSuchElementException() {
        // Arrange
        Item item = createSampleItem();
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> itemService.editItem(item));
        verify(itemRepository, times(1)).findById(item.getId());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void findAll_MultipleItems_ReturnsAllItems() {
        // Arrange
        Item item1 = createSampleItem();
        Item item2 = createSampleItem();
        List<Item> items = Arrays.asList(item1, item2);
        when(itemRepository.findAll()).thenReturn(items);

        // Act
        List<Item> result = itemService.findAll();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(item1));
        assertTrue(result.contains(item2));
        verify(itemRepository, times(1)).findAll();
    }

    private Item createSampleItem() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Sample Item");
        item.setCategory(new Category());
        item.setUnit(new Unit());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        return item;
    }
}
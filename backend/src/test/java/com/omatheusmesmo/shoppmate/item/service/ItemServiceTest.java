package com.omatheusmesmo.shoppmate.item.service;

import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.category.service.CategoryService;
import com.omatheusmesmo.shoppmate.item.dto.ItemRequestDTO;
import com.omatheusmesmo.shoppmate.item.entity.Item;
import com.omatheusmesmo.shoppmate.item.mapper.ItemMapper;
import com.omatheusmesmo.shoppmate.item.repository.ItemRepository;
import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import com.omatheusmesmo.shoppmate.shared.testutils.ItemTestFactory;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.service.UnitService;
import com.omatheusmesmo.shoppmate.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private UnitService unitService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemService itemService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        currentUser = new User();
        currentUser.setId(1L);
    }

    @Test
    void addItem_AccessibleCategoryAndUnit_ReturnsSavedItem() {
        // Arrange
        Item item = createSampleItem();

        Category category = item.getCategory();
        Unit unit = item.getUnit();

        ItemRequestDTO requestDTO = new ItemRequestDTO(item.getName(), category.getId(), unit.getId());

        when(categoryService.findAccessibleCategoryById(requestDTO.idCategory(), currentUser)).thenReturn(category);

        when(unitService.findAccessibleUnitById(requestDTO.idUnit(), currentUser)).thenReturn(unit);

        when(itemMapper.toEntity(requestDTO, category, unit)).thenReturn(item);

        when(itemRepository.save(item)).thenReturn(item);

        // Act
        Item result = itemService.addItem(requestDTO, currentUser);

        // Assert
        assertNotNull(result);
        assertEquals(item, result);

        verify(categoryService).findAccessibleCategoryById(requestDTO.idCategory(), currentUser);

        verify(unitService).findAccessibleUnitById(requestDTO.idUnit(), currentUser);

        verify(itemMapper).toEntity(requestDTO, category, unit);

        verify(categoryService).isCategoryValid(category);

        verify(unitService).isUnitValid(unit);

        verify(auditService).setAuditData(item, true);

        verify(itemRepository).save(item);
    }

    @Test
    void addItem_InaccessibleCategory_DoesNotSaveItem() {
        // Arrange
        Item item = createSampleItem();

        ItemRequestDTO requestDTO = new ItemRequestDTO(item.getName(), item.getCategory().getId(),
                item.getUnit().getId());

        when(categoryService.findAccessibleCategoryById(requestDTO.idCategory(), currentUser))
                .thenThrow(new NoSuchElementException("Category not found"));

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> itemService.addItem(requestDTO, currentUser));

        verify(unitService, never()).findAccessibleUnitById(any(), any());

        verify(itemMapper, never()).toEntity(any(), any(), any());

        verify(itemRepository, never()).save(any());
    }

    @Test
    void addItem_InaccessibleUnit_DoesNotSaveItem() {
        // Arrange
        Item item = createSampleItem();

        Category category = item.getCategory();

        ItemRequestDTO requestDTO = new ItemRequestDTO(item.getName(), category.getId(), item.getUnit().getId());

        when(categoryService.findAccessibleCategoryById(requestDTO.idCategory(), currentUser)).thenReturn(category);

        when(unitService.findAccessibleUnitById(requestDTO.idUnit(), currentUser))
                .thenThrow(new NoSuchElementException("Unit not found"));

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> itemService.addItem(requestDTO, currentUser));

        verify(itemMapper, never()).toEntity(any(), any(), any());

        verify(itemRepository, never()).save(any());
    }

    @Test
    void isItemValid_ValidItem_NoExceptionThrown() {
        // Arrange
        Item item = createSampleItem();

        // Act & Assert
        assertDoesNotThrow(() -> itemService.isItemValid(item));

        verify(categoryService).isCategoryValid(item.getCategory());

        verify(unitService).isUnitValid(item.getUnit());
    }

    @Test
    void findItem_ExistingItem_ReturnsItem() {
        // Arrange
        Item item = createSampleItem();

        when(itemRepository.findByIdAndDeletedFalse(item.getId())).thenReturn(Optional.of(item));

        // Act
        Optional<Item> result = itemService.findItem(item);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(item, result.get());

        verify(itemRepository).findByIdAndDeletedFalse(item.getId());
    }

    @Test
    void findItem_NonExistingItem_ThrowsNoSuchElementException() {
        // Arrange
        Item item = createSampleItem();

        when(itemRepository.findByIdAndDeletedFalse(item.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> itemService.findItem(item));

        verify(itemRepository).findByIdAndDeletedFalse(item.getId());
    }

    @Test
    void findItemById_ExistingId_ReturnsItem() {
        // Arrange
        Long id = 1L;

        Item item = createSampleItem();
        item.setId(id);

        when(itemRepository.findByIdWithRelations(id)).thenReturn(Optional.of(item));

        // Act
        Item result = itemService.findById(id);

        // Assert
        assertEquals(item, result);

        verify(itemRepository).findByIdWithRelations(id);
    }

    @Test
    void findItemById_NonExistingId_ThrowsNoSuchElementException() {
        // Arrange
        Long id = 1L;

        when(itemRepository.findByIdWithRelations(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> itemService.findById(id));

        verify(itemRepository).findByIdWithRelations(id);
    }

    @Test
    void removeItem_ExistingId_DeletesItem() {
        // Arrange
        Long id = 1L;

        Item item = createSampleItem();
        item.setId(id);

        when(itemRepository.findByIdWithRelations(id)).thenReturn(Optional.of(item));

        // Act
        itemService.removeItem(id);

        // Assert
        verify(itemRepository).findByIdWithRelations(id);

        verify(itemRepository).deleteById(id);
    }

    @Test
    void removeItem_NonExistingId_ThrowsNoSuchElementException() {
        // Arrange
        Long id = 1L;

        when(itemRepository.findByIdWithRelations(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> itemService.removeItem(id));

        verify(itemRepository).findByIdWithRelations(id);

        verify(itemRepository, never()).deleteById(any());
    }

    @Test
    void editItem_ExistingItemAndAccessibleReferences_ReturnsEditedItem() {
        // Arrange
        Long id = 1L;

        Item existingItem = createSampleItem();
        existingItem.setId(id);

        Category category = existingItem.getCategory();
        Unit unit = existingItem.getUnit();

        ItemRequestDTO requestDTO = new ItemRequestDTO("Updated item", category.getId(), unit.getId());

        when(itemRepository.findByIdWithRelations(id)).thenReturn(Optional.of(existingItem));

        when(categoryService.findAccessibleCategoryById(requestDTO.idCategory(), currentUser)).thenReturn(category);

        when(unitService.findAccessibleUnitById(requestDTO.idUnit(), currentUser)).thenReturn(unit);

        when(itemRepository.save(existingItem)).thenReturn(existingItem);

        // Act
        Item result = itemService.editItem(id, requestDTO, currentUser);

        // Assert
        assertNotNull(result);
        assertEquals(existingItem, result);
        assertEquals("Updated item", result.getName());
        assertEquals(category, result.getCategory());
        assertEquals(unit, result.getUnit());

        verify(itemRepository).findByIdWithRelations(id);

        verify(categoryService).findAccessibleCategoryById(requestDTO.idCategory(), currentUser);

        verify(unitService).findAccessibleUnitById(requestDTO.idUnit(), currentUser);

        verify(categoryService).isCategoryValid(category);

        verify(unitService).isUnitValid(unit);

        verify(auditService).setAuditData(existingItem, false);

        verify(itemRepository).save(existingItem);
    }

    @Test
    void editItem_NonExistingItem_ThrowsNoSuchElementException() {
        // Arrange
        Long id = 1L;

        Item item = createSampleItem();

        ItemRequestDTO requestDTO = new ItemRequestDTO(item.getName(), item.getCategory().getId(),
                item.getUnit().getId());

        when(itemRepository.findByIdWithRelations(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> itemService.editItem(id, requestDTO, currentUser));

        verify(itemRepository).findByIdWithRelations(id);

        verify(categoryService, never()).findAccessibleCategoryById(any(), any());

        verify(unitService, never()).findAccessibleUnitById(any(), any());

        verify(itemRepository, never()).save(any());
    }

    @Test
    void findAll_MultipleItems_ReturnsAllItems() {
        // Arrange
        Item item1 = createSampleItem();
        Item item2 = createSampleItem();

        List<Item> items = Arrays.asList(item1, item2);

        when(itemRepository.findAllByDeletedFalse()).thenReturn(items);

        // Act
        List<Item> result = itemService.findAll();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(item1));
        assertTrue(result.contains(item2));

        verify(itemRepository, times(1)).findAllByDeletedFalse();
    }

    private Item createSampleItem() {
        return ItemTestFactory.createValidItem();
    }
}

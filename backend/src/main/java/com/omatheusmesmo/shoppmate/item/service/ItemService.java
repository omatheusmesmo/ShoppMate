package com.omatheusmesmo.shoppmate.item.service;

import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.category.service.CategoryService;
import com.omatheusmesmo.shoppmate.item.dto.ItemRequestDTO;
import com.omatheusmesmo.shoppmate.item.entity.Item;
import com.omatheusmesmo.shoppmate.item.mapper.ItemMapper;
import com.omatheusmesmo.shoppmate.item.repository.ItemRepository;
import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.service.UnitService;
import com.omatheusmesmo.shoppmate.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    private final AuditService auditService;

    private final UnitService unitService;

    private final CategoryService categoryService;

    private final ItemMapper itemMapper;

    public ItemService(ItemRepository itemRepository, AuditService auditService, UnitService unitService,
            CategoryService categoryService, ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.auditService = auditService;
        this.unitService = unitService;
        this.categoryService = categoryService;
        this.itemMapper = itemMapper;
    }

    public Item addItem(ItemRequestDTO itemDTO, User currentUser) {

        Category category = categoryService.findAccessibleCategoryById(itemDTO.idCategory(), currentUser);

        Unit unit = unitService.findAccessibleUnitById(itemDTO.idUnit(), currentUser);

        Item item = itemMapper.toEntity(itemDTO, category, unit);

        isItemValid(item);
        auditService.setAuditData(item, true);
        itemRepository.save(item);

        return item;
    }

    public void isItemValid(Item item) {
        categoryService.isCategoryValid(item.getCategory());
        unitService.isUnitValid(item.getUnit());
        item.checkName();
    }

    public Optional<Item> findItem(Item item) {
        Optional<Item> foundItem = itemRepository.findByIdAndDeletedFalse(item.getId());

        if (foundItem.isPresent()) {
            return foundItem;
        }

        throw new NoSuchElementException("Item not found");
    }

    public Item findById(Long id) {
        return itemRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new NoSuchElementException("Item not found with id: " + id));
    }

    // TODO remove item by item || Use soft delete
    public void removeItem(Long id) {
        findById(id);
        itemRepository.deleteById(id);
    }

    public Item editItem(Long id, ItemRequestDTO itemDTO, User currentUser) {

        Item existingItem = findById(id);

        Category category = categoryService.findAccessibleCategoryById(itemDTO.idCategory(), currentUser);

        Unit unit = unitService.findAccessibleUnitById(itemDTO.idUnit(), currentUser);

        existingItem.setName(itemDTO.name());
        existingItem.setCategory(category);
        existingItem.setUnit(unit);

        isItemValid(existingItem);
        auditService.setAuditData(existingItem, false);
        itemRepository.save(existingItem);

        return existingItem;
    }

    public List<Item> findAll() {
        return itemRepository.findAllByDeletedFalse();
    }
}

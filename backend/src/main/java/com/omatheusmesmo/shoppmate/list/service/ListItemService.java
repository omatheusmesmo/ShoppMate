package com.omatheusmesmo.shoppmate.list.service;

import com.omatheusmesmo.shoppmate.item.entity.Item;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.ListItem;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.list.mapper.ListItemMapper;
import com.omatheusmesmo.shoppmate.list.repository.ListItemRepository;
import com.omatheusmesmo.shoppmate.item.service.ItemService;
import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import com.omatheusmesmo.shoppmate.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ListItemService {

    private final ListItemRepository listItemRepository;

    private final ShoppingListService shoppingListService;

    private final ItemService itemService;

    private final AuditService auditService;

    private final ListItemMapper listItemMapper;

    public ListItemService(ListItemRepository listItemRepository, ShoppingListService shoppingListService,
            ItemService itemService, AuditService auditService, ListItemMapper listItemMapper) {
        this.listItemRepository = listItemRepository;
        this.shoppingListService = shoppingListService;
        this.itemService = itemService;
        this.auditService = auditService;
        this.listItemMapper = listItemMapper;
    }

    public ListItem addShoppItemList(ListItemRequestDTO listItemRequestDTO, User user) {
        Item item = itemService.findById(listItemRequestDTO.itemId());
        ShoppingList shoppingList = shoppingListService.findListById(listItemRequestDTO.listId());
        shoppingListService.verifyOwnership(listItemRequestDTO.listId(), user);

        ListItem listItem = listItemMapper.toEntity(listItemRequestDTO, item, shoppingList);

        isListItemValid(listItem);
        auditService.setAuditData(listItem, true);
        listItemRepository.save(listItem);
        return listItem;
    }

    public void isListItemValid(ListItem listItem) throws NoSuchElementException {
        itemService.isItemValid(listItem.getItem());
        shoppingListService.isListValid(listItem.getShoppList());

        checkQuantity(listItem);
    }

    private void checkQuantity(ListItem listItem) {
        if (listItem.getQuantity() == null || listItem.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be informed and greater than 0!");
        }
    }

    public ListItem findListItemById(Long listId, Long id, User user) {
        // Verify access first to prevent existence leakage
        shoppingListService.verifyOwnership(listId, user);

        // Query constrained by shoppListId - fails if item doesn't belong to the authorized list
        return listItemRepository.findByIdAndShoppListIdAndDeletedFalseFetchShoppList(id, listId)
                .orElseThrow(() -> new NoSuchElementException("ListItem not found"));
    }

    public void removeList(Long listId, Long id, User user) {
        ListItem deletedItem = findListItemById(listId, id, user);
        auditService.softDelete(deletedItem);
        listItemRepository.save(deletedItem);
    }

    public ListItem editList(Long listId, Long id, ListItemUpdateRequestDTO listItemUpdateRequestDTO, User user) {
        ListItem existingListItem = findListItemById(listId, id, user);

        existingListItem.setQuantity(listItemUpdateRequestDTO.quantity());
        existingListItem.setPurchased(listItemUpdateRequestDTO.purchased());
        existingListItem.setUnitPrice(listItemUpdateRequestDTO.unitPrice());

        auditService.setAuditData(existingListItem, false);
        listItemRepository.save(existingListItem);
        return existingListItem;
    }

    public List<ListItem> findAll(Long idList, User user) {
        shoppingListService.verifyOwnership(idList, user);
        return listItemRepository.findByShoppListIdAndDeletedFalse(idList);
    }
}

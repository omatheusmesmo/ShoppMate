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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ListItemService {

    @Autowired
    private ListItemRepository ListItemRepository;

    @Autowired
    private ShoppingListService shoppingListService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private AuditService auditService;

    @Autowired
    private ListItemMapper listItemMapper;

    public ListItem addShoppItemList(ListItemRequestDTO listItemRequestDTO) {
        Item item = itemService.findById(listItemRequestDTO.itemId());
        ShoppingList shoppingList = shoppingListService.findListById(listItemRequestDTO.listId());

        ListItem listItem = listItemMapper.toEntity(listItemRequestDTO, item, shoppingList);

        isListItemValid(listItem);
        auditService.setAuditData(listItem, true);
        ListItemRepository.save(listItem);
        return listItem;
    }

    public void isListItemValid(ListItem ListItem) throws NoSuchElementException {
        itemService.isItemValid(ListItem.getItem());
        shoppingListService.isListValid(ListItem.getShoppList());

        checkQuantity(ListItem);
    }

    private void checkQuantity(ListItem ListItem){
        if (ListItem.getQuantity() == null || ListItem.getQuantity() <= 0){
            throw new IllegalArgumentException("Quantity must be informed and greater than 0!");
        }
    }

    public ListItem findListItemById(Long id) {
        return ListItemRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(()-> new NoSuchElementException("ListItem not found"));
    }

    public void removeList(Long id) {
        ListItem deletedItem = findListItemById(id);
        auditService.softDelete(deletedItem);
        ListItemRepository.save(deletedItem);
    }

    public ListItem editList(Long id, ListItemUpdateRequestDTO listItemUpdateRequestDTO) {
        ListItem existingListItem = findListItemById(id);

        existingListItem.setQuantity(listItemUpdateRequestDTO.quantity());
        existingListItem.setPurchased(listItemUpdateRequestDTO.purchased());

        auditService.setAuditData(existingListItem, false);
        ListItemRepository.save(existingListItem);
        return existingListItem;
    }

    public List<ListItem> findAll(Long idList) {
        return ListItemRepository.findByShoppListIdAndDeletedFalse(idList);
    }
}

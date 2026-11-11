package com.omatheusmesmo.shoppmate.list.mapper;

import com.omatheusmesmo.shoppmate.item.entity.Item;
import com.omatheusmesmo.shoppmate.item.mapper.ItemMapper;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemResponseDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemSummaryDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.ListItem;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ListItemMapper {

    @Autowired
    private ListMapper listMapper;

    @Autowired
    private ItemMapper itemMapper;

    public ListItem toEntity(ListItemRequestDTO dto, Item item, ShoppingList shoppingList) {
        ListItem listItem = new ListItem();
        listItem.setShoppList(shoppingList);
        listItem.setItem(item);
        listItem.setQuantity(dto.quantity());
        listItem.setUnitPrice(dto.unitPrice());
        return listItem;
    }

    public ListItemResponseDTO toResponseDTO(ListItem listItem) {
        BigDecimal unitPrice = listItem.getUnitPrice();
        BigDecimal totalPrice = calculateTotalPrice(unitPrice, listItem.getQuantity());

        return new ListItemResponseDTO(listMapper.toResponseDTO(listItem.getShoppList()),
                itemMapper.toResponseDTO(listItem.getItem()), listItem.getId(), listItem.getQuantity(),
            listItem.getPurchased(), unitPrice, totalPrice, listItem.getVersion());

    }

    private BigDecimal calculateTotalPrice(BigDecimal unitPrice, Integer quantity) {
        if (unitPrice == null || quantity == null) {
            return null;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public ListItemSummaryDTO toSummaryDTO(ListItem listItem) {
        return new ListItemSummaryDTO(listItem.getId(), listItem.getItem().getId(), listItem.getItem().getName(),
                listItem.getQuantity(), listItem.getPurchased());
    }

    public ListItem toDetachedEntityForUpdate(
        ListItemUpdateRequestDTO dto,
        Long listItemId,
        Item item,
        ShoppingList shoppingList,
        ListItem existingListItem
    ) {
        ListItem listItem = new ListItem();

        listItem.setId(listItemId);
        listItem.setShoppList(shoppingList);
        listItem.setItem(item);
        listItem.setQuantity(dto.quantity());
        listItem.setPurchased(dto.purchased());
        listItem.setUnitPrice(dto.unitPrice());
        listItem.setVersion(dto.version());

        listItem.setCreatedAt(existingListItem.getCreatedAt());
        listItem.setDeleted(existingListItem.getDeleted());

        return listItem;
    }
}

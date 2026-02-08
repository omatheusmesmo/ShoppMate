package com.omatheusmesmo.shoppmate.list.mapper;

import com.omatheusmesmo.shoppmate.item.entity.Item;
import com.omatheusmesmo.shoppmate.item.mapper.ItemMapper;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemResponseDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ListItemSummaryDTO;
import com.omatheusmesmo.shoppmate.list.entity.ListItem;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        return listItem;
    }

    public ListItemResponseDTO toResponseDTO(ListItem listItem) {
        return new ListItemResponseDTO(
                listMapper.toResponseDTO(listItem.getShoppList()),
                itemMapper.toResponseDTO(listItem.getItem()),
                listItem.getId(),
                listItem.getQuantity(),
                listItem.getPurchased()
        );
    }

    public ListItemSummaryDTO toSummaryDTO(ListItem listItem) {
        return new ListItemSummaryDTO(
            listItem.getId(),
            listItem.getItem().getId(),
            listItem.getItem().getName(),
            listItem.getQuantity(),
            listItem.getPurchased()
        );
    }
}
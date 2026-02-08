package com.omatheusmesmo.shoppmate.list.dtos;

import com.omatheusmesmo.shoppmate.item.dto.ItemResponseDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ListItemResponseDTO(
        ShoppingListResponseDTO shoppingList,
        ItemResponseDTO item,
        Long idListItem,
        Integer quantity,
        Boolean purchased
        ) {
}

package com.omatheusmesmo.shoppmate.list.dtos;

import com.omatheusmesmo.shoppmate.item.dto.ItemResponseDTO;

import java.math.BigDecimal;

public record ListItemResponseDTO(ShoppingListResponseDTO shoppingList, ItemResponseDTO item, Long idListItem,
        Integer quantity, Boolean purchased, BigDecimal unitPrice, BigDecimal totalPrice) {
}

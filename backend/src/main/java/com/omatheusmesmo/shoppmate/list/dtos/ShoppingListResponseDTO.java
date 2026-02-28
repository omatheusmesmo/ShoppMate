package com.omatheusmesmo.shoppmate.list.dtos;

import com.omatheusmesmo.shoppmate.user.dtos.UserResponseDTO;

import java.math.BigDecimal;

public record ShoppingListResponseDTO(Long idList, String listName, UserResponseDTO owner, BigDecimal totalValue) {
}

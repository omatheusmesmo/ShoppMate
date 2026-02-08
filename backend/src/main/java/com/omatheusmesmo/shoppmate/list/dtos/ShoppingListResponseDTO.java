package com.omatheusmesmo.shoppmate.list.dtos;

import com.omatheusmesmo.shoppmate.user.dtos.UserResponseDTO;

public record ShoppingListResponseDTO(
        Long idList,
        String listName,
        UserResponseDTO owner
) {
}

package com.omatheusmesmo.shoppmate.list.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ShoppingListRequestDTO(
        @NotBlank(message = "List name cannot be blank")
        String name,
        @NotNull(message = "User ID cannot be null")
        Long idUser
) {
}

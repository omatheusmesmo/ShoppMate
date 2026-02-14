package com.omatheusmesmo.shoppmate.list.dtos;

import jakarta.validation.constraints.NotBlank;

public record ShoppingListUpdateRequestDTO(@NotBlank(message = "List name cannot be blank") String name) {
}

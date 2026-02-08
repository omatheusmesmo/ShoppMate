package com.omatheusmesmo.shoppmate.list.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ListItemRequestDTO(
        @NotNull(message = "List ID cannot be null")
        Long listId,
        @NotNull(message = "Item ID cannot be null")
        Long itemId,
        @Min(1)
        Integer quantity
        ) {
}

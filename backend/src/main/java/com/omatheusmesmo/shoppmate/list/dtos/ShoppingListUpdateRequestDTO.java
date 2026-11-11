package com.omatheusmesmo.shoppmate.list.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ShoppingListUpdateRequestDTO(@NotNull(message = "List id cannot be null") Long id,
                                           @NotBlank(message = "List name cannot be blank") String name,
                                           @NotNull(message = "Version cannot be null") Long version) {
}

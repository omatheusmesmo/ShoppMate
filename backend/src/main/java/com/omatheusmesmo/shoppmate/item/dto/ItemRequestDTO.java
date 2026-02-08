package com.omatheusmesmo.shoppmate.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemRequestDTO(@NotBlank (message = "Item name cannot be blank")
                      String name,

                             @NotNull (message = "Category ID cannot be null")
                      Long idCategory,

                             @NotNull (message = "Unit ID cannot be null")
                      Long idUnit) {
}

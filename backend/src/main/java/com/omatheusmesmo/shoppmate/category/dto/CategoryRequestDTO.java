package com.omatheusmesmo.shoppmate.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequestDTO(
        @NotBlank(message = "Category name cannot be blank")
        String name
) {
}

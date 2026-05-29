package com.omatheusmesmo.shoppmate.unit.dto;

import jakarta.validation.constraints.NotBlank;

public record UnitRequestDTO(@NotBlank(message = "Unit name cannot be blank") String name,
        @NotBlank(message = "Unit symbol cannot be blank") String symbol) {
}

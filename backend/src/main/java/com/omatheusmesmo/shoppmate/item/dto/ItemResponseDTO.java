package com.omatheusmesmo.shoppmate.item.dto;

import com.omatheusmesmo.shoppmate.category.dto.CategoryResponseDTO;
import com.omatheusmesmo.shoppmate.unit.dto.UnitResponseDTO;

public record ItemResponseDTO(
        Long id,
        String name,

        CategoryResponseDTO category,
        UnitResponseDTO unit) {
}

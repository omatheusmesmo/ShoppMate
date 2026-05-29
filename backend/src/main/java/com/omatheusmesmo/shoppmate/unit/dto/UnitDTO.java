package com.omatheusmesmo.shoppmate.unit.dto;

import com.omatheusmesmo.shoppmate.user.dtos.UserResponseDTO;

public record UnitDTO(Long id, String name, String symbol, boolean isSystemStandard, UserResponseDTO owner) {
}

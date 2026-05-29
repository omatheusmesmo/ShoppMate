package com.omatheusmesmo.shoppmate.unit.dto;

import com.omatheusmesmo.shoppmate.user.dtos.UserResponseDTO;

public record UnitResponseDTO(Long id, String name, String symbol, boolean isSystemStandard, UserResponseDTO owner) {
}

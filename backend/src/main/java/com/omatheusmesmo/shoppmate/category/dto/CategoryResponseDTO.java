package com.omatheusmesmo.shoppmate.category.dto;

import com.omatheusmesmo.shoppmate.user.dtos.UserResponseDTO;

public record CategoryResponseDTO(Long id, String name, boolean isSystemStandard, UserResponseDTO owner) {
}

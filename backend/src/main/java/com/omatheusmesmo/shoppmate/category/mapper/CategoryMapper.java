package com.omatheusmesmo.shoppmate.category.mapper;

import com.omatheusmesmo.shoppmate.category.dto.CategoryRequestDTO;
import com.omatheusmesmo.shoppmate.category.dto.CategoryResponseDTO;
import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.user.dtos.UserResponseDTO;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.mapper.UserMapper;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    private final UserMapper userMapper;

    public CategoryMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Category toEntity(CategoryRequestDTO requestDTO, User owner) {
        Category category = new Category();
        category.setName(requestDTO.name());
        category.setSystemStandard(false);
        category.setOwner(owner);
        return category;
    }

    public Category toEntity(Long id, CategoryRequestDTO requestDTO) {
        Category category = new Category();
        category.setName(requestDTO.name());
        category.setId(id);
        return category;
    }

    public CategoryResponseDTO toResponseDTO(Category category) {
        UserResponseDTO ownerDTO = category.getOwner() != null ? userMapper.toResponseDTO(category.getOwner()) : null;
        return new CategoryResponseDTO(category.getId(), category.getName(), category.isSystemStandard(), ownerDTO);
    }
}

package com.omatheusmesmo.shoppmate.category.mapper;

import com.omatheusmesmo.shoppmate.category.dto.CategoryRequestDTO;
import com.omatheusmesmo.shoppmate.category.dto.CategoryResponseDTO;
import com.omatheusmesmo.shoppmate.category.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequestDTO requestDTO){
        Category category = new Category();
        category.setName(requestDTO.name());

        return category;
    }

    public Category toEntity(Long id, CategoryRequestDTO requestDTO){
        Category category = new Category();
        category.setName(requestDTO.name());
        category.setId(id);

        return category;
    }

    public CategoryResponseDTO toResponseDTO(Category category){
        return new CategoryResponseDTO(category.getId(), category.getName());
    }
}

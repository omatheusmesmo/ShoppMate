package com.omatheusmesmo.shoppmate.item.mapper;

import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.category.mapper.CategoryMapper;
import com.omatheusmesmo.shoppmate.item.dto.ItemRequestDTO;
import com.omatheusmesmo.shoppmate.item.dto.ItemResponseDTO;
import com.omatheusmesmo.shoppmate.item.entity.Item;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.mapper.UnitMapper;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {

    private final CategoryMapper categoryMapper;
    private final UnitMapper unitMapper;

    public ItemMapper(CategoryMapper categoryMapper, UnitMapper unitMapper) {
        this.categoryMapper = categoryMapper;
        this.unitMapper = unitMapper;
    }

    public Item toEntity(ItemRequestDTO dto, Category category, Unit unit) {

        Item item = new Item();
        item.setName(dto.name());
        item.setCategory(category);
        item.setUnit(unit);

        return item;
    }

    public ItemResponseDTO toResponseDTO(Item entity) {
        var categoryDto = categoryMapper.toResponseDTO(entity.getCategory());

        var unitDto = unitMapper.toResponseDTO(entity.getUnit());

        return new ItemResponseDTO(entity.getId(), entity.getName(), categoryDto, unitDto);
    }
}

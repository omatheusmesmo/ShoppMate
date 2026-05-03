package com.omatheusmesmo.shoppmate.item.mapper;

import com.omatheusmesmo.shoppmate.category.dto.CategoryResponseDTO;
import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.category.mapper.CategoryMapper;
import com.omatheusmesmo.shoppmate.category.repository.CategoryRepository;
import com.omatheusmesmo.shoppmate.item.dto.ItemRequestDTO;
import com.omatheusmesmo.shoppmate.item.dto.ItemResponseDTO;
import com.omatheusmesmo.shoppmate.item.entity.Item;
import com.omatheusmesmo.shoppmate.unit.dto.UnitResponseDTO;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.mapper.UnitMapper;
import com.omatheusmesmo.shoppmate.unit.repository.UnitRepository;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
public class ItemMapper {

    private final CategoryRepository categoryRepository;
    private final UnitRepository unitRepository;
    private final CategoryMapper categoryMapper;
    private final UnitMapper unitMapper;

    public ItemMapper(CategoryRepository categoryRepository, UnitRepository unitRepository,
            CategoryMapper categoryMapper, UnitMapper unitMapper) {
        this.categoryRepository = categoryRepository;
        this.unitRepository = unitRepository;
        this.categoryMapper = categoryMapper;
        this.unitMapper = unitMapper;
    }

    public Item toEntity(ItemRequestDTO dto) {
        Category category = categoryRepository.findByIdAndDeletedFalse(dto.idCategory())
                .orElseThrow(() -> new NoSuchElementException("Category not found with id: " + dto.idCategory()));

        Unit unit = unitRepository.findByIdAndDeletedFalse(dto.idUnit())
                .orElseThrow(() -> new NoSuchElementException("Unit not found with id: " + dto.idUnit()));

        var item = new Item();
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

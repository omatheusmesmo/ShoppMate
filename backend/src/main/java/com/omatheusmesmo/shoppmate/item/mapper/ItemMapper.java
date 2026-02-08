package com.omatheusmesmo.shoppmate.item.mapper;

import com.omatheusmesmo.shoppmate.category.dto.CategoryResponseDTO;
import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.category.repository.CategoryRepository;
import com.omatheusmesmo.shoppmate.item.dto.ItemRequestDTO;
import com.omatheusmesmo.shoppmate.item.dto.ItemResponseDTO;
import com.omatheusmesmo.shoppmate.item.entity.Item;
import com.omatheusmesmo.shoppmate.unit.dto.UnitResponseDTO;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.repository.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
public class ItemMapper {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UnitRepository unitRepository;


    public Item toEntity(ItemRequestDTO dto) {
        Category category = categoryRepository.findById(dto.idCategory())
                .orElseThrow(() -> new NoSuchElementException("Category not found with id: " + dto.idCategory()));

        Unit unit = unitRepository.findById(dto.idUnit())
                .orElseThrow(() -> new NoSuchElementException("Unit not found with id: " + dto.idUnit()));

        Item item = new Item();
        item.setName(dto.name());
        item.setCategory(category);
        item.setUnit(unit);
        return item;
    }


    public ItemResponseDTO toResponseDTO(Item entity) {

        CategoryResponseDTO categoryDto = new CategoryResponseDTO(entity.getCategory().getId(), entity.getCategory().getName());
        UnitResponseDTO unitDto = new UnitResponseDTO(entity.getUnit().getId(), entity.getUnit().getSymbol());

        return new ItemResponseDTO(
                entity.getId(),
                entity.getName(),
                categoryDto,
                unitDto
        );
    }

}

package com.omatheusmesmo.shoppmate.category.controller;

import com.omatheusmesmo.shoppmate.category.dto.CategoryRequestDTO;
import com.omatheusmesmo.shoppmate.category.dto.CategoryResponseDTO;
import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.category.mapper.CategoryMapper;
import com.omatheusmesmo.shoppmate.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CategoryMapper categoryMapper;

    @Operation(summary = "Return all categories")
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        List<Category> categories = categoryService.findAll();
        List<CategoryResponseDTO> responseDTOS = categories.stream()
                .map(categoryMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(responseDTOS);
    }

    @Operation(summary = "Add a new category")
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> addCategory(@RequestBody @Valid CategoryRequestDTO categoryDTO) {

        Category category = categoryMapper.toEntity(categoryDTO);
        Category savedCategory = categoryService.saveCategory(category);
        CategoryResponseDTO responseDTO = categoryMapper.toResponseDTO(savedCategory);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedCategory.getId())
                .toUri();

        return ResponseEntity.created(location).body(responseDTO);
    }

    @Operation(summary = "Delete a category by id")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        categoryService.removeCategory(id);
    }

    @Operation(summary = "Update a category")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Long id, @RequestBody @Valid CategoryRequestDTO categoryDTO) {
        Category category = categoryService.findCategoryById(id);
        category.setName(categoryDTO.name());
        Category updatedCategory = categoryService.saveCategory(category);
        CategoryResponseDTO responseDTO = categoryMapper.toResponseDTO(updatedCategory);

        return  ResponseEntity.ok(responseDTO);
    }
}

package com.omatheusmesmo.shoppmate.category.controller;

import com.omatheusmesmo.shoppmate.category.dto.CategoryRequestDTO;
import com.omatheusmesmo.shoppmate.category.dto.CategoryResponseDTO;
import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.category.mapper.CategoryMapper;
import com.omatheusmesmo.shoppmate.category.service.CategoryService;
import com.omatheusmesmo.shoppmate.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    public CategoryController(CategoryService categoryService, CategoryMapper categoryMapper) {
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
    }

    @Operation(summary = "Return all accessible categories")
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories(@AuthenticationPrincipal User user) {
        List<Category> categories = categoryService.findAllAccessibleByUser(user.getId());
        List<CategoryResponseDTO> responseDTOS = categories.stream().map(categoryMapper::toResponseDTO).toList();
        return ResponseEntity.ok(responseDTOS);
    }

    @Operation(summary = "Add a new category")
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> addCategory(@RequestBody @Valid CategoryRequestDTO categoryDTO,
            @AuthenticationPrincipal User user) {
        Category category = categoryMapper.toEntity(categoryDTO, user);
        Category savedCategory = categoryService.saveCategory(category);
        CategoryResponseDTO responseDTO = categoryMapper.toResponseDTO(savedCategory);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedCategory.getId()).toUri();
        return ResponseEntity.created(location).body(responseDTO);
    }

    @Operation(summary = "Delete a category by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id, @AuthenticationPrincipal User user) {
        categoryService.removeCategory(id, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update a category")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Long id,
            @RequestBody @Valid CategoryRequestDTO categoryDTO, @AuthenticationPrincipal User user) {
        Category category = categoryMapper.toEntity(categoryDTO, user);
        categoryService.editCategory(id, category, user);
        Category updatedCategory = categoryService.findCategoryById(id);
        CategoryResponseDTO responseDTO = categoryMapper.toResponseDTO(updatedCategory);
        return ResponseEntity.ok(responseDTO);
    }
}

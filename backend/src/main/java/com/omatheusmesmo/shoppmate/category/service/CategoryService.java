package com.omatheusmesmo.shoppmate.category.service;

import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.category.repository.CategoryRepository;
import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private AuditService auditService;

    public Category saveCategory(Category category){
        isCategoryValid(category);
        auditService.setAuditData(category,true);
        categoryRepository.save(category);
        return category;
    }

    public Category findCategoryById(Long id){
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Category not found"));
    }

    public Optional<Category> findCategoryByName(String name){
        return categoryRepository.findByName(name);
    }

    public void removeCategory(Long id){
        Category category = categoryRepository.findById(id).orElseThrow();
        auditService.softDelete(category);
        saveCategory(category);
    }

    public void isCategoryValid(Category category) {
        category.checkName();
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
}

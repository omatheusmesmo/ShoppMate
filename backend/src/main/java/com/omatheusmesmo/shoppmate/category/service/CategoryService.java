package com.omatheusmesmo.shoppmate.category.service;

import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.category.repository.CategoryRepository;
import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.utils.exception.ResourceOwnershipException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuditService auditService;

    public CategoryService(CategoryRepository categoryRepository, AuditService auditService) {
        this.categoryRepository = categoryRepository;
        this.auditService = auditService;
    }

    public Category saveCategory(Category category) {
        isCategoryValid(category);
        auditService.setAuditData(category, true);
        categoryRepository.save(category);
        return category;
    }

    public Category findCategoryById(Long id) {
        return categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NoSuchElementException("Item not found with id: " + id));
    }

    public Optional<Category> findCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    public void removeCategory(Long id, User currentUser) {
        Category category = findCategoryById(id);
        verifyOwnershipOrSystem(category, currentUser);
        auditService.softDelete(category);
        auditService.setAuditData(category, false);
        categoryRepository.save(category);
    }

    public void editCategory(Long id, Category category, User currentUser) {
        Category existingCategory = findCategoryById(id);
        verifyOwnershipOrSystem(existingCategory, currentUser);
        existingCategory.setName(category.getName());
        isCategoryValid(existingCategory);
        auditService.setAuditData(existingCategory, false);
        categoryRepository.save(existingCategory);
    }

    public void verifyOwnershipOrSystem(Category category, User currentUser) {
        if (category.isSystemStandard()) {
            throw new ResourceOwnershipException("System standard categories cannot be modified or deleted!");
        }
        if (category.getOwner() == null) {
            throw new ResourceOwnershipException("Non-system categories must have an owner; operation not permitted");
        }
        if (!category.getOwner().getId().equals(currentUser.getId())) {
            throw new ResourceOwnershipException("You can only modify or delete categories you own!");
        }
    }

    public void isCategoryValid(Category category) {
        category.checkName();
    }

    public List<Category> findAllAccessibleByUser(Long userId) {
        return categoryRepository.findAllAccessibleByUserId(userId);
    }
}

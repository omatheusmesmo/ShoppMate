package com.omatheusmesmo.shoppmate.service;

import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuditServiceTest {

    private final AuditService auditService = new AuditService();

    @Test
    void setAuditDataWhenNew() {
        Category category = new Category();

        auditService.setAuditData(category, true);

        assertNotNull(category.getCreatedAt());
        assertFalse(category.getDeleted());
    }

    @Test
    void setAuditDataWhenExisting() {
        Category category = new Category();

        auditService.setAuditData(category, false);

        assertNotNull(category.getUpdatedAt());
    }

    @Test
    void softDelete() {
        Category category = new Category();
        category.setDeleted(false);

        auditService.softDelete(category);

        assertTrue(category.getDeleted());
        assertNotNull(category.getUpdatedAt());
    }
}

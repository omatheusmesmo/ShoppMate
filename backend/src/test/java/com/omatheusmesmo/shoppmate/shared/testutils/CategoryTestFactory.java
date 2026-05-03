package com.omatheusmesmo.shoppmate.shared.testutils;

import com.omatheusmesmo.shoppmate.category.dto.CategoryRequestDTO;
import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.user.entity.User;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class CategoryTestFactory {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    public static Category createValidCategory() {
        var category = buildBaseCategory();
        category.setSystemStandard(false);
        category.setOwner(UserTestFactory.createValidUser());
        return category;
    }

    public static Category createValidSystemCategory() {
        var category = buildBaseCategory();
        category.setSystemStandard(true);
        category.setOwner(null);
        return category;
    }

    private static Category buildBaseCategory() {
        var category = new Category();
        category.setId(ID_GENERATOR.getAndIncrement());
        category.setName(FakerUtil.getFaker().commerce().department());
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category.setDeleted(false);
        return category;
    }

    public static CategoryRequestDTO createValidCategoryRequestDTO() {
        return new CategoryRequestDTO(FakerUtil.getFaker().commerce().department());
    }
}

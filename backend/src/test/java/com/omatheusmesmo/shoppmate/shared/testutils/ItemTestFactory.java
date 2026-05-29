package com.omatheusmesmo.shoppmate.shared.testutils;

import com.omatheusmesmo.shoppmate.item.dto.ItemRequestDTO;
import com.omatheusmesmo.shoppmate.item.entity.Item;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class ItemTestFactory {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    public static Item createValidItem() {
        Item item = new Item();
        item.setId(ID_GENERATOR.getAndIncrement());
        item.setName(FakerUtil.getFaker().commerce().productName());
        item.setCategory(CategoryTestFactory.createValidCategory());
        item.setUnit(UnitTestFactory.createValidUnit());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        item.setDeleted(false);
        return item;
    }

    public static ItemRequestDTO createValidItemRequestDTO(Long categoryId, Long unitId) {
        return new ItemRequestDTO(FakerUtil.getFaker().commerce().productName(), categoryId, unitId);
    }
}

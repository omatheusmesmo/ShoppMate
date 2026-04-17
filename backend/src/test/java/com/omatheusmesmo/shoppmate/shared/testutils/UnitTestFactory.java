package com.omatheusmesmo.shoppmate.shared.testutils;

import com.omatheusmesmo.shoppmate.unit.entity.Unit;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class UnitTestFactory {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    public static Unit createValidUnit() {
        Unit unit = new Unit();
        unit.setId(ID_GENERATOR.getAndIncrement());
        unit.setName(FakerUtil.getFaker().science().unit());
        unit.setSymbol(FakerUtil.getFaker().science().unit());
        unit.setCreatedAt(LocalDateTime.now());
        unit.setUpdatedAt(LocalDateTime.now());
        unit.setDeleted(false);
        return unit;
    }
}

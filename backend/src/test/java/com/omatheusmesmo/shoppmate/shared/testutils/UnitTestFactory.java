package com.omatheusmesmo.shoppmate.shared.testutils;

import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.user.entity.User;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class UnitTestFactory {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    private static Unit buildBaseUnit() {
        var unit = new Unit();
        unit.setId(ID_GENERATOR.getAndIncrement());
        unit.setName(FakerUtil.getFaker().science().unit());
        unit.setSymbol(FakerUtil.getFaker().science().unit());
        unit.setCreatedAt(LocalDateTime.now());
        unit.setUpdatedAt(LocalDateTime.now());
        unit.setDeleted(false);
        return unit;
    }

    public static Unit createValidUnit() {
        var unit = buildBaseUnit();
        unit.setSystemStandard(false);
        unit.setOwner(UserTestFactory.createValidUser());
        return unit;
    }

    public static Unit createValidSystemUnit() {
        var unit = buildBaseUnit();
        unit.setSystemStandard(true);
        unit.setOwner(null);
        return unit;
    }
}

package com.omatheusmesmo.shoppmate.service;

import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.repository.UnitRepository;
import com.omatheusmesmo.shoppmate.unit.service.UnitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UnitService unitService;

    private Unit unit;

    @BeforeEach
    void setUp() {
        unit = new Unit();
        unit.setId(1L);
        unit.setName("Kilogram");
        unit.setSymbol("kg");
    }

    @Test
    void saveUnit() {
        when(unitRepository.save(any(Unit.class))).thenReturn(unit);

        Unit result = unitService.saveUnit(unit);

        assertNotNull(result);
        verify(auditService, times(1)).setAuditData(unit, true);
        verify(unitRepository, times(1)).save(unit);
    }

    @Test
    void findAll() {
        when(unitRepository.findAll()).thenReturn(List.of(unit));

        List<Unit> result = unitService.findAll();

        assertEquals(1, result.size());
        verify(unitRepository, times(1)).findAll();
    }

    @Test
    void findUnitById() {
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));

        Optional<Unit> result = unitService.findUnitById(1L);

        assertTrue(result.isPresent());
        assertEquals("kg", result.get().getSymbol());
    }

    @Test
    void isUnitValid_InvalidSymbol() {
        unit.setSymbol(" ");

        assertThrows(IllegalArgumentException.class, () -> unitService.isUnitValid(unit));
    }
}

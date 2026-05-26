package com.omatheusmesmo.shoppmate.service;

import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.repository.UnitRepository;
import com.omatheusmesmo.shoppmate.unit.service.UnitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UnitServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UnitService unitService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void editUnit_WhenUnitExists_ShouldUpdateUnit() {
        Unit unit = createValidUnit();
        unit.setId(1L);

        when(unitRepository.existsById(1L)).thenReturn(true);
        when(unitRepository.save(unit)).thenReturn(unit);

        assertDoesNotThrow(() -> unitService.editUnit(unit));

        verify(unitRepository, times(1)).existsById(1L);
        verify(auditService, times(1)).setAuditData(unit, false);
        verify(unitRepository, times(1)).save(unit);
    }

    @Test
    void editUnit_WhenUnitDoesNotExist_ShouldThrowNoSuchElementException() {
        Unit unit = createValidUnit();
        unit.setId(999L);

        when(unitRepository.existsById(999L)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> unitService.editUnit(unit));

        verify(unitRepository, times(1)).existsById(999L);
        verify(auditService, never()).setAuditData(unit, false);
        verify(unitRepository, never()).save(unit);
    }

    @Test
    void editUnit_WhenNameIsBlank_ShouldThrowIllegalArgumentException() {
        Unit unit = createValidUnit();
        unit.setId(1L);
        unit.setName(" ");

        assertThrows(IllegalArgumentException.class, () -> unitService.editUnit(unit));

        verify(unitRepository, never()).existsById(anyLong());
        verify(auditService, never()).setAuditData(any(Unit.class), anyBoolean());
        verify(unitRepository, never()).save(any(Unit.class));
    }

    @Test
    void editUnit_WhenSymbolIsBlank_ShouldThrowIllegalArgumentException() {
        Unit unit = createValidUnit();
        unit.setId(1L);
        unit.setSymbol(" ");

        assertThrows(IllegalArgumentException.class, () -> unitService.editUnit(unit));

        verify(unitRepository, never()).existsById(anyLong());
        verify(auditService, never()).setAuditData(any(Unit.class), anyBoolean());
        verify(unitRepository, never()).save(any(Unit.class));
    }

    private Unit createValidUnit() {
        Unit unit = new Unit();
        unit.setName("Original Unit");
        unit.setSymbol("orig");
        return unit;
    }
}
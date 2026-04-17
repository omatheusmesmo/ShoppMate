package com.omatheusmesmo.shoppmate.unit.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import com.omatheusmesmo.shoppmate.shared.testutils.UnitTestFactory;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.repository.UnitRepository;
import com.omatheusmesmo.shoppmate.unit.service.UnitService;

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
        unit = UnitTestFactory.createValidUnit();
    }

    @Test
    void saveUnit_ValidUnit_ReturnsSavedUnit() {
        // Arrange
        when(unitRepository.save(any(Unit.class))).thenReturn(unit);

        // Act
        Unit result = unitService.saveUnit(unit);

        // Assert
        assertNotNull(result);
        verify(auditService, times(1)).setAuditData(unit, true);
        verify(unitRepository, times(1)).save(unit);
    }

    @Test
    void findAll_ExistingUnits_ReturnsUnitList() {
        // Arrange
        when(unitRepository.findAll()).thenReturn(List.of(unit));

        // Act
        List<Unit> result = unitService.findAll();

        // Assert
        assertEquals(1, result.size());
        verify(unitRepository, times(1)).findAll();
    }

    @Test
    void findUnitById_ExistingId_ReturnsUnit() {
        // Arrange
        when(unitRepository.findById(unit.getId())).thenReturn(Optional.of(unit));

        // Act
        Optional<Unit> result = unitService.findUnitById(unit.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(unit.getSymbol(), result.get().getSymbol());
    }

    @Test
    void isUnitValid_BlankSymbol_ThrowsIllegalArgumentException() {
        // Arrange
        unit.setSymbol(" ");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> unitService.isUnitValid(unit));
    }
}

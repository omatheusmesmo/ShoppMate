package com.omatheusmesmo.shoppmate.unit.service;

import java.util.List;
import java.util.NoSuchElementException;
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
import com.omatheusmesmo.shoppmate.shared.testutils.UserTestFactory;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.repository.UnitRepository;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.utils.exception.ResourceOwnershipException;

@ExtendWith(MockitoExtension.class)
class UnitServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UnitService unitService;

    private Unit unit;
    private User user;

    @BeforeEach
    void setUp() {
        user = UserTestFactory.createValidUser();
        unit = UnitTestFactory.createValidUnit();
        unit.setOwner(user);
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
    void findAllAccessibleByUser_ReturnsAccessibleUnits() {
        // Arrange
        when(unitRepository.findAllAccessibleByUserId(user.getId())).thenReturn(List.of(unit));

        // Act
        List<Unit> result = unitService.findAllAccessibleByUser(user.getId());

        // Assert
        assertEquals(1, result.size());
        verify(unitRepository, times(1)).findAllAccessibleByUserId(user.getId());
    }

    @Test
    void findUnitById_ExistingId_ReturnsUnit() {
        // Arrange
        when(unitRepository.findByIdAndDeletedFalse(unit.getId())).thenReturn(Optional.of(unit));

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

    @Test
    void removeUnitById_UserOwner_Succeeds() {
        // Arrange
        when(unitRepository.findByIdAndDeletedFalse(unit.getId())).thenReturn(Optional.of(unit));
        when(unitRepository.save(any(Unit.class))).thenReturn(unit);

        // Act
        unitService.removeUnitById(unit.getId(), user);

        // Assert
        verify(auditService, times(1)).softDelete(unit);
        verify(auditService, times(1)).setAuditData(unit, false);
        verify(unitRepository, times(1)).save(unit);
    }

    @Test
    void removeUnitById_NotFound_ThrowsNoSuchElementException() {
        // Arrange
        when(unitRepository.findByIdAndDeletedFalse(unit.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> unitService.removeUnitById(unit.getId(), user));
    }

    @Test
    void removeUnitById_SystemStandard_ThrowsResourceOwnershipException() {
        // Arrange
        Unit systemUnit = UnitTestFactory.createValidSystemUnit();
        when(unitRepository.findByIdAndDeletedFalse(systemUnit.getId())).thenReturn(Optional.of(systemUnit));

        // Act & Assert
        assertThrows(ResourceOwnershipException.class, () -> unitService.removeUnitById(systemUnit.getId(), user));
        verify(auditService, times(0)).softDelete(any(Unit.class));
        verify(auditService, times(0)).setAuditData(any(Unit.class), any(Boolean.TYPE));
        verify(unitRepository, times(0)).save(any(Unit.class));
    }

    @Test
    void removeUnitById_OtherUserOwner_ThrowsResourceOwnershipException() {
        // Arrange
        User otherOwner = UserTestFactory.createValidUser();
        unit.setOwner(otherOwner);
        when(unitRepository.findByIdAndDeletedFalse(unit.getId())).thenReturn(Optional.of(unit));

        // Act & Assert
        assertThrows(ResourceOwnershipException.class, () -> unitService.removeUnitById(unit.getId(), user));
        verify(auditService, times(0)).softDelete(any(Unit.class));
        verify(auditService, times(0)).setAuditData(any(Unit.class), any(Boolean.TYPE));
        verify(unitRepository, times(0)).save(any(Unit.class));
    }

    @Test
    void verifyOwnershipOrSystem_CorrectOwner_Succeeds() {
        // Act & Assert - no exception expected
        unitService.verifyOwnershipOrSystem(unit, user);
    }

    @Test
    void verifyOwnershipOrSystem_SystemStandard_ThrowsResourceOwnershipException() {
        // Arrange
        Unit systemUnit = UnitTestFactory.createValidSystemUnit();

        // Act & Assert
        assertThrows(ResourceOwnershipException.class, () -> unitService.verifyOwnershipOrSystem(systemUnit, user));
    }

    @Test
    void verifyOwnershipOrSystem_OtherOwner_ThrowsResourceOwnershipException() {
        // Arrange
        User otherOwner = UserTestFactory.createValidUser();
        unit.setOwner(otherOwner);

        // Act & Assert
        assertThrows(ResourceOwnershipException.class, () -> unitService.verifyOwnershipOrSystem(unit, user));
    }

    @Test
    void verifyOwnershipOrSystem_NullOwner_ThrowsResourceOwnershipException() {
        // Arrange
        unit.setOwner(null);

        // Act & Assert
        assertThrows(ResourceOwnershipException.class, () -> unitService.verifyOwnershipOrSystem(unit, user));
    }
}

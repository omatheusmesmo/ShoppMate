package com.omatheusmesmo.shoppmate.unit.service;

import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.repository.UnitRepository;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.utils.exception.ResourceOwnershipException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UnitService {

    private final UnitRepository unitRepository;
    private final AuditService auditService;

    public UnitService(UnitRepository unitRepository, AuditService auditService) {
        this.unitRepository = unitRepository;
        this.auditService = auditService;
    }

    public Unit saveUnit(Unit unit) {
        isUnitValid(unit);
        auditService.setAuditData(unit, true);
        unitRepository.save(unit);
        return unit;
    }

    public void editUnit(Long id, Unit unit, User currentUser) {
        Unit existingUnit = findUnitById(id).orElseThrow(() -> new NoSuchElementException("Unit not found"));
        verifyOwnershipOrSystem(existingUnit, currentUser);
        existingUnit.setName(unit.getName());
        existingUnit.setSymbol(unit.getSymbol());
        isUnitValid(existingUnit);
        auditService.setAuditData(existingUnit, false);
        unitRepository.save(existingUnit);
    }

    public List<Unit> findAllAccessibleByUser(Long userId) {
        return unitRepository.findAllAccessibleByUserId(userId);
    }

    public Optional<Unit> findUnitById(Long id) {
        return unitRepository.findByIdAndDeletedFalse(id);
    }

    public Optional<Unit> findUnitBySymbol(String symbol) {
        return unitRepository.findBySymbol(symbol);
    }

    public Optional<Unit> findUnitByName(String name) {
        return unitRepository.findByName(name);
    }

    public void removeUnitById(Long id, User currentUser) {
        Unit unit = findUnitById(id).orElseThrow(() -> new NoSuchElementException("Unit not found"));
        verifyOwnershipOrSystem(unit, currentUser);
        auditService.softDelete(unit);
        auditService.setAuditData(unit, false);
        unitRepository.save(unit);
    }

    public void verifyOwnershipOrSystem(Unit unit, User currentUser) {
        if (unit.isSystemStandard()) {
            throw new ResourceOwnershipException("System standard units cannot be modified or deleted!");
        }
        if (unit.getOwner() == null || !unit.getOwner().getId().equals(currentUser.getId())) {
            throw new ResourceOwnershipException("You can only modify or delete units you own!");
        }
    }

    public void isUnitValid(Unit unit) {
        unit.checkName();
        checkSymbol(unit.getSymbol());
    }

    private void checkSymbol(String symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("The unit symbol cannot be null!");
        } else if (symbol.isBlank()) {
            throw new IllegalArgumentException("Enter a valid unit symbol!");
        }
    }
}

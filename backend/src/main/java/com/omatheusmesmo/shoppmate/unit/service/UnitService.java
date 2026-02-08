package com.omatheusmesmo.shoppmate.unit.service;

import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.repository.UnitRepository;
import com.omatheusmesmo.shoppmate.shared.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UnitService {
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private AuditService auditService;

    public Unit saveUnit(Unit unit){
        isUnitValid(unit);
        auditService.setAuditData(unit, true);
        unitRepository.save(unit);
        return unit;
    }

    public void editUnit(Unit unit) {
        isUnitValid(unit);
        if (unitExists(unit)) {
            throw new NoSuchElementException("Unit not found");
        }
        auditService.setAuditData(unit, false);
        saveUnit(unit);
    }

    public List<Unit> findAll() {
        return unitRepository.findAll();
    }

    public Optional<Unit> findUnitById(Long id){
        return unitRepository.findById(id);
    }

    public Optional<Unit> findUnitBySymbol(String symbol){
        return unitRepository.findBySymbol(symbol);
    }

    public Optional<Unit> findUnitByName(String name){
        return unitRepository.findByName(name);
    }

    public void removeUnit(Unit unit){
        auditService.softDelete(unit);
        saveUnit(unit);
    }

    public void removeUnitById(Long id) {
        Optional<Unit> unit = findUnitById(id);
        Unit deletedUnit = unit.get();
        auditService.softDelete(deletedUnit);
        saveUnit(deletedUnit);
    }

    private boolean unitExists(Unit unit){
        return unitRepository.existsById(unit.getId());
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

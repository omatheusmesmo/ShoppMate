package com.omatheusmesmo.shoppmate.unit.controller;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

import com.omatheusmesmo.shoppmate.unit.dto.UnitRequestDTO;
import com.omatheusmesmo.shoppmate.unit.dto.UnitResponseDTO;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.unit.mapper.UnitMapper;
import com.omatheusmesmo.shoppmate.unit.service.UnitService;
import com.omatheusmesmo.shoppmate.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/unit")
public class UnitController {

    private final UnitService unitService;
    private final UnitMapper unitMapper;

    public UnitController(UnitService unitService, UnitMapper unitMapper) {
        this.unitService = unitService;
        this.unitMapper = unitMapper;
    }

    @Operation(summary = "Return all accessible units")
    @GetMapping
    public ResponseEntity<List<UnitResponseDTO>> getAllUnits(@AuthenticationPrincipal User user) {
        List<Unit> units = unitService.findAllAccessibleByUser(user.getId());
        List<UnitResponseDTO> responseDTOs = units.stream().map(unitMapper::toResponseDTO).toList();
        return ResponseEntity.ok(responseDTOs);
    }

    @Operation(summary = "Add a new unit")
    @PostMapping
    public ResponseEntity<UnitResponseDTO> addUnit(@RequestBody @Valid UnitRequestDTO unitRequestDTO,
            @AuthenticationPrincipal User user) {
        Unit unit = unitMapper.toEntity(unitRequestDTO, user);
        Unit addedUnit = unitService.saveUnit(unit);
        UnitResponseDTO responseDTO = unitMapper.toResponseDTO(addedUnit);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(addedUnit.getId())
                .toUri();
        return ResponseEntity.created(location).body(responseDTO);
    }

    @Operation(summary = "Delete a unit by id")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUnit(@PathVariable Long id, @AuthenticationPrincipal User user) {
        unitService.removeUnitById(id, user);
    }

    @Operation(summary = "Update a unit")
    @PutMapping("/{id}")
    public ResponseEntity<UnitResponseDTO> updateUnit(@PathVariable Long id,
            @RequestBody @Valid UnitRequestDTO unitRequestDTO, @AuthenticationPrincipal User user) {
        Unit unit = unitMapper.toEntity(unitRequestDTO, user);
        unit.setId(id);
        unitService.editUnit(id, unit, user);
        Unit updatedUnit = unitService.findUnitById(id).orElseThrow(() -> new NoSuchElementException("Unit not found"));
        UnitResponseDTO responseDTO = unitMapper.toResponseDTO(updatedUnit);
        return ResponseEntity.ok(responseDTO);
    }
}

package com.omatheusmesmo.shoppmate.unit.mapper;

import com.omatheusmesmo.shoppmate.unit.dto.UnitResponseDTO;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import org.springframework.stereotype.Component;

@Component
public class UnitMapper {

    public Unit toEntity(UnitResponseDTO unitRequest) {
        Unit unit = new Unit();
        unit.setSymbol(unitRequest.symbol());
        return unit;
    }

    public Unit toEntity(Long id, UnitResponseDTO unitRequest) {
        Unit unit = new Unit();
        unit.setId(id);
        unit.setSymbol(unitRequest.symbol());

        return unit;
    }

    public UnitResponseDTO toResponseDTO(Unit unit) {
        return new UnitResponseDTO(unit.getId(), unit.getSymbol());
    }
}

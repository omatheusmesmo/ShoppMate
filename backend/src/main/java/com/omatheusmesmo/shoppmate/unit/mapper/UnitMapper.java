package com.omatheusmesmo.shoppmate.unit.mapper;

import org.springframework.stereotype.Component;

import com.omatheusmesmo.shoppmate.unit.dto.UnitRequestDTO;
import com.omatheusmesmo.shoppmate.unit.dto.UnitResponseDTO;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import com.omatheusmesmo.shoppmate.user.dtos.UserResponseDTO;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.mapper.UserMapper;

@Component
public class UnitMapper {

    private final UserMapper userMapper;

    public UnitMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Unit toEntity(UnitRequestDTO requestDTO, User owner) {
        var unit = new Unit();
        unit.setName(requestDTO.name());
        unit.setSymbol(requestDTO.symbol());
        unit.setSystemStandard(false);
        unit.setOwner(owner);
        return unit;
    }

    public UnitResponseDTO toResponseDTO(Unit unit) {
        var ownerDTO = unit.getOwner() != null ? userMapper.toResponseDTO(unit.getOwner()) : null;
        return new UnitResponseDTO(unit.getId(), unit.getName(), unit.getSymbol(), unit.isSystemStandard(), ownerDTO);
    }
}

package com.omatheusmesmo.shoppmate.list.mapper;

import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListResponseDTO;
import com.omatheusmesmo.shoppmate.list.dtos.ShoppingListUpdateRequestDTO;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import com.omatheusmesmo.shoppmate.user.dtos.UserResponseDTO;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.user.mapper.UserMapper;
import com.omatheusmesmo.shoppmate.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListMapper {

    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public ListMapper(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    public ShoppingList toEntity(ShoppingListRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        ShoppingList entity = new ShoppingList();
        entity.setName(dto.name());

        User owner = userService.findUser(dto.idUser());
        entity.setOwner(owner);

        return entity;
    }

    public ShoppingListResponseDTO toResponseDTO(ShoppingList entity) {
        if (entity == null) {
            return null;
        }

        UserResponseDTO ownerDTO = userMapper.toResponseDTO(entity.getOwner());

        return new ShoppingListResponseDTO(entity.getId(), entity.getName(), ownerDTO);
    }

    public void updateEntityFromDto(ShoppingListUpdateRequestDTO dto, ShoppingList entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setName(dto.name());
    }
}

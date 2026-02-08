package com.omatheusmesmo.shoppmate.list.mapper;

import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionSummaryDTO;
import com.omatheusmesmo.shoppmate.user.dtos.UserResponseDTO;
import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionRequestDTO;
import com.omatheusmesmo.shoppmate.list.dtos.listpermission.ListPermissionResponseDTO;
import com.omatheusmesmo.shoppmate.list.entity.ListPermission;
import com.omatheusmesmo.shoppmate.list.entity.ShoppingList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListPermissionMapper {

    @Autowired
    private ListMapper listMapper;
    
    public ListPermission toEntity(ListPermissionRequestDTO dto, ShoppingList shoppingList, User user) {
        ListPermission listPermission = new ListPermission();
        listPermission.setShoppingList(shoppingList);
        listPermission.setPermission(dto.permission());
        listPermission.setUser(user);
        return listPermission;
    }

    public ListPermissionResponseDTO toResponseDTO(ListPermission listPermission) {
        UserResponseDTO userResponseDTO = new UserResponseDTO(
                listPermission.getUser().getId(),
                listPermission.getUser().getFullName(),
                listPermission.getUser().getEmail());

        return new ListPermissionResponseDTO(
                listPermission.getId(),
                listMapper.toResponseDTO(listPermission.getShoppingList()),
                userResponseDTO,
                listPermission.getPermission()
        );
    }

    public ListPermissionSummaryDTO toSummaryDTO(ListPermission listPermission){
        return new ListPermissionSummaryDTO(
                listPermission.getId(),
                listPermission.getUser().getFullName(),
                listPermission.getUser().getEmail(),
                listPermission.getPermission()
        );
    }
}
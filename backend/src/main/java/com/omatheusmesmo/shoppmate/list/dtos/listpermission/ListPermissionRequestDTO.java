package com.omatheusmesmo.shoppmate.list.dtos.listpermission;

import com.omatheusmesmo.shoppmate.list.entity.Permission;
import jakarta.validation.constraints.NotNull;

public record ListPermissionRequestDTO(
        @NotNull(message = "List ID cannot be null")
        Long idList,
        @NotNull(message = "User ID cannot be null")
        Long idUser,
        @NotNull(message = "Permission cannot be null")
        Permission permission
        ) {
}

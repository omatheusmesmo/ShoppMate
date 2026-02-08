package com.omatheusmesmo.shoppmate.list.dtos.listpermission;

import com.omatheusmesmo.shoppmate.list.entity.Permission;
import jakarta.validation.constraints.NotNull;

public record ListPermissionUpdateRequestDTO(
        @NotNull(message = "Permission cannot be null")
        Permission permission
        ) {
}

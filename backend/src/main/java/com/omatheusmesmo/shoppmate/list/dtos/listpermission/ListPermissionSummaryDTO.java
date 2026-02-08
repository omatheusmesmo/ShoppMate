package com.omatheusmesmo.shoppmate.list.dtos.listpermission;

import com.omatheusmesmo.shoppmate.list.entity.Permission;

public record ListPermissionSummaryDTO(
        Long id,
        String userFullName,
        String userEmail,
        Permission permission
) {
}

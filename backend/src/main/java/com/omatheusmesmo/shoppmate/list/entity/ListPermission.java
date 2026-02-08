package com.omatheusmesmo.shoppmate.list.entity;

import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.shared.domain.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "list_user_permissions")
@Getter
@Setter
public class ListPermission extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_list", nullable = false)
    private ShoppingList shoppingList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @Column(name = "permission_type")
    private Permission permission;
}

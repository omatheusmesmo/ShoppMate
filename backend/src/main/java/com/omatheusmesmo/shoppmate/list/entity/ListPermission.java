package com.omatheusmesmo.shoppmate.list.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import com.omatheusmesmo.shoppmate.shared.domain.BaseAuditableEntity;
import com.omatheusmesmo.shoppmate.user.entity.User;

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
    @Enumerated(EnumType.STRING)
    private Permission permission;
}

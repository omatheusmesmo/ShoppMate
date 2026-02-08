package com.omatheusmesmo.shoppmate.list.entity;

import com.omatheusmesmo.shoppmate.user.entity.User;
import com.omatheusmesmo.shoppmate.shared.domain.DomainEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="lists")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingList extends DomainEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id_user", nullable = false)
    private User owner;
}

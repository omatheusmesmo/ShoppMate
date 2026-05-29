package com.omatheusmesmo.shoppmate.category.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.omatheusmesmo.shoppmate.shared.domain.DomainEntity;
import com.omatheusmesmo.shoppmate.user.entity.User;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class Category extends DomainEntity {

    @Column(name = "is_system_standard", nullable = false)
    private boolean isSystemStandard = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;
}

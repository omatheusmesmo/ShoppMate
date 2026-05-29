package com.omatheusmesmo.shoppmate.item.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.shared.domain.DomainEntity;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "items")
public class Item extends DomainEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_category", nullable = false)
    private Category category;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unit", nullable = false)
    private Unit unit;
}

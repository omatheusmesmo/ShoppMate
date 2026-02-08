package com.omatheusmesmo.shoppmate.item.entity;

import com.omatheusmesmo.shoppmate.category.entity.Category;
import com.omatheusmesmo.shoppmate.shared.domain.DomainEntity;
import com.omatheusmesmo.shoppmate.unit.entity.Unit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


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

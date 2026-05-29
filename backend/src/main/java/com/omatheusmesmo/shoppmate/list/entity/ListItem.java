package com.omatheusmesmo.shoppmate.list.entity;

import com.omatheusmesmo.shoppmate.item.entity.Item;
import com.omatheusmesmo.shoppmate.shared.domain.BaseAuditableEntity;
import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "list_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListItem extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_list", nullable = false)
    private ShoppingList shoppList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_item", nullable = false)
    private Item item;

    private Integer quantity;
    private Boolean purchased = false;

    private BigDecimal unitPrice;
}

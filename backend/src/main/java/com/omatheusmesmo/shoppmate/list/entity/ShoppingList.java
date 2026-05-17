package com.omatheusmesmo.shoppmate.list.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.omatheusmesmo.shoppmate.shared.domain.DomainEntity;
import com.omatheusmesmo.shoppmate.user.entity.User;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@SQLDelete(sql = "UPDATE lists SET deleted = true WHERE id=?")
@Where(clause = "deleted = false")
@Table(name = "lists")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingList extends DomainEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id_user", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "shoppList", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ListItem> items;
}

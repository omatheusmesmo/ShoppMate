package com.omatheusmesmo.shoppmate.category.entity;

import com.omatheusmesmo.shoppmate.shared.domain.DomainEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "categories")
@SQLDelete(sql = "UPDATE categories SET deleted = true WHERE id=?")
@Where(clause = "deleted = false")
@Getter
@Setter
public class Category extends DomainEntity {
}

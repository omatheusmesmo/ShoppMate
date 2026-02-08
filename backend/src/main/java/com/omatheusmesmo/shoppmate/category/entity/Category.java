package com.omatheusmesmo.shoppmate.category.entity;

import com.omatheusmesmo.shoppmate.shared.domain.DomainEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="categories")
@Getter
@Setter
public class Category extends DomainEntity {
}

package com.omatheusmesmo.shoppmate.unit.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import com.omatheusmesmo.shoppmate.shared.domain.DomainEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@SQLDelete(sql = "UPDATE units SET deleted = true WHERE id=?")
@Where(clause = "deleted = false")
@Table(name = "units")
@Getter
@Setter
public class Unit extends DomainEntity {

    private String symbol;
}

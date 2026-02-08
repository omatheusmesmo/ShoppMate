package com.omatheusmesmo.shoppmate.unit.entity;

import com.omatheusmesmo.shoppmate.shared.domain.DomainEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="units")
@Getter
@Setter
public class Unit extends DomainEntity {

    private String symbol;
}

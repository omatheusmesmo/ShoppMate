package com.omatheusmesmo.shoppmate.shared.domain;

import com.omatheusmesmo.shoppmate.shared.utils.SnowflakeIdentifierGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseAuditableEntity implements AuditableEntity {

    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(name = "snowflake", type = SnowflakeIdentifierGenerator.class)
    @Column(name = "id")
    private Long id;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Boolean deleted = false;
}

package com.omatheusmesmo.shoppmate.shared.domain;

import com.omatheusmesmo.shoppmate.shared.utils.SnowflakeIdentifierGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.EntityListeners;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class DomainEntity implements AuditableEntity {

    @Id
    @GeneratedValue(generator = "snowflake")
    @GenericGenerator(name = "snowflake", type = SnowflakeIdentifierGenerator.class)
    @Column(name = "id")
    private Long id;

    private String name;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Boolean deleted = false;

    public void checkName() {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null!");
        } else if (name.isBlank()) {
            throw new IllegalArgumentException("Enter a valid name!");
        }
    }
}

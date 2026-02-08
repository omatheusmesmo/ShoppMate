package com.omatheusmesmo.shoppmate.shared.domain;

import java.time.LocalDateTime;

public interface AuditableEntity {
    void setCreatedAt(LocalDateTime timestamp);
    void setUpdatedAt(LocalDateTime timestamp);
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    void setDeleted(Boolean deleted);
    Boolean getDeleted();
}

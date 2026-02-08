package com.omatheusmesmo.shoppmate.shared.service;

import com.omatheusmesmo.shoppmate.shared.domain.AuditableEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuditService {
    public <T extends AuditableEntity> void setAuditData(T entity, boolean isNew){
        LocalDateTime now = LocalDateTime.now();
        if(isNew){
            entity.setCreatedAt(now);
            entity.setDeleted(false);
        }else{
            entity.setUpdatedAt(now);
        }
    }

    public <T extends AuditableEntity> void softDelete(T entity){
        entity.setDeleted(true);
        entity.setUpdatedAt(LocalDateTime.now());
    }
}

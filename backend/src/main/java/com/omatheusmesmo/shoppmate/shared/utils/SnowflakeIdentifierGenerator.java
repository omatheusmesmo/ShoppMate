package com.omatheusmesmo.shoppmate.shared.utils;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class SnowflakeIdentifierGenerator implements IdentifierGenerator, ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(org.springframework.context.ApplicationContext applicationContext) {
        context = applicationContext;
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        if (context == null) {
            return new SnowflakeIdGenerator().nextId();
        }
        return context.getBean(SnowflakeIdGenerator.class).nextId();
    }
}

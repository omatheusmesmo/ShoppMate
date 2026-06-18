package com.omatheusmesmo.shoppmate.auth.configs;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.distributed.jdbc.SQLProxyConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.postgresql.PostgreSQLadvisoryLockBasedProxyManager;

@Configuration
public class Bucket4jPostgresConfig {

    @Bean
    public ProxyManager<Long> bucket4jProxyManager(DataSource dataSource) {
        SQLProxyConfiguration<Long> sqlProxyConfiguration = SQLProxyConfiguration.builder().build(dataSource);

        return new PostgreSQLadvisoryLockBasedProxyManager(sqlProxyConfiguration);
    }
}

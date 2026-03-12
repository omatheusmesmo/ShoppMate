package com.omatheusmesmo.shoppmate.shared.testcontainers;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Map;
import java.util.stream.Stream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = AbstractIntegrationTest.Initializer.class)
public class AbstractIntegrationTest {

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:18");

        private static void startContainers() {
            Startables.deepStart(Stream.of(postgreSQLContainer)).join();
        }

        private static Map<String, Object> createConnectionConfiguration() {
            return Map.of("spring.datasource.url", postgreSQLContainer.getJdbcUrl(), "spring.datasource.username",
                    postgreSQLContainer.getUsername(), "spring.datasource.password", postgreSQLContainer.getPassword(),
                    "jwt.private-key", "file:src/test/resources/certs/private_key.pem", "jwt.public-key",
                    "file:src/test/resources/certs/public_key.pem");
        }

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            startContainers();
            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            MapPropertySource testcontainers = new MapPropertySource("testcontainers", createConnectionConfiguration());
            environment.getPropertySources().addFirst(testcontainers);
        }
    }
}

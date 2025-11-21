package io.github.tourem.test.micronaut;

import io.github.tourem.test.micronaut.config.ApiConfig;
import io.github.tourem.test.micronaut.config.DatabaseConfig;
import io.github.tourem.test.micronaut.config.MessagingConfig;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class ConfigValidationTest {

    @Inject
    DatabaseConfig databaseConfig;

    @Inject
    ApiConfig apiConfig;

    @Inject
    MessagingConfig messagingConfig;

    @Test
    void testDatabaseConfigLoaded() {
        assertNotNull(databaseConfig);
        assertEquals("jdbc:postgresql://localhost:5432/testdb", databaseConfig.getUrl());
        assertEquals("testuser", databaseConfig.getUsername());
        // password and timeout are missing - config-preflight should detect this
    }

    @Test
    void testApiConfigLoaded() {
        assertNotNull(apiConfig);
        assertEquals("test-api-key-12345", apiConfig.getApiKey());
        assertEquals(3, apiConfig.getRetryCount());
        // endpoint and cacheDirectory are missing - config-preflight should detect this
    }

    @Test
    void testMessagingConfigLoaded() {
        assertNotNull(messagingConfig);
        assertEquals("amqp://localhost:5672", messagingConfig.getBrokerUrl());
        assertEquals("guest", messagingConfig.getUsername());
        // queueName and connectionTimeout are missing - config-preflight should detect this
    }
}

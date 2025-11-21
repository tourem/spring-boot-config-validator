package io.github.tourem.test.quarkus;

import io.github.tourem.test.quarkus.config.ApiConfig;
import io.github.tourem.test.quarkus.config.DatabaseConfig;
import io.github.tourem.test.quarkus.config.MessagingConfig;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
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
        assertEquals("jdbc:postgresql://localhost:5432/testdb", databaseConfig.url());
        assertEquals("testuser", databaseConfig.username());
        // password and timeout are missing - config-preflight should detect this
    }

    @Test
    void testApiConfigLoaded() {
        assertNotNull(apiConfig);
        assertEquals("test-api-key-12345", apiConfig.apiKey());
        assertEquals(3, apiConfig.retryCount());
        // endpoint and cacheDirectory are missing - config-preflight should detect this
    }

    @Test
    void testMessagingConfigLoaded() {
        assertNotNull(messagingConfig);
        assertEquals("amqp://localhost:5672", messagingConfig.brokerUrl());
        assertEquals("guest", messagingConfig.username());
        // queueName and connectionTimeout are missing - config-preflight should detect this
    }
}

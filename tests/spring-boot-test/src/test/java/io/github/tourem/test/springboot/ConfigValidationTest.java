package io.github.tourem.test.springboot;

import io.github.tourem.test.springboot.config.ApiConfig;
import io.github.tourem.test.springboot.config.DatabaseConfig;
import io.github.tourem.test.springboot.config.MessagingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConfigValidationTest {

    @Autowired
    private DatabaseConfig databaseConfig;

    @Autowired
    private ApiConfig apiConfig;

    @Autowired
    private MessagingConfig messagingConfig;

    @Test
    void testDatabaseConfigLoaded() {
        assertThat(databaseConfig).isNotNull();
        assertThat(databaseConfig.getUrl()).isEqualTo("jdbc:postgresql://localhost:5432/testdb");
        assertThat(databaseConfig.getUsername()).isEqualTo("testuser");
        // password and timeout are missing - config-preflight should detect this
    }

    @Test
    void testApiConfigLoaded() {
        assertThat(apiConfig).isNotNull();
        assertThat(apiConfig.getApiKey()).isEqualTo("test-api-key-12345");
        assertThat(apiConfig.getRetryCount()).isEqualTo(3);
        // endpoint and cacheDirectory are missing - config-preflight should detect this
    }

    @Test
    void testMessagingConfigLoaded() {
        assertThat(messagingConfig).isNotNull();
        assertThat(messagingConfig.getBrokerUrl()).isEqualTo("amqp://localhost:5672");
        assertThat(messagingConfig.getUsername()).isEqualTo("guest");
        // queueName and connectionTimeout are missing - config-preflight should detect this
    }
}

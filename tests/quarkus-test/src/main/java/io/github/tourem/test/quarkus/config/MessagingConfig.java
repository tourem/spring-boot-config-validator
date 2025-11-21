package io.github.tourem.test.quarkus.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "messaging")
public interface MessagingConfig {
    String brokerUrl();
    String queueName();
    String username();
    String password();
    Integer connectionTimeout();
    Boolean autoReconnect();
}

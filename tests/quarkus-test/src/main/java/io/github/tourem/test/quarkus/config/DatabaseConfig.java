package io.github.tourem.test.quarkus.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "database")
public interface DatabaseConfig {
    String url();
    String username();
    String password();
    Integer maxConnections();
    Long timeout();
}

package io.github.tourem.test.quarkus.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "api")
public interface ApiConfig {
    String endpoint();
    String apiKey();
    Integer retryCount();
    Boolean enableCache();
    String cacheDirectory();
}

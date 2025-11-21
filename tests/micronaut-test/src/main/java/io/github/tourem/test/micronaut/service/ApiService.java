package io.github.tourem.test.micronaut.service;

import io.github.tourem.test.micronaut.config.ApiConfig;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service qui utilise ApiConfig.
 * Démontre comment config-preflight évite les NullPointerException.
 */
@Singleton
public class ApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);
    
    private final ApiConfig config;
    
    public ApiService(ApiConfig config) {
        this.config = config;
        logger.info("✅ ApiService created (config-preflight validated all properties)");
    }
    
    public void callApi() {
        // Sans config-preflight, ces appels crasheraient si les propriétés sont null
        logger.info("Calling API at: {}", config.getEndpoint());
        logger.info("Retry count: {}", config.getRetryCount());
        logger.info("Cache enabled: {}", config.getEnableCache());
        
        if (config.getCacheDirectory() == null) {
            throw new IllegalStateException("Cache directory is required!");
        }
        
        logger.info("Using cache directory: {}", config.getCacheDirectory());
        logger.info("✅ API call successful");
    }
}

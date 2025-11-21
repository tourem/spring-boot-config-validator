package io.github.tourem.test.quarkus.service;

import io.github.tourem.test.quarkus.config.ApiConfig;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service qui utilise ApiConfig.
 * Démontre comment config-preflight évite les NullPointerException.
 */
@ApplicationScoped
public class ApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);
    
    private final ApiConfig config;
    
    public ApiService(ApiConfig config) {
        this.config = config;
        logger.info("✅ ApiService created (config-preflight validated all properties)");
    }
    
    public void callApi() {
        // Sans config-preflight, ces appels crasheraient si les propriétés sont null
        logger.info("Calling API at: {}", config.endpoint());
        logger.info("Retry count: {}", config.retryCount());
        logger.info("Cache enabled: {}", config.enableCache());
        
        if (config.cacheDirectory() == null) {
            throw new IllegalStateException("Cache directory is required!");
        }
        
        logger.info("Using cache directory: {}", config.cacheDirectory());
        logger.info("✅ API call successful");
    }
}

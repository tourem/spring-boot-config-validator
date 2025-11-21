package io.github.tourem.test.micronaut.service;

import io.github.tourem.test.micronaut.config.DatabaseConfig;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service qui utilise DatabaseConfig.
 * 
 * DÉMONSTRATION:
 * - SANS config-preflight: Ce service crasherait avec NullPointerException
 *   quand connect() est appelé si database.password ou database.timeout sont null
 * - AVEC config-preflight: L'application ne démarre même pas, vous voyez
 *   immédiatement un rapport formaté listant TOUTES les propriétés manquantes
 * 
 * Config-preflight bloque le démarrage AVANT que ce service ne soit créé,
 * vous évitant ainsi de découvrir les erreurs une par une.
 */
@Singleton
public class DatabaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    
    private final DatabaseConfig config;
    
    public DatabaseService(DatabaseConfig config) {
        this.config = config;
        logger.info("✅ DatabaseService created (config-preflight validated all properties)");
    }
    
    public void connect() {
        // Sans config-preflight, cette méthode crasherait si password ou timeout sont null
        logger.info("Connecting to database at: {}", config.getUrl());
        logger.info("Using username: {}", config.getUsername());
        logger.info("Max connections: {}", config.getMaxConnections());
        logger.info("Timeout: {} ms", config.getTimeout());
        
        // Cette ligne crasherait sans config-preflight
        if (config.getPassword() == null) {
            throw new IllegalStateException("Database password is required!");
        }
        
        if (config.getTimeout() == null) {
            throw new IllegalStateException("Database timeout is required!");
        }
        
        logger.info("✅ Connected successfully");
    }
}

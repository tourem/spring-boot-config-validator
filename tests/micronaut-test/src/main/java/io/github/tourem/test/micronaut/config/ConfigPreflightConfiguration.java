package io.github.tourem.test.micronaut.config;

import com.mycompany.validator.core.api.ValidationResult;
import com.mycompany.validator.core.detector.SecretDetector;
import com.mycompany.validator.core.formatter.BeautifulErrorFormatter;
import com.mycompany.validator.core.model.ConfigurationError;
import com.mycompany.validator.core.model.ErrorType;
import com.mycompany.validator.core.model.PropertySource;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration pour activer config-preflight dans le projet de test.
 * Valide les propriétés requises en vérifiant directement l'Environment.
 */
@Singleton
public class ConfigPreflightConfiguration implements ApplicationEventListener<ServerStartupEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigPreflightConfiguration.class);
    
    private final Environment environment;
    private final SecretDetector secretDetector = new SecretDetector();
    private final BeautifulErrorFormatter formatter = new BeautifulErrorFormatter();
    
    // Liste des propriétés requises pour chaque bean
    private static final List<String> REQUIRED_PROPERTIES = Arrays.asList(
        "database.url",
        "database.username",
        "database.password",
        "database.max-connections",
        "database.timeout",
        "api.endpoint",
        "api.api-key",
        "api.retry-count",
        "api.enable-cache",
        "api.cache-directory",
        "messaging.broker-url",
        "messaging.queue-name",
        "messaging.username",
        "messaging.password",
        "messaging.connection-timeout",
        "messaging.auto-reconnect"
    );
    
    public ConfigPreflightConfiguration(Environment environment) {
        this.environment = environment;
    }
    
    @Override
    public void onApplicationEvent(ServerStartupEvent event) {
        logger.info("🔍 Scanning required configuration properties...");
        
        // Debug: vérifier quel fichier est chargé
        logger.info("Application name: {}", environment.getProperty("micronaut.application.name", String.class).orElse("NOT SET"));
        logger.info("Active environments: {}", environment.getActiveNames());
        
        // Debug: afficher toutes les propriétés "api.*"
        logger.debug("All api.* properties:");
        for (String key : environment.getPropertyEntries("api")) {
            logger.debug("  {} = {}", key, environment.getProperty(key, String.class).orElse("null"));
        }
        
        List<ConfigurationError> errors = new ArrayList<>();
        
        // Vérifier chaque propriété requise dans l'Environment
        for (String propertyName : REQUIRED_PROPERTIES) {
            String value = environment.getProperty(propertyName, String.class).orElse(null);
            logger.debug("Checking property '{}': {}", propertyName, value != null ? "SET" : "NOT SET");
            
            if (value == null) {
                boolean isSensitive = secretDetector.isSensitive(propertyName);
                
                logger.warn("Property '{}' is not set", propertyName);
                
                errors.add(ConfigurationError.builder()
                    .type(ErrorType.MISSING_PROPERTY)
                    .propertyName(propertyName)
                    .source(new PropertySource("application.yml", "classpath:application.yml", PropertySource.SourceType.APPLICATION_YAML))
                    .errorMessage("Property '" + propertyName + "' is not set")
                    .suggestion(generateSuggestion(propertyName))
                    .isSensitive(isSensitive)
                    .build());
            }
        }
        
        if (!errors.isEmpty()) {
            ValidationResult result = new ValidationResult(errors);
            String formattedErrors = formatter.format(result);
            
            System.err.println(formattedErrors);
            logger.error("❌ Configuration validation failed with {} error(s)", errors.size());
            
            // Arrêter l'application
            throw new ConfigurationValidationException(
                "Configuration validation failed with " + errors.size() + " error(s)",
                result
            );
        } else {
            logger.info("✅ All required configuration properties are set");
        }
    }
    
    private String generateSuggestion(String propertyName) {
        String envVarName = propertyName.replace('.', '_').replace('-', '_').toUpperCase();
        return String.format("Add to application.yml: %s: <value>\nOR set environment variable: export %s=<value>",
                propertyName, envVarName);
    }
    
    public static class ConfigurationValidationException extends RuntimeException {
        private final ValidationResult validationResult;
        
        public ConfigurationValidationException(String message, ValidationResult validationResult) {
            super(message);
            this.validationResult = validationResult;
        }
        
        public ValidationResult getValidationResult() {
            return validationResult;
        }
    }
}

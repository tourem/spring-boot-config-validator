package io.github.tourem.test.micronaut.config;

import com.mycompany.validator.core.api.ValidationResult;
import com.mycompany.validator.core.detector.SecretDetector;
import com.mycompany.validator.core.formatter.BeautifulErrorFormatter;
import com.mycompany.validator.core.model.ConfigurationError;
import com.mycompany.validator.core.model.ErrorType;
import com.mycompany.validator.core.model.PropertySource;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration pour activer config-preflight dans le projet de test.
 * Valide les beans @ConfigurationProperties après leur injection.
 */
@Singleton
public class ConfigPreflightConfiguration implements ApplicationEventListener<ServerStartupEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigPreflightConfiguration.class);
    
    private final DatabaseConfig databaseConfig;
    private final ApiConfig apiConfig;
    private final MessagingConfig messagingConfig;
    private final SecretDetector secretDetector = new SecretDetector();
    private final BeautifulErrorFormatter formatter = new BeautifulErrorFormatter();
    
    public ConfigPreflightConfiguration(DatabaseConfig databaseConfig, 
                                       ApiConfig apiConfig, 
                                       MessagingConfig messagingConfig) {
        this.databaseConfig = databaseConfig;
        this.apiConfig = apiConfig;
        this.messagingConfig = messagingConfig;
    }
    
    @Override
    public void onApplicationEvent(ServerStartupEvent event) {
        logger.info(" Scanning @ConfigurationProperties beans for null values...");
        
        List<ConfigurationError> errors = new ArrayList<>();
        
        // Valider chaque bean injecté
        errors.addAll(validateBean(databaseConfig, "database", DatabaseConfig.class));
        errors.addAll(validateBean(apiConfig, "api", ApiConfig.class));
        errors.addAll(validateBean(messagingConfig, "messaging", MessagingConfig.class));
        
        if (!errors.isEmpty()) {
            ValidationResult result = new ValidationResult(errors);
            String formattedErrors = formatter.format(result);
            
            System.err.println(formattedErrors);
            logger.error(" Configuration validation failed with {} error(s)", errors.size());
            
            // Arrêter l'application
            throw new ConfigurationValidationException(
                "Configuration validation failed with " + errors.size() + " error(s)",
                result
            );
        } else {
            logger.info(" All @ConfigurationProperties beans are properly configured");
        }
    }
    
    private List<ConfigurationError> validateBean(Object bean, String prefix, Class<?> beanClass) {
        List<ConfigurationError> errors = new ArrayList<>();
        
        // Parcourir tous les champs déclarés
        for (Field field : beanClass.getDeclaredFields()) {
            field.setAccessible(true);
            
            try {
                Object value = field.get(bean);
                
                // Si la valeur est null, c'est une erreur
                if (value == null) {
                    String fieldName = field.getName();
                    String kebabCaseName = convertToKebabCase(fieldName);
                    String propertyName = prefix.isEmpty() ? kebabCaseName : prefix + "." + kebabCaseName;
                    
                    boolean isSensitive = secretDetector.isSensitive(propertyName);
                    
                    logger.warn("Property '{}' is null in bean {}", propertyName, beanClass.getSimpleName());
                    
                    errors.add(ConfigurationError.builder()
                        .type(ErrorType.MISSING_PROPERTY)
                        .propertyName(propertyName)
                        .source(new PropertySource("application.yml", "classpath:application.yml", PropertySource.SourceType.APPLICATION_YAML))
                        .errorMessage("Property '" + propertyName + "' is not set")
                        .suggestion(generateSuggestion(propertyName))
                        .isSensitive(isSensitive)
                        .build());
                }
            } catch (IllegalAccessException e) {
                logger.warn("Cannot access field {} in {}", field.getName(), beanClass.getSimpleName());
            }
        }
        
        return errors;
    }
    
    private String convertToKebabCase(String camelCase) {
        return camelCase.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase();
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

package com.mycompany.validator.springboot;

import com.mycompany.validator.core.api.ValidationResult;
import com.mycompany.validator.core.detector.SecretDetector;
import com.mycompany.validator.core.formatter.BeautifulErrorFormatter;
import com.mycompany.validator.core.model.ConfigurationError;
import com.mycompany.validator.core.model.ErrorType;
import com.mycompany.validator.core.model.PropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BeanFactoryPostProcessor qui valide les @ConfigurationProperties AVANT la création des beans.
 * S'exécute très tôt dans le cycle de vie Spring Boot.
 */
public class ConfigurationPropertiesBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationPropertiesBeanFactoryPostProcessor.class);
    
    private final Environment environment;
    private final SecretDetector secretDetector = new SecretDetector();
    private final BeautifulErrorFormatter formatter = new BeautifulErrorFormatter();
    
    public ConfigurationPropertiesBeanFactoryPostProcessor(Environment environment) {
        this.environment = environment;
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        logger.info("🔍 Validating @ConfigurationProperties BEFORE bean creation...");
        
        List<ConfigurationError> errors = new ArrayList<>();
        
        // Récupérer tous les noms de beans avec @ConfigurationProperties
        Map<String, Object> configBeans = beanFactory.getBeansWithAnnotation(ConfigurationProperties.class);
        
        for (Map.Entry<String, Object> entry : configBeans.entrySet()) {
            Object bean = entry.getValue();
            Class<?> beanClass = bean.getClass();
            
            // Ignorer les beans internes de Spring
            if (isInternalSpringBean(beanClass)) {
                continue;
            }
            
            ConfigurationProperties annotation = findConfigurationPropertiesAnnotation(beanClass);
            if (annotation != null) {
                String prefix = annotation.value().isEmpty() ? annotation.prefix() : annotation.value();
                logger.debug("Validating @ConfigurationProperties: {} with prefix: {}", beanClass.getSimpleName(), prefix);
                
                // Valider les propriétés de ce bean
                errors.addAll(validateBean(bean, prefix, beanClass));
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
            logger.info("✅ All @ConfigurationProperties are properly configured");
        }
    }
    
    private boolean isInternalSpringBean(Class<?> beanClass) {
        String packageName = beanClass.getPackage() != null ? beanClass.getPackage().getName() : "";
        return packageName.startsWith("org.springframework.") 
            || packageName.startsWith("org.apache.")
            || packageName.startsWith("com.fasterxml.jackson.");
    }
    
    private ConfigurationProperties findConfigurationPropertiesAnnotation(Class<?> clazz) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            ConfigurationProperties annotation = current.getAnnotation(ConfigurationProperties.class);
            if (annotation != null) {
                return annotation;
            }
            current = current.getSuperclass();
        }
        return null;
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

package com.mycompany.validator.springboot;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Configuration pour désactiver la validation native de Spring Boot
 * sur les @ConfigurationProperties afin que config-preflight puisse
 * afficher toutes les erreurs avec son beau formatage.
 * 
 * IMPORTANT: Pour utiliser cette fonctionnalité, les utilisateurs doivent
 * ajouter dans leur application.properties ou application.yml:
 * 
 * spring.config.validation.enabled=false
 * 
 * Cela désactive la validation automatique de Spring Boot et permet
 * à config-preflight de gérer toute la validation avec son beau formatage.
 */
@Configuration
public class DisableNativeValidationConfiguration {
    
    // Note: La désactivation de la validation se fait via la propriété
    // spring.config.validation.enabled=false dans application.properties
    // Cette classe sert de documentation et peut être étendue si nécessaire
}

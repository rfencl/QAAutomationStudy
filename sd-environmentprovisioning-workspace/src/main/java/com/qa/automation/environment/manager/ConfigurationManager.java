package com.qa.automation.environment.manager;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages environment-specific configurations and variable substitution.
 * 
 * Design Decision: Centralized configuration management with variable
 * substitution support for dynamic values like database connections.
 */
@Slf4j
public class ConfigurationManager {
    
    private final Map<String, Map<String, Object>> environmentConfigurations = new ConcurrentHashMap<>();

    /**
     * Applies configurations to an environment.
     */
    public void applyConfigurations(Map<String, Object> configurations, String environmentId) {
        if (configurations == null || configurations.isEmpty()) {
            log.info("No configurations to apply for environment: {}", environmentId);
            return;
        }
        
        log.info("Applying {} configurations for environment: {}", configurations.size(), environmentId);
        
        // Process and store configurations
        Map<String, Object> processedConfigurations = processConfigurations(configurations, environmentId);
        environmentConfigurations.put(environmentId, processedConfigurations);
        
        log.info("Configurations applied successfully for environment: {}", environmentId);
    }

    /**
     * Retrieves configurations for an environment.
     */
    public Map<String, Object> getConfigurations(String environmentId) {
        return environmentConfigurations.getOrDefault(environmentId, Map.of());
    }

    /**
     * Processes configurations with variable substitution.
     */
    private Map<String, Object> processConfigurations(Map<String, Object> configurations, String environmentId) {
        Map<String, Object> processed = new ConcurrentHashMap<>();
        
        configurations.forEach((key, value) -> {
            if (value instanceof String) {
                String processedValue = substituteVariables((String) value, environmentId);
                processed.put(key, processedValue);
                log.debug("Configuration processed: {} = {}", key, processedValue);
            } else {
                processed.put(key, value);
            }
        });
        
        return processed;
    }

    /**
     * Substitutes variables in configuration values.
     * Supports patterns like ${database.user-db.connection-string}
     */
    private String substituteVariables(String value, String environmentId) {
        if (value == null || !value.contains("${")) {
            return value;
        }
        
        String result = value;
        
        // Replace common variables
        result = result.replace("${environment.id}", environmentId);
        result = result.replace("${random.password}", generateRandomPassword());
        
        // Replace database connection variables
        result = substituteDatabaseVariables(result, environmentId);
        
        return result;
    }

    /**
     * Substitutes database-related variables.
     */
    private String substituteDatabaseVariables(String value, String environmentId) {
        String result = value;
        
        // Example substitutions for database connections
        if (result.contains("${database.")) {
            result = result.replace("${database.user-db.connection-string}", 
                "postgresql://admin:password@user-db-" + environmentId + ".example.com:5432/userdb");
            result = result.replace("${database.user-db.username}", "admin");
            result = result.replace("${database.user-db.password}", "password");
            
            result = result.replace("${database.order-db.connection-string}", 
                "mysql://admin:password@order-db-" + environmentId + ".example.com:3306/orderdb");
            result = result.replace("${database.order-db.username}", "admin");
            result = result.replace("${database.order-db.password}", "password");
        }
        
        return result;
    }

    /**
     * Generates a random password for configuration substitution.
     */
    private String generateRandomPassword() {
        return "pwd-" + java.util.UUID.randomUUID().toString().substring(0, 12);
    }

    /**
     * Removes configurations for an environment during cleanup.
     */
    public void removeConfigurations(String environmentId) {
        environmentConfigurations.remove(environmentId);
        log.info("Configurations removed for environment: {}", environmentId);
    }
}

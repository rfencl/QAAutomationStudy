package com.qa.automation.environment.provider;

import com.qa.automation.environment.model.DatabaseInstance;

/**
 * Interface for database provider implementations.
 * 
 * Design Decision: Strategy pattern allows supporting multiple database
 * types (PostgreSQL, MySQL, MongoDB) with consistent interface.
 */
public interface DatabaseProvider {
    
    /**
     * Gets the database type this provider supports.
     */
    String getDatabaseType();
    
    /**
     * Creates a database instance.
     */
    DatabaseInstance createInstance(DatabaseInstanceConfig config);
    
    /**
     * Deletes a database instance.
     */
    void deleteInstance(String instanceId);
    
    /**
     * Checks if the provider is available.
     */
    boolean isAvailable();
}

/**
 * Configuration for database instance creation.
 */
class DatabaseInstanceConfig {
    private String instanceType;
    private String version;
    private String environmentId;
    // Additional configuration properties would be added here
}

package com.qa.automation.environment.manager;

import com.qa.automation.environment.model.*;
import com.qa.automation.environment.provider.DatabaseProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manages database provisioning and configuration.
 * 
 * Design Decision: Uses provider pattern to support multiple database
 * types (PostgreSQL, MySQL, MongoDB) with consistent interface.
 */
@Slf4j
@RequiredArgsConstructor
public class DatabaseManager {
    
    private final Map<String, DatabaseProvider> databaseProviders;

    /**
     * Provisions multiple databases based on specifications.
     */
    public List<DatabaseInstance> provisionDatabases(List<DatabaseSpec> specs, String environmentId) {
        if (specs == null || specs.isEmpty()) {
            log.info("No databases to provision for environment: {}", environmentId);
            return List.of();
        }
        
        log.info("Provisioning {} databases for environment: {}", specs.size(), environmentId);
        
        return specs.stream()
            .map(spec -> provisionDatabase(spec, environmentId))
            .collect(Collectors.toList());
    }

    /**
     * Provisions a single database instance.
     */
    private DatabaseInstance provisionDatabase(DatabaseSpec spec, String environmentId) {
        log.info("Provisioning database: {} (type: {})", spec.getName(), spec.getDatabaseType());
        
        DatabaseProvider provider = getDatabaseProvider(spec.getDatabaseType());
        
        // Create database instance
        DatabaseInstance instance = createDatabaseInstance(spec, environmentId);
        
        // Configure database
        configureDatabaseInstance(instance, spec);
        
        // Create databases and users
        createDatabasesAndUsers(instance, spec.getDatabases());
        
        // Load initial data if specified
        if (spec.getInitialDataScript() != null) {
            loadInitialData(instance, spec.getInitialDataScript());
        }
        
        log.info("Database provisioned successfully: {}", instance.getId());
        return instance;
    }

    private DatabaseProvider getDatabaseProvider(String databaseType) {
        DatabaseProvider provider = databaseProviders.get(databaseType.toLowerCase());
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }
        return provider;
    }

    private DatabaseInstance createDatabaseInstance(DatabaseSpec spec, String environmentId) {
        String instanceId = "db-" + spec.getName() + "-" + UUID.randomUUID().toString().substring(0, 8);
        
        // Generate connection info
        ConnectionInfo connectionInfo = ConnectionInfo.builder()
            .host(instanceId + ".example.com")
            .port(getDefaultPort(spec.getDatabaseType()))
            .username("admin")
            .password(generatePassword())
            .connectionString(buildConnectionString(spec, instanceId))
            .build();

        return DatabaseInstance.builder()
            .id(instanceId)
            .name(spec.getName())
            .databaseType(spec.getDatabaseType())
            .version(spec.getVersion())
            .endpoint(connectionInfo.getHost())
            .port(connectionInfo.getPort())
            .status("Available")
            .connectionInfo(connectionInfo)
            .build();
    }

    private void configureDatabaseInstance(DatabaseInstance instance, DatabaseSpec spec) {
        log.debug("Configuring database instance: {}", instance.getId());
        
        // Apply database-specific configurations
        if (spec.getConfiguration() != null) {
            spec.getConfiguration().forEach((key, value) -> {
                log.debug("Applying configuration: {} = {}", key, value);
            });
        }
    }

    private void createDatabasesAndUsers(DatabaseInstance instance, List<DatabaseConfig> databases) {
        if (databases == null || databases.isEmpty()) {
            return;
        }
        
        List<String> createdDatabases = databases.stream()
            .map(dbConfig -> {
                log.info("Creating database: {} on instance: {}", dbConfig.getName(), instance.getId());
                
                // Create users for this database
                if (dbConfig.getUsers() != null) {
                    dbConfig.getUsers().forEach(userConfig -> {
                        log.info("Creating user: {} for database: {}", 
                            userConfig.getUsername(), dbConfig.getName());
                        // In real implementation, would execute SQL commands
                    });
                }
                
                return dbConfig.getName();
            })
            .collect(Collectors.toList());
        
        instance.setDatabases(createdDatabases);
    }

    private void loadInitialData(DatabaseInstance instance, String initialDataScript) {
        log.info("Loading initial data for database: {} from script: {}", 
            instance.getId(), initialDataScript);
        // In real implementation, would execute the SQL script
    }

    private int getDefaultPort(String databaseType) {
        switch (databaseType.toLowerCase()) {
            case "postgresql": return 5432;
            case "mysql": return 3306;
            case "mongodb": return 27017;
            case "redis": return 6379;
            default: return 5432;
        }
    }

    private String generatePassword() {
        return "pwd-" + UUID.randomUUID().toString().substring(0, 12);
    }

    private String buildConnectionString(DatabaseSpec spec, String instanceId) {
        String host = instanceId + ".example.com";
        int port = getDefaultPort(spec.getDatabaseType());
        
        switch (spec.getDatabaseType().toLowerCase()) {
            case "postgresql":
                return String.format("postgresql://admin:%s@%s:%d/postgres", 
                    generatePassword(), host, port);
            case "mysql":
                return String.format("mysql://admin:%s@%s:%d/mysql", 
                    generatePassword(), host, port);
            default:
                return String.format("jdbc:%s://%s:%d", spec.getDatabaseType(), host, port);
        }
    }
}

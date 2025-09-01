# Practice Problem 8: Design a Test Configuration Management System

## Problem Statement
Design a comprehensive configuration management system for test automation that handles environment-specific settings, supports multiple configuration sources, provides runtime configuration updates, and ensures secure handling of sensitive data.

## Requirements

### Functional Requirements
1. **Multi-Source Configuration**: Support files, environment variables, system properties
2. **Environment Management**: Handle dev, test, staging, production configurations
3. **Runtime Updates**: Allow configuration changes without restart
4. **Hierarchical Configuration**: Support configuration inheritance and overrides
5. **Validation**: Validate configuration values and dependencies
6. **Encryption**: Secure handling of passwords and API keys
7. **Caching**: Efficient configuration caching with refresh capabilities

### Non-Functional Requirements
1. **Performance**: Sub-millisecond configuration retrieval
2. **Security**: Encrypted storage of sensitive configuration
3. **Reliability**: Fallback mechanisms for configuration failures
4. **Maintainability**: Easy to add new configuration properties

## Implementation Approach

### Configuration Manager
```java
@Component
public class TestConfigurationManager {
    private final Map<String, ConfigurationSource> sources;
    private final ConfigurationCache cache;
    private final EncryptionService encryptionService;
    
    public <T> T getProperty(String key, Class<T> type) {
        return getProperty(key, type, null);
    }
    
    public <T> T getProperty(String key, Class<T> type, T defaultValue) {
        String value = cache.get(key);
        
        if (value == null) {
            value = resolveProperty(key);
            if (value != null) {
                cache.put(key, value);
            }
        }
        
        return convertValue(value, type, defaultValue);
    }
    
    private String resolveProperty(String key) {
        for (ConfigurationSource source : sources.values()) {
            String value = source.getProperty(key);
            if (value != null) {
                return isEncrypted(value) ? encryptionService.decrypt(value) : value;
            }
        }
        return null;
    }
}
```

### Environment-Specific Configuration
```java
@Configuration
public class EnvironmentConfiguration {
    
    @Bean
    @Profile("test")
    public TestConfiguration testConfiguration() {
        return TestConfiguration.builder()
            .baseUrl("http://test.example.com")
            .databaseUrl("jdbc:h2:mem:testdb")
            .timeout(Duration.ofSeconds(30))
            .parallelThreads(4)
            .headlessMode(true)
            .build();
    }
    
    @Bean
    @Profile("staging")
    public TestConfiguration stagingConfiguration() {
        return TestConfiguration.builder()
            .baseUrl("https://staging.example.com")
            .databaseUrl("jdbc:postgresql://staging-db:5432/stagingdb")
            .timeout(Duration.ofSeconds(60))
            .parallelThreads(8)
            .headlessMode(false)
            .build();
    }
}
```

## Success Criteria
1. **Multi-Source Support**: Successfully load from all configuration sources
2. **Environment Switching**: Seamless environment-specific configuration
3. **Security**: Encrypted sensitive data with secure key management
4. **Performance**: Fast configuration retrieval with effective caching
5. **Validation**: Comprehensive validation with clear error messages

## Deliverables
1. `ConfigurationManager.java` - Main configuration manager
2. `ConfigurationSource.java` - Configuration source interface
3. `EncryptionService.java` - Configuration encryption utilities
4. `ConfigurationValidator.java` - Configuration validation
5. `ConfigurationTest.java` - Comprehensive tests

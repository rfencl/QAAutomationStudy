# Practice Problem 8: Configuration Management

## Problem Statement

Design and implement a comprehensive configuration management system for a QA automation framework that can handle multiple configuration sources, environment-specific settings, and runtime configuration updates.

## Requirements

### Core Functionality
1. **Multiple Configuration Sources**
   - Properties files (application.properties, environment-specific files)
   - YAML configuration files
   - System properties (-D parameters)
   - Environment variables
   - Runtime configuration updates

2. **Priority-Based Configuration Loading**
   - System properties should have highest priority
   - Environment variables should override file-based configuration
   - Environment-specific files should override base configuration
   - Provide sensible defaults for all configuration values

3. **Environment Management**
   - Support for multiple environments (DEV, QA, STAGING, PROD)
   - Automatic environment detection from system properties
   - Environment-specific configuration loading
   - Environment-aware utility methods

4. **Thread Safety**
   - Singleton configuration manager
   - Thread-safe access to configuration values
   - Concurrent caching mechanism
   - No shared mutable state issues

5. **Type Safety**
   - Support for String, int, boolean configuration values
   - Proper type conversion with error handling
   - Default value support for missing configurations
   - Validation of configuration values

### Technical Requirements

1. **Configuration Manager**
   - Implement singleton pattern for global configuration access
   - Provide methods for getting configuration values with defaults
   - Support runtime configuration updates
   - Implement configuration caching for performance

2. **Environment Abstraction**
   - Create enum for supported environments
   - Implement environment detection logic
   - Provide environment-specific configuration loading
   - Support case-insensitive environment specification

3. **Utility Layer**
   - Create convenience methods for common configuration access
   - Implement type-safe getters for different data types
   - Provide environment-specific utility methods
   - Support configuration validation

4. **Testing**
   - Unit tests for configuration loading and priority
   - Tests for environment detection and switching
   - Tests for type conversion and default values
   - Tests for thread safety and concurrent access

### Configuration Structure

```
config/
├── application.properties          # Base configuration
├── application-dev.properties      # Development overrides
├── application-qa.properties       # QA environment overrides
├── application-staging.properties  # Staging environment overrides
├── application-prod.properties     # Production environment overrides
└── config.yaml                    # YAML-based configuration
```

### Expected Configuration Properties

```properties
# Browser Configuration
browser.type=chrome
browser.headless=true
browser.timeout=10

# Application Configuration
app.url=https://example.com
app.name=Test Application

# Database Configuration
database.url=jdbc:h2:mem:testdb
database.username=sa
database.password=

# Test Configuration
test.retry.count=2
test.parallel.threads=4
```

## Implementation Guidelines

### 1. Configuration Manager Design
```java
public class ConfigurationManager {
    // Singleton implementation
    // Configuration caching
    // Multiple source loading
    // Priority-based resolution
    // Thread-safe operations
}
```

### 2. Environment Management
```java
public enum Environment {
    DEV, QA, STAGING, PROD;
    // Environment detection
    // Case-insensitive parsing
    // Default environment handling
}
```

### 3. Utility Layer
```java
public class TestConfig {
    // Static convenience methods
    // Type-safe getters
    // Environment-specific utilities
    // Configuration validation
}
```

### 4. Configuration Priority Order
1. System Properties (-Dproperty=value)
2. Environment Variables (PROPERTY_NAME=value)
3. Environment-specific properties files
4. Base properties file
5. YAML configuration
6. Default values

## Success Criteria

1. **Functionality**
   - ✅ All configuration sources are properly loaded and merged
   - ✅ Priority order is correctly implemented and tested
   - ✅ Environment detection works automatically
   - ✅ Type conversion handles all supported types safely
   - ✅ Runtime configuration updates work correctly

2. **Design Quality**
   - ✅ Singleton pattern is properly implemented
   - ✅ Thread safety is ensured for concurrent access
   - ✅ Configuration caching improves performance
   - ✅ Clean separation between manager and utility layers
   - ✅ Proper error handling for missing/invalid configurations

3. **Testing**
   - ✅ Comprehensive unit tests cover all functionality
   - ✅ Tests validate configuration priority and merging
   - ✅ Environment switching is properly tested
   - ✅ Type conversion edge cases are covered
   - ✅ Thread safety is validated through concurrent tests

4. **Documentation**
   - ✅ Clear README with usage examples
   - ✅ Configuration file examples for all environments
   - ✅ Class and sequence diagrams showing architecture
   - ✅ Integration examples with test frameworks

## Bonus Features

1. **Configuration Validation**
   - Validate required configuration properties
   - Implement configuration schema validation
   - Provide meaningful error messages for invalid configurations

2. **Configuration Monitoring**
   - Track configuration access patterns
   - Implement configuration change notifications
   - Provide configuration usage statistics

3. **Advanced Features**
   - Support for encrypted configuration values
   - Configuration hot-reloading without restart
   - Integration with external configuration services

## Real-World Applications

This configuration management system can be used for:

1. **Test Framework Configuration**
   - Browser settings and capabilities
   - Test environment URLs and credentials
   - Test execution parameters and timeouts

2. **CI/CD Integration**
   - Environment-specific deployment configurations
   - Build pipeline parameters
   - Integration with configuration management tools

3. **Multi-Environment Testing**
   - Seamless switching between test environments
   - Environment-specific test data and settings
   - Consistent configuration across team members

4. **Production Deployment**
   - Secure handling of production credentials
   - Environment-specific feature flags
   - Performance and monitoring configurations

This problem tests understanding of design patterns, configuration management, environment abstraction, thread safety, and comprehensive testing strategies essential for robust QA automation frameworks.

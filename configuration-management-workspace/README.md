# Configuration Management Workspace

This workspace demonstrates a comprehensive configuration management system for QA automation frameworks, handling multiple configuration sources with proper priority ordering and environment-specific settings.

## 🎯 Key Features

- **Multiple Configuration Sources**: Properties files, YAML files, system properties, environment variables
- **Environment-Specific Configuration**: DEV, QA, STAGING, PROD environments
- **Priority-Based Loading**: System properties > Environment variables > Config files > Defaults
- **Thread-Safe Singleton**: Concurrent access with caching mechanism
- **Type-Safe Configuration**: String, int, boolean conversion with defaults
- **Runtime Configuration**: Dynamic property setting and cache management

## 📁 Project Structure

```
configuration-management-workspace/
├── src/main/java/com/qa/config/
│   ├── ConfigurationManager.java    # Main singleton configuration manager
│   ├── ConfigData.java             # YAML configuration data classes
│   ├── Environment.java            # Environment enumeration
│   └── TestConfig.java             # Static utility wrapper
├── src/main/resources/config/
│   ├── application.properties      # Base configuration
│   ├── application-dev.properties  # Development environment
│   ├── application-qa.properties   # QA environment
│   ├── application-prod.properties # Production environment
│   └── config.yaml                # YAML configuration
├── src/test/java/com/qa/tests/
│   ├── ConfigurationManagerTest.java
│   ├── TestConfigTest.java
│   └── EnvironmentTest.java
└── docs/
    ├── class-diagram.puml
    └── sequence-diagram.puml
```

## 🚀 Quick Start

### 1. Run Tests
```bash
cd configuration-management-workspace
mvn clean test
```

### 2. Environment-Specific Execution
```bash
# Run with QA environment
mvn test -Denv=qa

# Run with production environment  
mvn test -Denv=prod

# Override specific properties
mvn test -Dbrowser.type=firefox -Dbrowser.headless=false
```

### 3. Basic Usage
```java
// Using TestConfig utility class
String browserType = TestConfig.getBrowserType();
boolean headless = TestConfig.isHeadless();
String appUrl = TestConfig.getAppUrl();

// Using ConfigurationManager directly
ConfigurationManager config = ConfigurationManager.getInstance();
String value = config.getString("custom.property", "default");
config.setProperty("runtime.property", "value");
```

## 🔧 Configuration Priority

The system follows this priority order (highest to lowest):

1. **System Properties** (`-Dproperty=value`)
2. **Environment Variables** (`PROPERTY_NAME=value`)
3. **Environment-Specific Properties** (`application-{env}.properties`)
4. **Base Properties** (`application.properties`)
5. **YAML Configuration** (`config.yaml`)
6. **Default Values** (hardcoded fallbacks)

## 📊 Configuration Sources

### Properties Files
- `application.properties` - Base configuration for all environments
- `application-{env}.properties` - Environment-specific overrides
- Supports standard Java properties format

### YAML Configuration
- `config.yaml` - Structured configuration with nested objects
- Supports complex data structures and type safety
- Automatically mapped to Java objects

### System Properties
```bash
# Override any configuration at runtime
java -Dbrowser.type=firefox -Dapp.url=https://custom.url.com MyTest
```

### Environment Variables
```bash
# Environment variables with underscore notation
export BROWSER_TYPE=chrome
export APP_URL=https://staging.example.com
```

## 🏗️ Architecture

### ConfigurationManager (Singleton)
- Thread-safe singleton implementation
- Concurrent caching with `ConcurrentHashMap`
- Automatic configuration loading and merging
- Support for runtime property updates

### Environment Enum
- Type-safe environment representation
- Automatic environment detection from system properties
- Default fallback to DEV environment

### TestConfig (Utility)
- Static convenience methods for common configurations
- Type-safe getters with sensible defaults
- Environment-specific utility methods

## 🧪 Test Coverage

### ConfigurationManagerTest
- Singleton pattern validation
- Configuration loading and priority testing
- System property and environment variable overrides
- Runtime property management
- Type conversion testing

### TestConfigTest
- Utility method validation
- Configuration consistency testing
- Environment-specific behavior
- Default value handling

### EnvironmentTest
- Environment enumeration testing
- String-to-enum conversion
- Default value behavior
- Case-insensitive parsing

## 📈 Usage Examples

### Basic Configuration Access
```java
// Get browser configuration
String browser = TestConfig.getBrowserType();        // "chrome"
boolean headless = TestConfig.isHeadless();          // true
int timeout = TestConfig.getBrowserTimeout();        // 10

// Get application configuration  
String appUrl = TestConfig.getAppUrl();              // "https://..."
String appName = TestConfig.getAppName();            // "Test Application"

// Get database configuration
String dbUrl = TestConfig.getDatabaseUrl();          // "jdbc:h2:mem:testdb"
String dbUser = TestConfig.getDatabaseUsername();    // "sa"
```

### Environment-Specific Logic
```java
if (TestConfig.isDevEnvironment()) {
    // Development-specific setup
    System.out.println("Running in DEV mode");
} else if (TestConfig.isProdEnvironment()) {
    // Production-specific setup
    System.out.println("Running in PROD mode");
}

Environment env = TestConfig.getEnvironment();
switch (env) {
    case DEV:
        // Dev configuration
        break;
    case QA:
        // QA configuration
        break;
    case PROD:
        // Production configuration
        break;
}
```

### Runtime Configuration Updates
```java
ConfigurationManager config = ConfigurationManager.getInstance();

// Set runtime properties
config.setProperty("test.run.id", UUID.randomUUID().toString());
config.setProperty("test.start.time", System.currentTimeMillis());

// Clear cache and reload
config.clearCache();
```

### Custom Configuration Properties
```java
// Add custom properties to application.properties
// custom.api.endpoint=https://api.example.com
// custom.retry.attempts=3

// Access in code
String apiEndpoint = config.getString("custom.api.endpoint");
int retryAttempts = config.getInt("custom.retry.attempts", 1);
```

## 🔍 Advanced Features

### Thread Safety
- Singleton implementation with double-checked locking
- Concurrent collections for thread-safe caching
- No shared mutable state between configuration accesses

### Performance Optimization
- Configuration caching to avoid repeated file I/O
- Lazy loading of configuration sources
- Efficient priority-based property resolution

### Extensibility
- Easy addition of new configuration sources
- Support for custom property converters
- Pluggable environment detection logic

## 🎓 Learning Objectives

This workspace demonstrates:

1. **Singleton Pattern** - Thread-safe singleton implementation
2. **Configuration Management** - Multiple source handling with priorities
3. **Environment Abstraction** - Environment-specific configuration loading
4. **Type Safety** - Proper type conversion with error handling
5. **Caching Strategy** - Performance optimization through intelligent caching
6. **Resource Management** - Proper handling of configuration files and streams
7. **Testing Strategy** - Comprehensive testing of configuration behavior

## 🚀 Integration with Test Frameworks

### TestNG Integration
```java
@BeforeClass
public void setUpClass() {
    // Configuration is automatically loaded
    String browser = TestConfig.getBrowserType();
    WebDriver driver = createDriver(browser);
}
```

### Selenium Integration
```java
public WebDriver createDriver() {
    String browserType = TestConfig.getBrowserType();
    boolean headless = TestConfig.isHeadless();
    
    ChromeOptions options = new ChromeOptions();
    if (headless) {
        options.addArguments("--headless");
    }
    
    return new ChromeDriver(options);
}
```

### Database Integration
```java
public Connection getConnection() throws SQLException {
    String url = TestConfig.getDatabaseUrl();
    String username = TestConfig.getDatabaseUsername();
    String password = TestConfig.getDatabasePassword();
    
    return DriverManager.getConnection(url, username, password);
}
```

This configuration management system provides a robust foundation for any QA automation framework, ensuring consistent and flexible configuration handling across different environments and execution contexts.

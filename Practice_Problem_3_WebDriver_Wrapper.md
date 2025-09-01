# Practice Problem 3: Create a Custom WebDriver Wrapper with Enhanced Logging

## Problem Statement
Design a custom WebDriver wrapper that enhances the standard Selenium WebDriver with comprehensive logging, automatic screenshot capture, performance monitoring, and improved error handling for better test debugging and maintenance.

## Requirements

### Functional Requirements
1. **Enhanced Logging**: Log all WebDriver actions with timestamps
2. **Screenshot Capture**: Automatic screenshots on failures and key actions
3. **Performance Monitoring**: Track page load times and element interaction times
4. **Smart Waits**: Intelligent waiting strategies with detailed logging
5. **Error Enhancement**: Enrich exceptions with context and screenshots
6. **Action Replay**: Ability to replay failed actions for debugging
7. **Element Highlighting**: Visual highlighting of interacted elements

### Non-Functional Requirements
1. **Transparency**: Maintain WebDriver interface compatibility
2. **Performance**: Minimal overhead on test execution
3. **Thread Safety**: Support parallel test execution
4. **Configurability**: Enable/disable features via configuration

## Technical Specifications

### Core Components
1. **EnhancedWebDriver**: Main wrapper implementing WebDriver interface
2. **ActionLogger**: Handles detailed action logging
3. **ScreenshotManager**: Manages screenshot capture and storage
4. **PerformanceMonitor**: Tracks timing metrics
5. **ElementHighlighter**: Visual element highlighting utilities
6. **ConfigurationManager**: Manages wrapper settings

### Wrapper Architecture
```java
public class EnhancedWebDriver implements WebDriver {
    private final WebDriver delegate;
    private final ActionLogger logger;
    private final ScreenshotManager screenshotManager;
    private final PerformanceMonitor performanceMonitor;
    private final WrapperConfiguration config;
}
```

## Implementation Approach

### Step 1: Basic Wrapper Structure
```java
public class EnhancedWebDriver implements WebDriver {
    private final WebDriver delegate;
    private final ActionLogger logger;
    
    public EnhancedWebDriver(WebDriver driver) {
        this.delegate = driver;
        this.logger = new ActionLogger();
    }
    
    @Override
    public void get(String url) {
        long startTime = System.currentTimeMillis();
        logger.logAction("Navigating to URL: " + url);
        
        try {
            delegate.get(url);
            long loadTime = System.currentTimeMillis() - startTime;
            logger.logSuccess("Page loaded successfully in " + loadTime + "ms");
        } catch (Exception e) {
            screenshotManager.captureScreenshot("navigation_failure");
            logger.logError("Navigation failed", e);
            throw new EnhancedWebDriverException("Failed to navigate to " + url, e);
        }
    }
}
```

### Step 2: Enhanced Element Wrapper
```java
public class EnhancedWebElement implements WebElement {
    private final WebElement delegate;
    private final ActionLogger logger;
    private final ScreenshotManager screenshotManager;
    
    @Override
    public void click() {
        logger.logAction("Clicking element: " + getElementDescription());
        highlightElement();
        
        try {
            long startTime = System.currentTimeMillis();
            delegate.click();
            long clickTime = System.currentTimeMillis() - startTime;
            logger.logSuccess("Element clicked successfully in " + clickTime + "ms");
        } catch (Exception e) {
            screenshotManager.captureScreenshot("click_failure");
            logger.logError("Click failed on element: " + getElementDescription(), e);
            throw new EnhancedElementException("Failed to click element", e, this);
        }
    }
    
    private void highlightElement() {
        if (config.isHighlightingEnabled()) {
            ElementHighlighter.highlight(delegate);
        }
    }
}
```

### Step 3: Smart Wait Implementation
```java
public class EnhancedWebDriverWait extends WebDriverWait {
    private final ActionLogger logger;
    
    public EnhancedWebDriverWait(WebDriver driver, Duration timeout) {
        super(driver, timeout);
        this.logger = new ActionLogger();
    }
    
    @Override
    public <V> V until(Function<? super WebDriver, V> isTrue) {
        logger.logAction("Starting wait condition with timeout: " + getTimeout());
        long startTime = System.currentTimeMillis();
        
        try {
            V result = super.until(isTrue);
            long waitTime = System.currentTimeMillis() - startTime;
            logger.logSuccess("Wait condition satisfied in " + waitTime + "ms");
            return result;
        } catch (TimeoutException e) {
            long waitTime = System.currentTimeMillis() - startTime;
            screenshotManager.captureScreenshot("wait_timeout");
            logger.logError("Wait condition timed out after " + waitTime + "ms", e);
            throw new EnhancedTimeoutException("Wait condition failed", e);
        }
    }
}
```

## Example Usage Scenarios

### Scenario 1: Basic Navigation with Logging
```java
@Test
public void testNavigationWithLogging() {
    EnhancedWebDriver driver = new EnhancedWebDriver(new ChromeDriver());
    
    // This will log: "Navigating to URL: https://example.com"
    driver.get("https://example.com");
    
    // This will log: "Page loaded successfully in 1250ms"
    // Screenshot automatically captured if navigation fails
}
```

### Scenario 2: Element Interaction with Highlighting
```java
@Test
public void testElementInteractionWithHighlighting() {
    EnhancedWebDriver driver = new EnhancedWebDriver(new ChromeDriver());
    driver.get("https://example.com/login");
    
    // Element will be highlighted before interaction
    EnhancedWebElement loginButton = driver.findElement(By.id("login-btn"));
    loginButton.click(); // Logs click action and timing
}
```

### Scenario 3: Performance Monitoring
```java
@Test
public void testPerformanceMonitoring() {
    EnhancedWebDriver driver = new EnhancedWebDriver(new ChromeDriver());
    
    // Performance metrics automatically collected
    driver.get("https://example.com");
    
    PerformanceReport report = driver.getPerformanceReport();
    Assert.assertTrue(report.getPageLoadTime() < 3000, 
        "Page load time exceeded threshold: " + report.getPageLoadTime());
}
```

### Scenario 4: Enhanced Error Handling
```java
@Test
public void testEnhancedErrorHandling() {
    EnhancedWebDriver driver = new EnhancedWebDriver(new ChromeDriver());
    
    try {
        driver.findElement(By.id("non-existent")).click();
    } catch (EnhancedElementException e) {
        // Exception includes screenshot path and detailed context
        System.out.println("Screenshot saved at: " + e.getScreenshotPath());
        System.out.println("Element selector: " + e.getElementSelector());
        System.out.println("Page URL: " + e.getPageUrl());
    }
}
```

## Advanced Features

### Configuration Management
```java
public class WrapperConfiguration {
    private boolean loggingEnabled = true;
    private boolean screenshotOnFailure = true;
    private boolean screenshotOnAction = false;
    private boolean performanceMonitoring = true;
    private boolean elementHighlighting = false;
    private Duration defaultTimeout = Duration.ofSeconds(10);
    private String screenshotDirectory = "screenshots";
    private LogLevel logLevel = LogLevel.INFO;
}
```

### Action Replay System
```java
public class ActionRecorder {
    private List<RecordedAction> actions = new ArrayList<>();
    
    public void recordAction(String action, Map<String, Object> parameters) {
        actions.add(new RecordedAction(action, parameters, System.currentTimeMillis()));
    }
    
    public void replayActions(WebDriver driver) {
        for (RecordedAction action : actions) {
            executeAction(driver, action);
        }
    }
}
```

### Performance Analytics
```java
public class PerformanceReport {
    private long pageLoadTime;
    private Map<String, Long> elementInteractionTimes;
    private List<PerformanceMetric> metrics;
    
    public void generateReport() {
        // Generate detailed performance report
    }
    
    public boolean hasPerformanceIssues() {
        return pageLoadTime > 5000 || 
               elementInteractionTimes.values().stream()
                   .anyMatch(time -> time > 1000);
    }
}
```

## Integration Examples

### TestNG Integration
```java
public class BaseTest {
    protected EnhancedWebDriver driver;
    
    @BeforeMethod
    public void setUp() {
        WrapperConfiguration config = WrapperConfiguration.builder()
            .enableScreenshotOnFailure(true)
            .enablePerformanceMonitoring(true)
            .setLogLevel(LogLevel.DEBUG)
            .build();
            
        driver = new EnhancedWebDriver(new ChromeDriver(), config);
    }
    
    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            String screenshotPath = driver.captureScreenshot(result.getName());
            System.setProperty("screenshot.path", screenshotPath);
        }
        
        PerformanceReport report = driver.getPerformanceReport();
        report.saveToFile("performance_" + result.getName() + ".json");
        
        driver.quit();
    }
}
```

### Page Object Integration
```java
public class LoginPage {
    private EnhancedWebDriver driver;
    
    @FindBy(id = "username")
    private EnhancedWebElement usernameField;
    
    public void login(String username, String password) {
        // All interactions automatically logged and monitored
        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        loginButton.click();
    }
}
```

## Success Criteria
1. All WebDriver actions are comprehensively logged
2. Screenshots captured automatically on failures
3. Performance metrics collected for all interactions
4. Enhanced exceptions provide debugging context
5. Minimal impact on test execution performance
6. Easy integration with existing test frameworks
7. Configurable features for different environments

## Extension Points
1. **Custom Loggers**: Integration with different logging frameworks
2. **Cloud Storage**: Upload screenshots to cloud storage
3. **Real-time Monitoring**: Live dashboard for test execution
4. **AI Analysis**: Automatic failure pattern detection
5. **Video Recording**: Capture video of test execution

## Testing Strategy
1. **Unit Tests**: Test individual wrapper components
2. **Integration Tests**: Verify WebDriver compatibility
3. **Performance Tests**: Measure wrapper overhead
4. **Reliability Tests**: Test with various failure scenarios

## Deliverables
1. `EnhancedWebDriver.java` - Main wrapper class
2. `EnhancedWebElement.java` - Element wrapper
3. `ActionLogger.java` - Logging implementation
4. `ScreenshotManager.java` - Screenshot utilities
5. `PerformanceMonitor.java` - Performance tracking
6. `WrapperConfiguration.java` - Configuration management
7. `EnhancedWebDriverTest.java` - Comprehensive tests
8. `README.md` - Usage guide and configuration options

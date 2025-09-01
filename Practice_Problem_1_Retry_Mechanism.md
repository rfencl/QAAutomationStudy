# Practice Problem 1: Implement a Retry Mechanism for Flaky Tests

## Problem Statement
Design and implement a robust retry mechanism for handling flaky tests in a Selenium automation framework. The solution should be configurable, provide detailed logging, and integrate seamlessly with TestNG.

## Requirements

### Functional Requirements
1. **Configurable Retry Count**: Allow setting maximum retry attempts per test
2. **Conditional Retry**: Only retry on specific exceptions (not assertion failures)
3. **Delay Between Retries**: Implement exponential backoff or fixed delay
4. **Detailed Logging**: Log each retry attempt with failure reason
5. **TestNG Integration**: Work with TestNG's IRetryAnalyzer interface
6. **Thread Safety**: Support parallel test execution

### Non-Functional Requirements
1. **Performance**: Minimal overhead when tests pass on first attempt
2. **Maintainability**: Easy to configure and extend
3. **Observability**: Clear reporting of retry statistics

## Technical Specifications

### Core Components
1. **RetryAnalyzer**: Implements IRetryAnalyzer interface
2. **RetryConfiguration**: Manages retry settings
3. **RetryLogger**: Handles retry-specific logging
4. **RetryException**: Custom exception for retry logic

### Configuration Options
```java
public class RetryConfig {
    private int maxRetries = 3;
    private long delayMs = 1000;
    private boolean exponentialBackoff = true;
    private Set<Class<? extends Throwable>> retryableExceptions;
}
```

## Implementation Approach

### Step 1: Basic Retry Analyzer
```java
public class TestRetryAnalyzer implements IRetryAnalyzer {
    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = 3;
    
    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY_COUNT) {
            if (isRetryableException(result.getThrowable())) {
                retryCount++;
                logRetryAttempt(result);
                return true;
            }
        }
        return false;
    }
}
```

### Step 2: Enhanced Configuration
```java
@Test(retryAnalyzer = TestRetryAnalyzer.class)
public void flakySeleniumTest() {
    // Test implementation
}
```

### Step 3: Integration Points
- TestNG listener for retry statistics
- Screenshot capture on retry
- Test result modification for reporting

## Example Scenarios

### Scenario 1: Network Timeout
```java
// Should retry on WebDriverException
@Test(retryAnalyzer = TestRetryAnalyzer.class)
public void testWithNetworkIssues() {
    driver.get("https://example.com");
    // May fail due to network timeout
}
```

### Scenario 2: Element Not Found
```java
// Should retry on NoSuchElementException
@Test(retryAnalyzer = TestRetryAnalyzer.class)
public void testDynamicElement() {
    WebElement element = driver.findElement(By.id("dynamic-element"));
    element.click();
}
```

### Scenario 3: Assertion Failure
```java
// Should NOT retry on AssertionError
@Test(retryAnalyzer = TestRetryAnalyzer.class)
public void testBusinessLogic() {
    String result = performBusinessOperation();
    Assert.assertEquals(result, "expected"); // Don't retry if this fails
}
```

## Success Criteria
1. Tests retry only on specified exceptions
2. Maximum retry count is respected
3. Detailed logs show retry attempts and reasons
4. No impact on test execution time for passing tests
5. Thread-safe operation in parallel execution
6. Integration with existing TestNG reporting

## Extension Points
1. **Custom Retry Strategies**: Different retry logic per test type
2. **Metrics Collection**: Retry statistics for analysis
3. **External Configuration**: Database or file-based retry settings
4. **Notification Integration**: Alert on excessive retries

## Testing Strategy
1. **Unit Tests**: Test retry logic with mock exceptions
2. **Integration Tests**: Verify TestNG integration
3. **Performance Tests**: Measure overhead of retry mechanism
4. **Concurrency Tests**: Validate thread safety

## Deliverables
1. `TestRetryAnalyzer.java` - Main retry implementation
2. `RetryConfiguration.java` - Configuration management
3. `RetryLogger.java` - Logging utilities
4. `RetryAnalyzerTest.java` - Unit tests
5. `README.md` - Usage documentation and examples

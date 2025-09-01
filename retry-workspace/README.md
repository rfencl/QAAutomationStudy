# Retry Workspace - Test Retry Mechanism Implementation

This workspace implements a robust retry mechanism for handling flaky tests in Selenium automation, as outlined in Practice_Problem_1_Retry_Mechanism.md.

## 🎯 Features Implemented

### Core Components
- **TestRetryAnalyzer** - Main retry logic implementing IRetryAnalyzer
- **RetryConfig** - Configurable retry settings
- **RetryListener** - TestNG listener for retry statistics and screenshots

### Key Features
- ✅ **Configurable Retry Count** - Set maximum retry attempts per test
- ✅ **Conditional Retry** - Only retry on specific exceptions (WebDriverException, NoSuchElementException, TimeoutException)
- ✅ **Exponential Backoff** - Configurable delay between retries
- ✅ **Thread Safety** - ThreadLocal pattern for parallel execution
- ✅ **Detailed Logging** - Log each retry attempt with failure reason
- ✅ **TestNG Integration** - Seamless integration with TestNG framework

## 🚀 Quick Start

### Run All Tests
```bash
cd retry-workspace
mvn clean test
```

### Run Specific Test Class
```bash
mvn test -Dtest=RetryTestExample
```

## 📋 Test Scenarios

### RetryTestExample.java
1. **testNetworkTimeout()** - Simulates network timeout, retries 2 times, passes on 3rd attempt
2. **testElementNotFound()** - Simulates element not found, retries 1 time, passes on 2nd attempt  
3. **testAssertionFailure()** - AssertionError should NOT retry (not in retryable exceptions)
4. **testSuccessfulTest()** - Passes on first attempt (no retry needed)

### RetryAnalyzerTest.java
- Unit tests for retry logic validation
- Tests retryable vs non-retryable exceptions
- Validates max retry limits

## ⚙️ Configuration

### Default Configuration (RetryConfig.java)
```java
private int maxRetries = 3;
private long delayMs = 1000;
private boolean exponentialBackoff = true;
private Set<Class<? extends Throwable>> retryableExceptions = Set.of(
    WebDriverException.class,
    NoSuchElementException.class,
    TimeoutException.class
);
```

### Custom Configuration Example
```java
RetryConfig customConfig = new RetryConfig();
customConfig.setMaxRetries(5);
customConfig.setDelayMs(2000);
customConfig.setExponentialBackoff(false);

TestRetryAnalyzer analyzer = new TestRetryAnalyzer(customConfig);
```

## 📊 Expected Output

When running tests, you'll see retry attempts logged:
```
[RETRY] Attempt 1/3 for test 'testNetworkTimeout' - Reason: Simulated network timeout
[RETRY] Attempt 2/3 for test 'testNetworkTimeout' - Reason: Simulated network timeout
[RETRY SUCCESS] Test 'testNetworkTimeout' passed after 3 attempts
```

## 🔧 Integration with Existing Tests

Add retry analyzer to any test method:
```java
@Test(retryAnalyzer = TestRetryAnalyzer.class)
public void yourFlakyTest() {
    // Your test logic here
}
```

## 📈 Success Criteria Met

- ✅ Tests retry only on specified exceptions
- ✅ Maximum retry count is respected  
- ✅ Detailed logs show retry attempts and reasons
- ✅ No impact on test execution time for passing tests
- ✅ Thread-safe operation for parallel execution
- ✅ Integration with TestNG reporting

## 🎓 Learning Outcomes

This implementation demonstrates:
- **IRetryAnalyzer Interface** - TestNG's built-in retry mechanism
- **ThreadLocal Pattern** - Thread-safe retry counting
- **Exception Handling** - Conditional retry based on exception types
- **Configuration Management** - Flexible retry configuration
- **Test Listeners** - Enhanced reporting and screenshot capture

## 🔄 Extension Points

- Add custom retry strategies per test type
- Implement retry metrics collection
- Add external configuration (database/file-based)
- Integrate with notification systems for excessive retries

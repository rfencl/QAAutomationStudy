# Practice Problem 4: Implement Parallel Test Execution with Proper Resource Management

## Problem Statement
Design and implement a robust parallel test execution system that efficiently manages WebDriver instances, database connections, and other shared resources while ensuring thread safety and optimal performance across multiple concurrent test threads.

## Requirements

### Functional Requirements
1. **Thread-Safe WebDriver Management**: Isolated WebDriver instances per thread
2. **Resource Pool Management**: Efficient allocation and cleanup of resources
3. **Dynamic Thread Scaling**: Adjust thread count based on system resources
4. **Cross-Browser Parallel Execution**: Support multiple browsers simultaneously
5. **Resource Monitoring**: Track resource usage and performance metrics
6. **Graceful Failure Handling**: Isolate failures to prevent cascade effects
7. **Load Balancing**: Distribute tests evenly across available threads

### Non-Functional Requirements
1. **Performance**: Optimal resource utilization without over-allocation
2. **Reliability**: Robust cleanup to prevent resource leaks
3. **Scalability**: Support scaling from 1 to 50+ parallel threads
4. **Observability**: Comprehensive monitoring and reporting

## Technical Specifications

### Core Components
1. **ThreadLocalResourceManager**: Manages thread-local resources
2. **ResourcePool**: Handles shared resource allocation
3. **ParallelExecutionCoordinator**: Orchestrates parallel execution
4. **ResourceMonitor**: Tracks resource usage and performance
5. **CleanupManager**: Ensures proper resource cleanup
6. **LoadBalancer**: Distributes tests across threads

### Architecture Overview
```java
public class ParallelTestExecutor {
    private final ThreadLocalResourceManager resourceManager;
    private final ResourcePool resourcePool;
    private final ParallelExecutionCoordinator coordinator;
    private final ResourceMonitor monitor;
    private final ExecutorService executorService;
}
```

## Implementation Approach

### Step 1: ThreadLocal Resource Management
```java
public class ThreadLocalResourceManager {
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Connection> dbConnectionThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> threadIdThreadLocal = new ThreadLocal<>();
    
    public static void initializeResources(String browser, String threadId) {
        threadIdThreadLocal.set(threadId);
        
        // Initialize WebDriver
        WebDriver driver = createWebDriver(browser);
        driverThreadLocal.set(driver);
        
        // Initialize database connection
        Connection connection = createDatabaseConnection();
        dbConnectionThreadLocal.set(connection);
        
        logResourceInitialization(threadId, browser);
    }
    
    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver not initialized for thread: " 
                + Thread.currentThread().getName());
        }
        return driver;
    }
    
    public static void cleanupResources() {
        String threadId = threadIdThreadLocal.get();
        
        // Cleanup WebDriver
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                logCleanupError("WebDriver cleanup failed for thread: " + threadId, e);
            } finally {
                driverThreadLocal.remove();
            }
        }
        
        // Cleanup database connection
        Connection connection = dbConnectionThreadLocal.get();
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logCleanupError("Database connection cleanup failed for thread: " + threadId, e);
            } finally {
                dbConnectionThreadLocal.remove();
            }
        }
        
        threadIdThreadLocal.remove();
    }
}
```

### Step 2: Resource Pool Implementation
```java
public class ResourcePool {
    private final BlockingQueue<WebDriver> availableDrivers;
    private final Map<String, Queue<Connection>> dbConnectionPools;
    private final AtomicInteger activeDrivers = new AtomicInteger(0);
    private final int maxPoolSize;
    
    public ResourcePool(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        this.availableDrivers = new LinkedBlockingQueue<>(maxPoolSize);
        this.dbConnectionPools = new ConcurrentHashMap<>();
        initializePools();
    }
    
    public WebDriver acquireDriver(String browser) throws InterruptedException {
        WebDriver driver = availableDrivers.poll(30, TimeUnit.SECONDS);
        if (driver == null) {
            if (activeDrivers.get() < maxPoolSize) {
                driver = createNewDriver(browser);
                activeDrivers.incrementAndGet();
            } else {
                throw new ResourceExhaustedException("No WebDriver available in pool");
            }
        }
        return driver;
    }
    
    public void releaseDriver(WebDriver driver) {
        if (driver != null) {
            try {
                // Reset driver state
                driver.manage().deleteAllCookies();
                driver.get("about:blank");
                availableDrivers.offer(driver);
            } catch (Exception e) {
                // Driver is corrupted, create new one
                driver.quit();
                activeDrivers.decrementAndGet();
            }
        }
    }
}
```

### Step 3: Parallel Execution Coordinator
```java
public class ParallelExecutionCoordinator {
    private final ResourcePool resourcePool;
    private final ExecutorService executorService;
    private final ResourceMonitor monitor;
    
    public CompletableFuture<TestResult> executeTest(TestMethod testMethod, String browser) {
        return CompletableFuture.supplyAsync(() -> {
            String threadId = "Thread-" + Thread.currentThread().getId();
            
            try {
                // Initialize thread-local resources
                ThreadLocalResourceManager.initializeResources(browser, threadId);
                monitor.recordThreadStart(threadId);
                
                // Execute test
                TestResult result = executeTestMethod(testMethod);
                monitor.recordTestCompletion(threadId, result);
                
                return result;
                
            } catch (Exception e) {
                monitor.recordTestFailure(threadId, e);
                return TestResult.failure(testMethod.getName(), e);
            } finally {
                // Cleanup resources
                ThreadLocalResourceManager.cleanupResources();
                monitor.recordThreadEnd(threadId);
            }
        }, executorService);
    }
    
    public List<TestResult> executeTestSuite(List<TestMethod> tests, int threadCount) {
        List<CompletableFuture<TestResult>> futures = tests.stream()
            .map(test -> executeTest(test, selectBrowser(test)))
            .collect(Collectors.toList());
        
        return futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
    }
}
```

### Step 4: Resource Monitoring
```java
public class ResourceMonitor {
    private final Map<String, ThreadMetrics> threadMetrics = new ConcurrentHashMap<>();
    private final AtomicLong totalTestsExecuted = new AtomicLong(0);
    private final AtomicLong totalFailures = new AtomicLong(0);
    
    public void recordThreadStart(String threadId) {
        ThreadMetrics metrics = new ThreadMetrics();
        metrics.startTime = System.currentTimeMillis();
        metrics.threadId = threadId;
        threadMetrics.put(threadId, metrics);
    }
    
    public void recordTestCompletion(String threadId, TestResult result) {
        ThreadMetrics metrics = threadMetrics.get(threadId);
        if (metrics != null) {
            metrics.testsExecuted++;
            metrics.totalExecutionTime += result.getExecutionTime();
            totalTestsExecuted.incrementAndGet();
        }
    }
    
    public ExecutionReport generateReport() {
        return ExecutionReport.builder()
            .totalTests(totalTestsExecuted.get())
            .totalFailures(totalFailures.get())
            .threadMetrics(new HashMap<>(threadMetrics))
            .averageExecutionTime(calculateAverageExecutionTime())
            .resourceUtilization(calculateResourceUtilization())
            .build();
    }
}
```

## Example Usage Scenarios

### Scenario 1: Basic Parallel Execution
```java
@Test
public void testParallelExecution() {
    ParallelTestExecutor executor = new ParallelTestExecutor(
        ParallelConfig.builder()
            .threadCount(4)
            .browsers(Arrays.asList("chrome", "firefox"))
            .maxRetries(2)
            .build()
    );
    
    List<TestMethod> tests = Arrays.asList(
        new TestMethod("loginTest", this::loginTest),
        new TestMethod("searchTest", this::searchTest),
        new TestMethod("checkoutTest", this::checkoutTest)
    );
    
    List<TestResult> results = executor.executeTests(tests);
    
    // Verify all tests completed
    Assert.assertEquals(results.size(), tests.size());
    Assert.assertTrue(results.stream().allMatch(TestResult::isSuccess));
}
```

### Scenario 2: Cross-Browser Parallel Testing
```java
@Test
public void testCrossBrowserParallel() {
    ParallelTestExecutor executor = new ParallelTestExecutor(
        ParallelConfig.builder()
            .threadCount(6)
            .browsers(Arrays.asList("chrome", "firefox", "edge"))
            .crossBrowserMode(true)
            .build()
    );
    
    // Each test will run on all browsers in parallel
    TestMethod loginTest = new TestMethod("loginTest", this::loginTest);
    List<TestResult> results = executor.executeCrossBrowser(loginTest);
    
    // Should have 3 results (one per browser)
    Assert.assertEquals(results.size(), 3);
}
```

### Scenario 3: Resource-Constrained Execution
```java
@Test
public void testResourceConstrainedExecution() {
    // Automatically adjust thread count based on system resources
    int optimalThreadCount = SystemResourceCalculator.calculateOptimalThreadCount();
    
    ParallelTestExecutor executor = new ParallelTestExecutor(
        ParallelConfig.builder()
            .threadCount(optimalThreadCount)
            .maxMemoryUsage(0.8) // 80% of available memory
            .resourceMonitoring(true)
            .build()
    );
    
    List<TestResult> results = executor.executeTests(getAllTests());
    
    // Verify resource usage stayed within limits
    ExecutionReport report = executor.getExecutionReport();
    Assert.assertTrue(report.getMaxMemoryUsage() < 0.8);
}
```

### Scenario 4: Fault-Tolerant Execution
```java
@Test
public void testFaultTolerantExecution() {
    ParallelTestExecutor executor = new ParallelTestExecutor(
        ParallelConfig.builder()
            .threadCount(8)
            .faultTolerance(true)
            .maxFailuresPerThread(3)
            .isolateFailures(true)
            .build()
    );
    
    // Include some tests that will fail
    List<TestMethod> tests = createMixedTestSuite(); // Some pass, some fail
    
    List<TestResult> results = executor.executeTests(tests);
    
    // Verify that failures in one thread don't affect others
    long successCount = results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
    Assert.assertTrue(successCount > 0, "Some tests should have passed despite failures");
}
```

## Advanced Features

### Dynamic Thread Scaling
```java
public class DynamicThreadScaler {
    public int calculateOptimalThreadCount() {
        int cpuCores = Runtime.getRuntime().availableProcessors();
        long availableMemory = getAvailableMemory();
        int currentLoad = getCurrentSystemLoad();
        
        // Algorithm to determine optimal thread count
        return Math.min(cpuCores * 2, (int)(availableMemory / MEMORY_PER_THREAD));
    }
    
    public void adjustThreadCount(ParallelTestExecutor executor, ExecutionMetrics metrics) {
        if (metrics.getAverageExecutionTime() > SLOW_THRESHOLD) {
            executor.reduceThreadCount();
        } else if (metrics.getResourceUtilization() < 0.6) {
            executor.increaseThreadCount();
        }
    }
}
```

### Load Balancing
```java
public class TestLoadBalancer {
    private final Map<String, Integer> threadWorkload = new ConcurrentHashMap<>();
    
    public String selectOptimalThread(TestMethod test) {
        // Select thread with lowest current workload
        return threadWorkload.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("default-thread");
    }
    
    public void updateWorkload(String threadId, int additionalWork) {
        threadWorkload.merge(threadId, additionalWork, Integer::sum);
    }
}
```

### Resource Health Monitoring
```java
public class ResourceHealthMonitor {
    public void monitorResourceHealth() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        
        scheduler.scheduleAtFixedRate(() -> {
            ResourceHealth health = checkResourceHealth();
            
            if (health.getMemoryUsage() > 0.9) {
                triggerGarbageCollection();
            }
            
            if (health.getOpenConnections() > MAX_CONNECTIONS) {
                closeIdleConnections();
            }
            
            if (health.getActiveThreads() > MAX_THREADS) {
                pauseNewTestExecution();
            }
        }, 0, 30, TimeUnit.SECONDS);
    }
}
```

## Success Criteria
1. Tests execute in parallel without resource conflicts
2. WebDriver instances are properly isolated per thread
3. Database connections are managed efficiently
4. Resource cleanup prevents memory leaks
5. System resources are optimally utilized
6. Failures in one thread don't affect others
7. Comprehensive monitoring and reporting available

## Extension Points
1. **Cloud Integration**: Scale execution across cloud instances
2. **Container Support**: Docker-based parallel execution
3. **Grid Integration**: Selenium Grid integration
4. **AI Optimization**: Machine learning for optimal resource allocation
5. **Real-time Dashboards**: Live monitoring of parallel execution

## Testing Strategy
1. **Unit Tests**: Test individual components in isolation
2. **Integration Tests**: Verify thread safety and resource management
3. **Load Tests**: Test with high thread counts and long-running tests
4. **Stress Tests**: Test resource limits and failure scenarios
5. **Performance Tests**: Measure overhead and optimization effectiveness

## Deliverables
1. `ParallelTestExecutor.java` - Main execution coordinator
2. `ThreadLocalResourceManager.java` - Thread-local resource management
3. `ResourcePool.java` - Shared resource pool implementation
4. `ResourceMonitor.java` - Resource monitoring and metrics
5. `ParallelConfig.java` - Configuration management
6. `ExecutionReport.java` - Reporting utilities
7. `ParallelExecutionTest.java` - Comprehensive test suite
8. `README.md` - Setup guide and best practices

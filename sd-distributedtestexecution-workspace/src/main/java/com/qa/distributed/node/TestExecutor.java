package com.qa.distributed.node;

import com.qa.distributed.common.TestResult;
import com.qa.distributed.common.TestTask;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Executes test tasks on distributed nodes using TestNG.
 * Design Decision: Isolated test execution with proper resource management
 * and comprehensive result capture for reliable distributed testing.
 */
public class TestExecutor {
    private final String nodeId;
    private final ExecutorService executorService;
    
    public TestExecutor(String nodeId, int maxConcurrentTests) {
        this.nodeId = nodeId;
        this.executorService = Executors.newFixedThreadPool(maxConcurrentTests);
    }
    
    /**
     * Execute test task asynchronously.
     * Design Decision: Async execution allows node to handle multiple tests
     * concurrently while maintaining isolation between test executions.
     */
    public CompletableFuture<TestResult> executeTaskAsync(TestTask task) {
        return CompletableFuture.supplyAsync(() -> executeTask(task), executorService);
    }
    
    /**
     * Execute test task synchronously.
     */
    public TestResult executeTask(TestTask task) {
        LocalDateTime startTime = LocalDateTime.now();
        long startMs = System.currentTimeMillis();
        
        System.out.println("Executing task: " + task);
        
        try {
            // Create TestNG suite programmatically
            XmlSuite suite = new XmlSuite();
            suite.setName("DistributedTestSuite");
            
            XmlTest test = new XmlTest(suite);
            test.setName("DistributedTest");
            
            // Add test class
            XmlClass xmlClass = new XmlClass(task.getTestClass());
            
            // If specific method is specified, include only that method
            if (task.getTestMethod() != null && !task.getTestMethod().isEmpty()) {
                List<String> methods = Arrays.asList(task.getTestMethod());
                xmlClass.setIncludedMethods(methods.stream()
                    .map(method -> {
                        org.testng.xml.XmlInclude include = new org.testng.xml.XmlInclude(method);
                        return include;
                    })
                    .collect(java.util.stream.Collectors.toList()));
            }
            
            test.setXmlClasses(Arrays.asList(xmlClass));
            
            // Set parameters from task
            for (Map.Entry<String, String> entry : task.getParameters().entrySet()) {
                test.addParameter(entry.getKey(), entry.getValue());
            }
            
            // Create and run TestNG
            TestNG testNG = new TestNG();
            testNG.setXmlSuites(Arrays.asList(suite));
            testNG.setVerbose(0); // Reduce output noise
            
            // Capture test results
            TestResultListener resultListener = new TestResultListener();
            testNG.addListener(resultListener);
            
            // Execute tests
            testNG.run();
            
            LocalDateTime endTime = LocalDateTime.now();
            long executionTime = System.currentTimeMillis() - startMs;
            
            // Determine overall result status
            TestResult.Status status = determineStatus(resultListener);
            String message = resultListener.getResultMessage();
            String stackTrace = resultListener.getStackTrace();
            
            return new TestResult(
                task.getTaskId(),
                nodeId,
                status,
                message,
                stackTrace,
                startTime,
                endTime,
                executionTime
            );
            
        } catch (Exception e) {
            LocalDateTime endTime = LocalDateTime.now();
            long executionTime = System.currentTimeMillis() - startMs;
            
            return new TestResult(
                task.getTaskId(),
                nodeId,
                TestResult.Status.ERROR,
                "Execution error: " + e.getMessage(),
                getStackTrace(e),
                startTime,
                endTime,
                executionTime
            );
        }
    }
    
    /**
     * Determine overall test status from TestNG results.
     */
    private TestResult.Status determineStatus(TestResultListener listener) {
        if (listener.getFailedCount() > 0) {
            return TestResult.Status.FAILED;
        } else if (listener.getSkippedCount() > 0) {
            return TestResult.Status.SKIPPED;
        } else if (listener.getPassedCount() > 0) {
            return TestResult.Status.PASSED;
        } else {
            return TestResult.Status.ERROR;
        }
    }
    
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage()).append("\n");
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
}

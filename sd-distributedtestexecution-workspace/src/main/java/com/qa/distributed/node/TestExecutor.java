package com.qa.distributed.node;

import com.qa.distributed.common.TestResult;
import com.qa.distributed.common.TestTask;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Simplified test executor for distributed testing demonstration.
 */
public class TestExecutor {
    private final String nodeId;
    private final ExecutorService executorService;
    
    public TestExecutor(String nodeId, int maxConcurrentTests) {
        this.nodeId = nodeId;
        this.executorService = Executors.newFixedThreadPool(maxConcurrentTests);
    }
    
    public CompletableFuture<TestResult> executeTaskAsync(TestTask task) {
        return CompletableFuture.supplyAsync(() -> executeTask(task), executorService);
    }
    
    public TestResult executeTask(TestTask task) {
        LocalDateTime startTime = LocalDateTime.now();
        long startMs = System.currentTimeMillis();
        
        try {
            // Simulate test execution
            Thread.sleep(100 + (int)(Math.random() * 200));
            
            LocalDateTime endTime = LocalDateTime.now();
            long executionTime = System.currentTimeMillis() - startMs;
            
            return new TestResult(
                task.getTaskId(),
                nodeId,
                TestResult.Status.PASSED,
                "Test executed successfully",
                null,
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
                TestResult.Status.FAILED,
                "Execution error: " + e.getMessage(),
                e.toString(),
                startTime,
                endTime,
                executionTime
            );
        }
    }
    
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

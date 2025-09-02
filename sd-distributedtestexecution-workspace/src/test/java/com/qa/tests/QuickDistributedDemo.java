package com.qa.tests;

import com.qa.distributed.common.TestTask;
import com.qa.distributed.common.TestResult;
import com.qa.distributed.node.TestExecutor;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class QuickDistributedDemo {
    
    @Test
    public void testDistributedConcepts() {
        System.out.println("=== Quick Distributed Test Demo ===");
        
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        try {
            // Create simple tasks
            List<TestTask> tasks = Arrays.asList(
                new TestTask("task-1", "com.qa.tests.SampleLoginTest", "testValidLogin", Map.of(), 1),
                new TestTask("task-2", "com.qa.tests.SampleCalculatorTest", "testAddition", Map.of(), 1)
            );
            
            System.out.println("Created " + tasks.size() + " tasks");
            
            // Execute tasks
            List<CompletableFuture<TestResult>> futures = new ArrayList<>();
            
            for (TestTask task : tasks) {
                CompletableFuture<TestResult> future = CompletableFuture.supplyAsync(() -> {
                    TestExecutor testExecutor = new TestExecutor("node-" + Thread.currentThread().getId(), 1);
                    try {
                        TestResult result = testExecutor.executeTask(task);
                        System.out.println("Completed: " + result.getTaskId() + " - " + result.getStatus());
                        return result;
                    } finally {
                        testExecutor.shutdown();
                    }
                }, executor);
                
                futures.add(future);
            }
            
            // Wait for completion
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(15, TimeUnit.SECONDS);
            
            // Show results
            System.out.println("\n=== Results ===");
            for (CompletableFuture<TestResult> future : futures) {
                TestResult result = future.get();
                System.out.printf("Task: %s | Status: %s | Time: %dms%n", 
                    result.getTaskId(), result.getStatus(), result.getExecutionTimeMs());
            }
            
            System.out.println("Demo completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            // Ensure proper cleanup
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}

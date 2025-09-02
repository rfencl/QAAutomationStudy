package com.qa.tests;

import com.qa.distributed.common.TestTask;
import com.qa.distributed.common.TestResult;
import com.qa.distributed.coordinator.TaskQueue;
import com.qa.distributed.node.TestExecutor;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Minimal demonstration of distributed test execution core concepts.
 */
public class MinimalDistributedDemo {
    
    @Test
    public void testDistributedExecution() {
        System.out.println("=== Minimal Distributed Test Execution Demo ===");
        
        // Create task queue
        TaskQueue taskQueue = new TaskQueue();
        
        // Create test tasks
        List<TestTask> tasks = Arrays.asList(
            new TestTask("task-1", "com.qa.tests.SampleLoginTest", "testValidLogin", Map.of(), 3),
            new TestTask("task-2", "com.qa.tests.SampleCalculatorTest", "testAddition", Map.of(), 2),
            new TestTask("task-3", "com.qa.tests.SampleCalculatorTest", "testSubtraction", Map.of(), 1)
        );
        
        // Add tasks to queue
        tasks.forEach(taskQueue::addTask);
        System.out.println("Added " + tasks.size() + " tasks to queue");
        
        // Create executors (simulating nodes)
        TestExecutor executor1 = new TestExecutor("node-1", 2);
        TestExecutor executor2 = new TestExecutor("node-2", 2);
        
        List<CompletableFuture<TestResult>> futures = new ArrayList<>();
        
        // Execute tasks
        try {
            while (!taskQueue.isEmpty()) {
                TestTask task = taskQueue.getNextTask();
                System.out.println("Executing task: " + task.getTaskId());
                
                // Alternate between executors
                TestExecutor executor = futures.size() % 2 == 0 ? executor1 : executor2;
                
                CompletableFuture<TestResult> future = executor.executeTaskAsync(task)
                    .thenApply(result -> {
                        taskQueue.completeTask(task.getTaskId());
                        System.out.println("Completed: " + result.getTaskId() + " - " + result.getStatus());
                        return result;
                    });
                
                futures.add(future);
            }
            
            // Wait for completion
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(30, TimeUnit.SECONDS);
            
            // Show results
            System.out.println("\n=== Results ===");
            futures.forEach(f -> {
                TestResult result = f.join();
                System.out.printf("Task: %s | Status: %s | Time: %dms%n", 
                    result.getTaskId(), result.getStatus(), result.getExecutionTimeMs());
            });
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            executor1.shutdown();
            executor2.shutdown();
        }
        
        System.out.println("Demo completed successfully!");
    }
}

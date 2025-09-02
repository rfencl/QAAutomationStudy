package com.qa.tests;

import com.qa.distributed.common.TestTask;
import com.qa.distributed.common.TestResult;
import com.qa.distributed.coordinator.TaskQueue;
import com.qa.distributed.coordinator.NodeRegistry;
import com.qa.distributed.common.NodeInfo;
import com.qa.distributed.node.TestExecutor;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simplified demonstration of distributed test execution core concepts
 * without HTTP communication complexity.
 */
public class SimpleDistributedDemo {
    
    @Test
    public void demonstrateDistributedExecution() {
        System.out.println("=== Distributed Test Execution Core Concepts Demo ===");
        
        // 1. Create task queue and node registry
        TaskQueue taskQueue = new TaskQueue();
        NodeRegistry nodeRegistry = new NodeRegistry(30);
        
        // 2. Register mock nodes
        NodeInfo node1 = new NodeInfo("node-1", "localhost", 8081, 2, 
                                     NodeInfo.Status.AVAILABLE, 0, LocalDateTime.now());
        NodeInfo node2 = new NodeInfo("node-2", "localhost", 8082, 3, 
                                     NodeInfo.Status.AVAILABLE, 0, LocalDateTime.now());
        
        nodeRegistry.registerNode(node1);
        nodeRegistry.registerNode(node2);
        
        System.out.println("Registered nodes: " + nodeRegistry.getNodeCount());
        
        // 3. Create test tasks
        List<TestTask> tasks = createSampleTasks();
        System.out.println("Created " + tasks.size() + " test tasks");
        
        // 4. Add tasks to queue
        tasks.forEach(taskQueue::addTask);
        System.out.println("Queue status: " + taskQueue.getQueueStats());
        
        // 5. Simulate distributed execution
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<CompletableFuture<TestResult>> futures = new ArrayList<>();
        
        // Create test executors for each node
        TestExecutor executor1 = new TestExecutor("node-1", 2);
        TestExecutor executor2 = new TestExecutor("node-2", 3);
        
        // Process tasks from queue
        while (!taskQueue.isEmpty()) {
            try {
                TestTask task = taskQueue.getNextTask();
                NodeInfo bestNode = nodeRegistry.getBestAvailableNode();
                
                if (bestNode != null) {
                    System.out.println("Assigning task " + task.getTaskId() + " to " + bestNode.getNodeId());
                    
                    // Select appropriate executor based on node
                    TestExecutor selectedExecutor = bestNode.getNodeId().equals("node-1") ? executor1 : executor2;
                    
                    // Execute task asynchronously
                    CompletableFuture<TestResult> future = selectedExecutor.executeTaskAsync(task)
                        .thenApply(result -> {
                            taskQueue.completeTask(task.getTaskId());
                            System.out.println("Task completed: " + result);
                            return result;
                        });
                    
                    futures.add(future);
                    
                    // Update node status (simulate load)
                    nodeRegistry.updateNodeStatus(bestNode.getNodeId(), 
                                                NodeInfo.Status.BUSY, 1);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // 6. Wait for all tasks to complete
        System.out.println("Waiting for " + futures.size() + " tasks to complete...");
        
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0]));
        
        try {
            allTasks.get(); // Wait for completion
            
            // 7. Collect and display results
            System.out.println("\n=== Execution Results ===");
            List<TestResult> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();
            
            results.forEach(result -> {
                System.out.printf("Task: %s | Node: %s | Status: %s | Time: %dms%n",
                    result.getTaskId().substring(0, 8), 
                    result.getNodeId(), 
                    result.getStatus(), 
                    result.getExecutionTimeMs());
            });
            
            // 8. Display statistics
            System.out.println("\n=== Statistics ===");
            System.out.println("Total tasks executed: " + results.size());
            System.out.println("Successful tasks: " + results.stream()
                .mapToInt(r -> r.isSuccess() ? 1 : 0).sum());
            System.out.println("Average execution time: " + results.stream()
                .mapToLong(TestResult::getExecutionTimeMs).average().orElse(0) + "ms");
            System.out.println("Final queue status: " + taskQueue.getQueueStats());
            
        } catch (Exception e) {
            System.err.println("Error waiting for task completion: " + e.getMessage());
        } finally {
            executor.shutdown();
            executor1.shutdown();
            executor2.shutdown();
        }
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private List<TestTask> createSampleTasks() {
        return Arrays.asList(
            new TestTask(UUID.randomUUID().toString(), 
                        "com.qa.tests.SampleLoginTest", "testValidLogin", 
                        Map.of("browser", "chrome"), 3),
            new TestTask(UUID.randomUUID().toString(), 
                        "com.qa.tests.SampleLoginTest", "testInvalidLogin", 
                        Map.of("browser", "firefox"), 2),
            new TestTask(UUID.randomUUID().toString(), 
                        "com.qa.tests.SampleCalculatorTest", "testAddition", 
                        Map.of(), 2),
            new TestTask(UUID.randomUUID().toString(), 
                        "com.qa.tests.SampleCalculatorTest", "testSubtraction", 
                        Map.of(), 1),
            new TestTask(UUID.randomUUID().toString(), 
                        "com.qa.tests.SampleCalculatorTest", "testMultiplication", 
                        Map.of(), 1)
        );
    }
}

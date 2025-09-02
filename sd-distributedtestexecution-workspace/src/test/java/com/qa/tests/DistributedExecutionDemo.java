package com.qa.tests;

import com.qa.distributed.common.TestTask;
import com.qa.distributed.coordinator.TestCoordinator;
import com.qa.distributed.node.TestNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Demonstration of distributed test execution system.
 * Design Decision: Integration test that shows complete system workflow
 * from coordinator setup to task distribution and result collection.
 */
public class DistributedExecutionDemo {
    private TestCoordinator coordinator;
    private List<TestNode> nodes;
    
    @BeforeClass
    public void setupDistributedSystem() {
        System.out.println("=== Setting up Distributed Test Execution System ===");
        
        // Start coordinator
        coordinator = new TestCoordinator(8080);
        coordinator.start();
        
        // Wait for coordinator to start
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Start multiple test nodes
        nodes = new ArrayList<>();
        
        // Node 1
        TestNode node1 = new TestNode("localhost", 8081, 2, "http://localhost:8080");
        node1.start();
        nodes.add(node1);
        
        // Node 2
        TestNode node2 = new TestNode("localhost", 8082, 3, "http://localhost:8080");
        node2.start();
        nodes.add(node2);
        
        // Node 3
        TestNode node3 = new TestNode("localhost", 8083, 2, "http://localhost:8080");
        node3.start();
        nodes.add(node3);
        
        // Wait for nodes to register
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Distributed system setup complete");
    }
    
    @Test
    public void testDistributedExecution() {
        System.out.println("=== Starting Distributed Test Execution ===");
        
        // Create test tasks
        List<TestTask> tasks = createTestTasks();
        
        System.out.println("Submitting " + tasks.size() + " tasks for distributed execution");
        
        // Submit tasks to coordinator
        coordinator.submitTasks(tasks);
        
        // Wait for all tasks to complete (timeout: 60 seconds)
        boolean completed = coordinator.waitForCompletion(60);
        
        if (completed) {
            System.out.println("All tasks completed successfully!");
            
            // Print results
            coordinator.getResults().forEach(result -> {
                System.out.println("Result: " + result);
            });
            
            System.out.println("Total results collected: " + coordinator.getResults().size());
        } else {
            System.err.println("Tasks did not complete within timeout");
        }
    }
    
    /**
     * Create sample test tasks for distributed execution.
     */
    private List<TestTask> createTestTasks() {
        List<TestTask> tasks = new ArrayList<>();
        
        // Login test tasks
        tasks.add(new TestTask(
            UUID.randomUUID().toString(),
            "com.qa.tests.SampleLoginTest",
            "testValidLogin",
            Map.of("browser", "chrome"),
            3
        ));
        
        tasks.add(new TestTask(
            UUID.randomUUID().toString(),
            "com.qa.tests.SampleLoginTest",
            "testInvalidLogin",
            Map.of("browser", "firefox"),
            2
        ));
        
        tasks.add(new TestTask(
            UUID.randomUUID().toString(),
            "com.qa.tests.SampleLoginTest",
            "testEmptyCredentials",
            Map.of("browser", "chrome"),
            1
        ));
        
        // Calculator test tasks
        tasks.add(new TestTask(
            UUID.randomUUID().toString(),
            "com.qa.tests.SampleCalculatorTest",
            "testAddition",
            Map.of(),
            2
        ));
        
        tasks.add(new TestTask(
            UUID.randomUUID().toString(),
            "com.qa.tests.SampleCalculatorTest",
            "testSubtraction",
            Map.of(),
            2
        ));
        
        tasks.add(new TestTask(
            UUID.randomUUID().toString(),
            "com.qa.tests.SampleCalculatorTest",
            "testMultiplication",
            Map.of(),
            1
        ));
        
        tasks.add(new TestTask(
            UUID.randomUUID().toString(),
            "com.qa.tests.SampleCalculatorTest",
            "testDivision",
            Map.of(),
            1
        ));
        
        return tasks;
    }
    
    @AfterClass
    public void teardownDistributedSystem() {
        System.out.println("=== Shutting down Distributed Test Execution System ===");
        
        // Shutdown nodes
        if (nodes != null) {
            nodes.forEach(TestNode::shutdown);
        }
        
        // Shutdown coordinator
        if (coordinator != null) {
            coordinator.shutdown();
        }
        
        System.out.println("Distributed system shutdown complete");
    }
}

package com.qa.microservices.tests;

import com.qa.microservices.client.OrderServiceClient;
import com.qa.microservices.client.UserServiceClient;
import com.qa.microservices.models.Order;
import com.qa.microservices.models.User;
import com.qa.microservices.testing.EndToEndTestOrchestrator;
import com.qa.microservices.virtualization.ServiceVirtualization;
import org.testng.annotations.*;
import org.testng.Assert;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * End-to-end testing across multiple microservices.
 * Design Decision: Tests complete business workflows that span multiple services.
 * This validates the entire system behavior from a user perspective.
 */
public class EndToEndTest {
    
    private ServiceVirtualization serviceVirtualization;
    private EndToEndTestOrchestrator orchestrator;
    private UserServiceClient userServiceClient;
    private OrderServiceClient orderServiceClient;
    
    @BeforeClass
    public void setupClass() {
        // Start virtual services
        serviceVirtualization = new ServiceVirtualization();
        serviceVirtualization.startServices();
        
        // Initialize service clients
        userServiceClient = new UserServiceClient("http://localhost:8081");
        orderServiceClient = new OrderServiceClient("http://localhost:8082");
        
        // Initialize orchestrator
        orchestrator = new EndToEndTestOrchestrator(userServiceClient, orderServiceClient);
    }
    
    @AfterClass
    public void teardownClass() {
        if (serviceVirtualization != null) {
            serviceVirtualization.stopServices();
        }
    }
    
    @Test(description = "Test complete order workflow from user creation to order completion")
    public void testCompleteOrderWorkflow() {
        // Create test user
        User testUser = new User("user-123", "testuser", "test@example.com", "ACTIVE");
        
        // Create test order
        Order testOrder = new Order(
            "order-123",
            "user-123",
            new ArrayList<>(), // Empty items list for simplicity
            new BigDecimal("99.99"),
            "PENDING",
            LocalDateTime.now()
        );
        
        // Execute end-to-end workflow
        boolean workflowSuccess = orchestrator.executeOrderWorkflowTest(testUser, testOrder);
        
        Assert.assertTrue(workflowSuccess, "Complete order workflow should succeed");
    }
    
    @Test(description = "Test service resilience and error handling")
    public void testServiceResilience() {
        boolean resilienceTest = orchestrator.testServiceResilience();
        
        Assert.assertTrue(resilienceTest, "Service resilience test should pass");
    }
    
    @Test(description = "Test cross-service performance")
    public void testCrossServicePerformance() {
        boolean performanceTest = orchestrator.testCrossServicePerformance();
        
        Assert.assertTrue(performanceTest, "Cross-service performance test should pass");
    }
    
    @Test(description = "Test individual service health checks")
    public void testServiceHealthChecks() {
        boolean userServiceHealthy = userServiceClient.isServiceHealthy();
        boolean orderServiceHealthy = orderServiceClient.isServiceHealthy();
        
        Assert.assertTrue(userServiceHealthy, "User service should be healthy");
        Assert.assertTrue(orderServiceHealthy, "Order service should be healthy");
    }
    
    @Test(description = "Test service integration with invalid data")
    public void testServiceIntegrationWithInvalidData() {
        // Test with null user
        User nullUser = null;
        Order testOrder = new Order(
            "order-invalid",
            "user-invalid",
            new ArrayList<>(),
            new BigDecimal("0.00"),
            "PENDING",
            LocalDateTime.now()
        );
        
        try {
            boolean result = orchestrator.executeOrderWorkflowTest(nullUser, testOrder);
            Assert.assertFalse(result, "Workflow should fail with invalid data");
        } catch (Exception e) {
            // Expected behavior - exception should be thrown for invalid data
            Assert.assertTrue(true, "Exception expected for invalid data");
        }
    }
    
    @Test(description = "Test concurrent service calls")
    public void testConcurrentServiceCalls() {
        // This test validates that multiple concurrent calls work correctly
        long startTime = System.currentTimeMillis();
        
        // Execute multiple performance tests concurrently
        boolean[] results = new boolean[3];
        
        Thread[] threads = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                results[index] = orchestrator.testCrossServicePerformance();
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Assert.fail("Thread interrupted during concurrent test");
            }
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        // Verify all tests passed
        for (boolean result : results) {
            Assert.assertTrue(result, "Concurrent service call should succeed");
        }
        
        // Verify concurrent execution was faster than sequential
        Assert.assertTrue(executionTime < 15000, 
            "Concurrent execution should complete within 15 seconds");
    }
}

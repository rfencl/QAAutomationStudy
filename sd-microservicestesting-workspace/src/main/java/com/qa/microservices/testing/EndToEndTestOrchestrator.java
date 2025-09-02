package com.qa.microservices.testing;

import com.qa.microservices.client.OrderServiceClient;
import com.qa.microservices.client.UserServiceClient;
import com.qa.microservices.models.Order;
import com.qa.microservices.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Orchestrates end-to-end testing across multiple microservices.
 * Design Decision: Coordinates complex business workflows that span multiple services.
 * This validates the entire system behavior from a user perspective.
 */
public class EndToEndTestOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(EndToEndTestOrchestrator.class);
    
    private final UserServiceClient userServiceClient;
    private final OrderServiceClient orderServiceClient;
    
    public EndToEndTestOrchestrator(UserServiceClient userServiceClient, 
                                   OrderServiceClient orderServiceClient) {
        this.userServiceClient = userServiceClient;
        this.orderServiceClient = orderServiceClient;
    }
    
    /**
     * Execute complete order workflow test.
     * Design Decision: Tests the entire business process from user creation to order completion.
     */
    public boolean executeOrderWorkflowTest(User testUser, Order testOrder) {
        logger.info("Starting end-to-end order workflow test");
        
        try {
            // Step 1: Verify user service is available
            if (!userServiceClient.isServiceHealthy()) {
                logger.error("User service is not healthy");
                return false;
            }
            
            // Step 2: Create or verify user exists
            User createdUser = userServiceClient.createUser(testUser);
            if (createdUser == null) {
                logger.error("Failed to create user");
                return false;
            }
            logger.info("User created successfully: {}", createdUser.getId());
            
            // Step 3: Verify order service is available
            if (!orderServiceClient.isServiceHealthy()) {
                logger.error("Order service is not healthy");
                return false;
            }
            
            // Step 4: Create order
            testOrder.setUserId(createdUser.getId());
            Order createdOrder = orderServiceClient.createOrder(testOrder);
            if (createdOrder == null) {
                logger.error("Failed to create order");
                return false;
            }
            logger.info("Order created successfully: {}", createdOrder.getOrderId());
            
            // Step 5: Verify order status progression
            return verifyOrderStatusProgression(createdOrder.getOrderId());
            
        } catch (Exception e) {
            logger.error("End-to-end test failed with exception", e);
            return false;
        }
    }
    
    /**
     * Test service resilience and recovery.
     * Design Decision: Validates system behavior under failure conditions.
     */
    public boolean testServiceResilience() {
        logger.info("Testing service resilience");
        
        try {
            // Test with non-existent user
            User nonExistentUser = userServiceClient.getUserById("user-404");
            if (nonExistentUser != null) {
                logger.error("Expected null for non-existent user");
                return false;
            }
            
            // Test error handling
            try {
                userServiceClient.getUserById("user-error");
                logger.error("Expected exception for error scenario");
                return false;
            } catch (RuntimeException e) {
                logger.info("Error handling working correctly: {}", e.getMessage());
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("Resilience test failed", e);
            return false;
        }
    }
    
    /**
     * Test performance across services.
     * Design Decision: Validates system performance under load and with slow dependencies.
     */
    public boolean testCrossServicePerformance() {
        logger.info("Testing cross-service performance");
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Test parallel service calls
            CompletableFuture<Boolean> userHealthCheck = CompletableFuture.supplyAsync(() -> 
                userServiceClient.isServiceHealthy());
            
            CompletableFuture<Boolean> orderHealthCheck = CompletableFuture.supplyAsync(() -> 
                orderServiceClient.isServiceHealthy());
            
            // Wait for both services with timeout
            CompletableFuture<Void> allChecks = CompletableFuture.allOf(userHealthCheck, orderHealthCheck);
            allChecks.get(5, TimeUnit.SECONDS);
            
            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("Cross-service performance test completed in {}ms", executionTime);
            
            return userHealthCheck.get() && orderHealthCheck.get() && executionTime < 5000;
            
        } catch (Exception e) {
            logger.error("Performance test failed", e);
            return false;
        }
    }
    
    /**
     * Verify order status progression through workflow states.
     */
    private boolean verifyOrderStatusProgression(String orderId) {
        try {
            // Verify initial status
            Order order = orderServiceClient.getOrderById(orderId);
            if (!"PENDING".equals(order.getStatus())) {
                logger.error("Expected PENDING status, got: {}", order.getStatus());
                return false;
            }
            
            // Update to CONFIRMED
            Order confirmedOrder = orderServiceClient.updateOrderStatus(orderId, "CONFIRMED");
            if (!"CONFIRMED".equals(confirmedOrder.getStatus())) {
                logger.error("Failed to update order status to CONFIRMED");
                return false;
            }
            
            logger.info("Order status progression verified successfully");
            return true;
            
        } catch (Exception e) {
            logger.error("Order status progression verification failed", e);
            return false;
        }
    }
}

package com.qa.microservices.tests;

import com.qa.microservices.client.UserServiceClient;
import com.qa.microservices.models.User;
import com.qa.microservices.virtualization.ServiceVirtualization;
import org.testng.annotations.*;
import org.testng.Assert;

/**
 * Tests for service virtualization using WireMock.
 * Design Decision: Validates that virtual services behave correctly and provide
 * predictable responses for various test scenarios.
 */
public class ServiceVirtualizationTest {
    
    private ServiceVirtualization serviceVirtualization;
    private UserServiceClient userServiceClient;
    
    @BeforeClass
    public void setupClass() {
        serviceVirtualization = new ServiceVirtualization();
        serviceVirtualization.startServices();
        userServiceClient = new UserServiceClient("http://localhost:8081");
    }
    
    @AfterClass
    public void teardownClass() {
        if (serviceVirtualization != null) {
            serviceVirtualization.stopServices();
        }
    }
    
    @BeforeMethod
    public void setupMethod() {
        // Reset stubs before each test for clean state
        serviceVirtualization.resetStubs();
        serviceVirtualization.startServices(); // Restart with fresh stubs
    }
    
    @Test(description = "Test successful user retrieval from virtual service")
    public void testSuccessfulUserRetrieval() {
        // Test successful scenario
        User user = userServiceClient.getUserById("user-123");
        
        Assert.assertNotNull(user, "User should not be null");
        Assert.assertEquals(user.getId(), "user-123", "User ID should match");
        Assert.assertEquals(user.getUsername(), "testuser", "Username should match");
        Assert.assertEquals(user.getEmail(), "test@example.com", "Email should match");
        Assert.assertEquals(user.getStatus(), "ACTIVE", "Status should be ACTIVE");
    }
    
    @Test(description = "Test user not found scenario")
    public void testUserNotFound() {
        // Test 404 scenario
        User user = userServiceClient.getUserById("user-404");
        
        Assert.assertNull(user, "User should be null for non-existent user");
    }
    
    @Test(description = "Test service error handling", 
          expectedExceptions = RuntimeException.class,
          expectedExceptionsMessageRegExp = "Failed to get user: 500")
    public void testServiceError() {
        // Test 500 error scenario
        userServiceClient.getUserById("user-error");
    }
    
    @Test(description = "Test service health check")
    public void testServiceHealthCheck() {
        boolean isHealthy = userServiceClient.isServiceHealthy();
        
        Assert.assertTrue(isHealthy, "Service should be healthy");
    }
    
    @Test(description = "Test slow response handling")
    public void testSlowResponse() {
        long startTime = System.currentTimeMillis();
        
        User user = userServiceClient.getUserById("user-slow");
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        Assert.assertNotNull(user, "User should not be null even with slow response");
        Assert.assertTrue(executionTime >= 3000, "Response should take at least 3 seconds");
        Assert.assertEquals(user.getId(), "user-slow", "User ID should match");
    }
}

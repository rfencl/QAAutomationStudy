package com.qa.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Sample test class for distributed execution demonstration.
 */
public class SampleLoginTest {
    
    @Test(priority = 1)
    public void testValidLogin() {
        System.out.println("Executing testValidLogin on thread: " + Thread.currentThread().getName());
        
        // Simulate test execution time
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Simulate successful login test
        boolean loginSuccess = true;
        Assert.assertTrue(loginSuccess, "Valid login should succeed");
        
        System.out.println("testValidLogin completed successfully");
    }
    
    @Test(priority = 2)
    public void testInvalidLogin() {
        System.out.println("Executing testInvalidLogin on thread: " + Thread.currentThread().getName());
        
        // Simulate test execution time
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Simulate invalid login test
        boolean loginFailed = true;
        Assert.assertTrue(loginFailed, "Invalid login should fail");
        
        System.out.println("testInvalidLogin completed successfully");
    }
    
    @Test(priority = 3)
    public void testEmptyCredentials() {
        System.out.println("Executing testEmptyCredentials on thread: " + Thread.currentThread().getName());
        
        // Simulate test execution time
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Simulate empty credentials test
        boolean validationError = true;
        Assert.assertTrue(validationError, "Empty credentials should show validation error");
        
        System.out.println("testEmptyCredentials completed successfully");
    }
}

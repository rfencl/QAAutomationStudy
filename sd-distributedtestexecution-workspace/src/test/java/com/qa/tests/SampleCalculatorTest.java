package com.qa.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Sample calculator test class for distributed execution demonstration.
 */
public class SampleCalculatorTest {
    
    @Test(priority = 1)
    public void testAddition() {
        System.out.println("Executing testAddition on thread: " + Thread.currentThread().getName());
        
        // Simulate test execution time
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        int result = 5 + 3;
        Assert.assertEquals(result, 8, "Addition should work correctly");
        
        System.out.println("testAddition completed successfully");
    }
    
    @Test(priority = 2)
    public void testSubtraction() {
        System.out.println("Executing testSubtraction on thread: " + Thread.currentThread().getName());
        
        // Simulate test execution time
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        int result = 10 - 4;
        Assert.assertEquals(result, 6, "Subtraction should work correctly");
        
        System.out.println("testSubtraction completed successfully");
    }
    
    @Test(priority = 3)
    public void testMultiplication() {
        System.out.println("Executing testMultiplication on thread: " + Thread.currentThread().getName());
        
        // Simulate test execution time
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        int result = 6 * 7;
        Assert.assertEquals(result, 42, "Multiplication should work correctly");
        
        System.out.println("testMultiplication completed successfully");
    }
    
    @Test(priority = 4)
    public void testDivision() {
        System.out.println("Executing testDivision on thread: " + Thread.currentThread().getName());
        
        // Simulate test execution time
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        double result = 15.0 / 3.0;
        Assert.assertEquals(result, 5.0, "Division should work correctly");
        
        System.out.println("testDivision completed successfully");
    }
}

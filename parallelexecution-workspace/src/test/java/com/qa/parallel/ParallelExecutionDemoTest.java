package com.qa.parallel;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ParallelExecutionDemoTest extends ParallelTestBase {
    
    @Test
    public void demonstrateParallelExecution1() {
        logTestInfo("demonstrateParallelExecution1");
        WebDriver driver = getDriver();
        
        long startTime = System.currentTimeMillis();
        
        driver.get("https://the-internet.herokuapp.com/");
        String title = driver.getTitle();
        Assert.assertEquals(title, "The Internet");
        
        // Simulate some processing time
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long endTime = System.currentTimeMillis();
        System.out.printf("[Thread-%d] Test 1 completed in %dms%n", 
            Thread.currentThread().getId(), (endTime - startTime));
    }
    
    @Test
    public void demonstrateParallelExecution2() {
        logTestInfo("demonstrateParallelExecution2");
        WebDriver driver = getDriver();
        
        long startTime = System.currentTimeMillis();
        
        driver.get("https://the-internet.herokuapp.com/login");
        boolean usernameExists = driver.findElement(By.id("username")).isDisplayed();
        Assert.assertTrue(usernameExists);
        
        // Simulate some processing time
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long endTime = System.currentTimeMillis();
        System.out.printf("[Thread-%d] Test 2 completed in %dms%n", 
            Thread.currentThread().getId(), (endTime - startTime));
    }
    
    @Test
    public void demonstrateParallelExecution3() {
        logTestInfo("demonstrateParallelExecution3");
        WebDriver driver = getDriver();
        
        long startTime = System.currentTimeMillis();
        
        driver.get("https://the-internet.herokuapp.com/dropdown");
        boolean dropdownExists = driver.findElement(By.id("dropdown")).isDisplayed();
        Assert.assertTrue(dropdownExists);
        
        // Simulate some processing time
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long endTime = System.currentTimeMillis();
        System.out.printf("[Thread-%d] Test 3 completed in %dms%n", 
            Thread.currentThread().getId(), (endTime - startTime));
    }
    
    @Test
    public void demonstrateParallelExecution4() {
        logTestInfo("demonstrateParallelExecution4");
        WebDriver driver = getDriver();
        
        long startTime = System.currentTimeMillis();
        
        driver.get("https://the-internet.herokuapp.com/checkboxes");
        boolean checkboxExists = !driver.findElements(By.xpath("//input[@type='checkbox']")).isEmpty();
        Assert.assertTrue(checkboxExists);
        
        // Simulate some processing time
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long endTime = System.currentTimeMillis();
        System.out.printf("[Thread-%d] Test 4 completed in %dms%n", 
            Thread.currentThread().getId(), (endTime - startTime));
    }
}

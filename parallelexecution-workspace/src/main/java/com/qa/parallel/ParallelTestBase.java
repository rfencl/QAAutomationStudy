package com.qa.parallel;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;

public class ParallelTestBase {
    
    @BeforeMethod
    @Parameters({"browser"})
    public void setUp(String browser) {
        long startTime = System.currentTimeMillis();
        
        ThreadSafeDriverManager.setDriver(browser);
        
        long endTime = System.currentTimeMillis();
        String threadInfo = String.format("[Thread-%d] Setup completed in %dms", 
            Thread.currentThread().getId(), (endTime - startTime));
        System.out.println(threadInfo);
    }
    
    @AfterMethod
    public void tearDown() {
        ThreadSafeDriverManager.quitDriver();
    }
    
    protected WebDriver getDriver() {
        return ThreadSafeDriverManager.getDriver();
    }
    
    protected void logTestInfo(String testName) {
        String threadInfo = String.format("[Thread-%d] Executing: %s", 
            Thread.currentThread().getId(), testName);
        System.out.println(threadInfo);
    }
}

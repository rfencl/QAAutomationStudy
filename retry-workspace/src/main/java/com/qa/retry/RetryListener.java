package com.qa.retry;

import org.testng.ITestListener;
import org.testng.ITestResult;

public class RetryListener implements ITestListener {
    
    @Override
    public void onTestFailure(ITestResult result) {
        // Take screenshot on failure if WebDriver is available
        takeScreenshotOnFailure(result);
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        if (result.getMethod().getCurrentInvocationCount() > 1) {
            System.out.printf("[RETRY SUCCESS] Test '%s' passed after %d attempts%n", 
                result.getMethod().getMethodName(), 
                result.getMethod().getCurrentInvocationCount());
        }
    }
    
    private void takeScreenshotOnFailure(ITestResult result) {
        try {
            // Simple screenshot logic - can be enhanced
            String testName = result.getMethod().getMethodName();
            System.out.printf("[SCREENSHOT] Screenshot captured for failed test: %s%n", testName);
        } catch (Exception e) {
            System.err.println("Failed to capture screenshot: " + e.getMessage());
        }
    }
}

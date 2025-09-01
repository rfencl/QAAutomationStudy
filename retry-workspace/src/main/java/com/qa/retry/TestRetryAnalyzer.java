package com.qa.retry;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class TestRetryAnalyzer implements IRetryAnalyzer {
    private static final ThreadLocal<Integer> retryCount = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };
    
    private final RetryConfig config;
    
    public TestRetryAnalyzer() {
        this.config = new RetryConfig();
    }
    
    public TestRetryAnalyzer(RetryConfig config) {
        this.config = config;
    }
    
    @Override
    public boolean retry(ITestResult result) {
        int currentRetryCount = retryCount.get();
        
        if (currentRetryCount < config.getMaxRetries()) {
            if (isRetryableException(result.getThrowable())) {
                currentRetryCount++;
                retryCount.set(currentRetryCount);
                
                logRetryAttempt(result, currentRetryCount);
                addDelay(currentRetryCount);
                
                return true;
            }
        }
        
        // Reset retry count for next test
        retryCount.remove();
        return false;
    }
    
    private boolean isRetryableException(Throwable throwable) {
        if (throwable == null) return false;
        
        return config.getRetryableExceptions().stream()
            .anyMatch(exceptionClass -> exceptionClass.isAssignableFrom(throwable.getClass()));
    }
    
    private void logRetryAttempt(ITestResult result, int attemptNumber) {
        String testName = result.getMethod().getMethodName();
        String errorMessage = result.getThrowable().getMessage();
        
        System.out.printf("[RETRY] Attempt %d/%d for test '%s' - Reason: %s%n", 
            attemptNumber, config.getMaxRetries(), testName, errorMessage);
    }
    
    private void addDelay(int attemptNumber) {
        try {
            long delay = config.isExponentialBackoff() 
                ? config.getDelayMs() * (long) Math.pow(2, attemptNumber - 1)
                : config.getDelayMs();
            
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

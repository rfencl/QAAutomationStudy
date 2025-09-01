package com.qa.retry;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class TestRetryAnalyzer implements IRetryAnalyzer {
    private static final ThreadLocal<java.util.Map<String, Integer>> retryCountMap = 
        ThreadLocal.withInitial(() -> new java.util.HashMap<>());
    
    private final RetryConfig config;
    
    public TestRetryAnalyzer() {
        this.config = new RetryConfig();
    }
    
    public TestRetryAnalyzer(RetryConfig config) {
        this.config = config;
    }
    
    @Override
    public boolean retry(ITestResult result) {
        String testKey = getTestKey(result);
        int currentRetryCount = retryCountMap.get().getOrDefault(testKey, 0);
        
        if (currentRetryCount < config.getMaxRetries()) {
            if (isRetryableException(result.getThrowable())) {
                currentRetryCount++;
                retryCountMap.get().put(testKey, currentRetryCount);
                
                logRetryAttempt(result, currentRetryCount);
                addDelay(currentRetryCount);
                
                return true;
            }
        }
        
        return false;
    }
    
    private String getTestKey(ITestResult result) {
        String methodName = result.getMethod() != null ? result.getMethod().getMethodName() : "unknown";
        return methodName + "_" + System.identityHashCode(this);
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

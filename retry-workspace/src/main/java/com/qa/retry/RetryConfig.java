package com.qa.retry;

import java.util.Set;

public class RetryConfig {
    private int maxRetries = 3;
    private long delayMs = 1000;
    private boolean exponentialBackoff = true;
    private Set<Class<? extends Throwable>> retryableExceptions = Set.of(
        org.openqa.selenium.WebDriverException.class,
        org.openqa.selenium.NoSuchElementException.class,
        org.openqa.selenium.TimeoutException.class
    );
    
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    
    public long getDelayMs() { return delayMs; }
    public void setDelayMs(long delayMs) { this.delayMs = delayMs; }
    
    public boolean isExponentialBackoff() { return exponentialBackoff; }
    public void setExponentialBackoff(boolean exponentialBackoff) { this.exponentialBackoff = exponentialBackoff; }
    
    public Set<Class<? extends Throwable>> getRetryableExceptions() { return retryableExceptions; }
    public void setRetryableExceptions(Set<Class<? extends Throwable>> retryableExceptions) { 
        this.retryableExceptions = retryableExceptions; 
    }
}

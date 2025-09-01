package com.qa.retry;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.ITestNGMethod;
import org.testng.annotations.Test;
import org.mockito.Mockito;
import java.util.Set;

public class RetryAnalyzerTest {
    
    @Test
    public void testRetryableException() {
        RetryConfig config = new RetryConfig();
        config.setMaxRetries(2);
        config.setRetryableExceptions(Set.of(RuntimeException.class));
        
        TestRetryAnalyzer analyzer = new TestRetryAnalyzer(config);
        ITestResult mockResult = Mockito.mock(ITestResult.class);
        ITestNGMethod mockMethod = Mockito.mock(ITestNGMethod.class);
        
        Mockito.when(mockResult.getThrowable()).thenReturn(new RuntimeException("Test exception"));
        Mockito.when(mockResult.getMethod()).thenReturn(mockMethod);
        Mockito.when(mockMethod.getMethodName()).thenReturn("testMethod");
        
        // First retry should return true
        boolean shouldRetry = analyzer.retry(mockResult);
        Assert.assertTrue(shouldRetry, "Should retry on retryable exception");
    }
    
    @Test
    public void testNonRetryableException() {
        RetryConfig config = new RetryConfig();
        config.setMaxRetries(2);
        config.setRetryableExceptions(Set.of(RuntimeException.class));
        
        TestRetryAnalyzer analyzer = new TestRetryAnalyzer(config);
        ITestResult mockResult = Mockito.mock(ITestResult.class);
        
        Mockito.when(mockResult.getThrowable()).thenReturn(new AssertionError("Assertion failed"));
        
        // Should not retry on non-retryable exception
        boolean shouldRetry = analyzer.retry(mockResult);
        Assert.assertFalse(shouldRetry, "Should not retry on non-retryable exception");
    }
    
    @Test
    public void testMaxRetriesExceeded() {
        RetryConfig config = new RetryConfig();
        config.setMaxRetries(1);
        config.setRetryableExceptions(Set.of(RuntimeException.class));
        
        TestRetryAnalyzer analyzer = new TestRetryAnalyzer(config);
        ITestResult mockResult = Mockito.mock(ITestResult.class);
        ITestNGMethod mockMethod = Mockito.mock(ITestNGMethod.class);
        
        Mockito.when(mockResult.getThrowable()).thenReturn(new RuntimeException("Test exception"));
        Mockito.when(mockResult.getMethod()).thenReturn(mockMethod);
        Mockito.when(mockMethod.getMethodName()).thenReturn("testMethod");
        
        // First retry should return true
        boolean firstRetry = analyzer.retry(mockResult);
        Assert.assertTrue(firstRetry, "First retry should return true");
        
        // Second retry should return false (max retries exceeded)
        boolean secondRetry = analyzer.retry(mockResult);
        Assert.assertFalse(secondRetry, "Should not retry when max retries exceeded");
    }
}

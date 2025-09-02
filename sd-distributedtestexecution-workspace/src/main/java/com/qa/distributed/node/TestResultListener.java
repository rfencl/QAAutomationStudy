package com.qa.distributed.node;

import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * TestNG listener to capture test execution results.
 * Design Decision: Custom listener provides detailed result capture
 * for comprehensive reporting in distributed environment.
 */
public class TestResultListener implements ITestListener {
    private final AtomicInteger passedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);
    private final AtomicInteger skippedCount = new AtomicInteger(0);
    private volatile String resultMessage = "";
    private volatile String stackTrace = "";
    
    @Override
    public void onTestSuccess(ITestResult result) {
        passedCount.incrementAndGet();
        updateResultMessage(result, "PASSED");
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        failedCount.incrementAndGet();
        updateResultMessage(result, "FAILED");
        captureStackTrace(result);
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        skippedCount.incrementAndGet();
        updateResultMessage(result, "SKIPPED");
    }
    
    private void updateResultMessage(ITestResult result, String status) {
        String methodName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName();
        resultMessage = String.format("%s: %s.%s", status, className, methodName);
    }
    
    private void captureStackTrace(ITestResult result) {
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(throwable.getClass().getSimpleName())
              .append(": ")
              .append(throwable.getMessage())
              .append("\n");
              
            for (StackTraceElement element : throwable.getStackTrace()) {
                sb.append("\tat ").append(element.toString()).append("\n");
            }
            stackTrace = sb.toString();
        }
    }
    
    public int getPassedCount() { return passedCount.get(); }
    public int getFailedCount() { return failedCount.get(); }
    public int getSkippedCount() { return skippedCount.get(); }
    public String getResultMessage() { return resultMessage; }
    public String getStackTrace() { return stackTrace; }
}

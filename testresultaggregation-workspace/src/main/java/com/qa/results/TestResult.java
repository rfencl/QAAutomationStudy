package com.qa.results;

import java.time.LocalDateTime;

public class TestResult {
    private String testName;
    private String className;
    private String status;
    private long duration;
    private String errorMessage;
    private LocalDateTime timestamp;
    private String browser;
    private String environment;
    
    public TestResult() {}
    
    public TestResult(String testName, String className, String status, long duration) {
        this.testName = testName;
        this.className = className;
        this.status = status;
        this.duration = duration;
        this.timestamp = LocalDateTime.now();
    }
    
    public TestResult(String testName, String className, String status, long duration, 
                     String errorMessage, String browser, String environment) {
        this(testName, className, status, duration);
        this.errorMessage = errorMessage;
        this.browser = browser;
        this.environment = environment;
    }
    
    // Getters and Setters
    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }
    
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }
    
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    
    public boolean isPassed() { return "PASS".equals(status); }
    public boolean isFailed() { return "FAIL".equals(status); }
    public boolean isSkipped() { return "SKIP".equals(status); }
    
    @Override
    public String toString() {
        return String.format("%s.%s: %s (%dms)", className, testName, status, duration);
    }
}

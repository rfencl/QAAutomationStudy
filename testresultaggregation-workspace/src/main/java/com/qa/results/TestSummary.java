package com.qa.results;

import java.time.LocalDateTime;
import java.util.List;

public class TestSummary {
    private int totalTests;
    private int passedTests;
    private int failedTests;
    private int skippedTests;
    private double passRate;
    private long totalDuration;
    private long averageDuration;
    private LocalDateTime executionTime;
    private List<TestResult> failedTestDetails;
    
    public TestSummary() {}
    
    public TestSummary(int totalTests, int passedTests, int failedTests, int skippedTests,
                      long totalDuration, List<TestResult> failedTestDetails) {
        this.totalTests = totalTests;
        this.passedTests = passedTests;
        this.failedTests = failedTests;
        this.skippedTests = skippedTests;
        this.totalDuration = totalDuration;
        this.failedTestDetails = failedTestDetails;
        this.executionTime = LocalDateTime.now();
        
        this.passRate = totalTests > 0 ? (double) passedTests / totalTests * 100 : 0;
        this.averageDuration = totalTests > 0 ? totalDuration / totalTests : 0;
    }
    
    // Getters and Setters
    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }
    
    public int getPassedTests() { return passedTests; }
    public void setPassedTests(int passedTests) { this.passedTests = passedTests; }
    
    public int getFailedTests() { return failedTests; }
    public void setFailedTests(int failedTests) { this.failedTests = failedTests; }
    
    public int getSkippedTests() { return skippedTests; }
    public void setSkippedTests(int skippedTests) { this.skippedTests = skippedTests; }
    
    public double getPassRate() { return passRate; }
    public void setPassRate(double passRate) { this.passRate = passRate; }
    
    public long getTotalDuration() { return totalDuration; }
    public void setTotalDuration(long totalDuration) { this.totalDuration = totalDuration; }
    
    public long getAverageDuration() { return averageDuration; }
    public void setAverageDuration(long averageDuration) { this.averageDuration = averageDuration; }
    
    public LocalDateTime getExecutionTime() { return executionTime; }
    public void setExecutionTime(LocalDateTime executionTime) { this.executionTime = executionTime; }
    
    public List<TestResult> getFailedTestDetails() { return failedTestDetails; }
    public void setFailedTestDetails(List<TestResult> failedTestDetails) { this.failedTestDetails = failedTestDetails; }
    
    @Override
    public String toString() {
        return String.format("Tests: %d, Passed: %d, Failed: %d, Skipped: %d, Pass Rate: %.1f%%, Duration: %dms",
                totalTests, passedTests, failedTests, skippedTests, passRate, totalDuration);
    }
}

package com.qa.results;

import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.ISuite;
import org.testng.ISuiteListener;

public class TestResultListener implements ITestListener, ISuiteListener {
    private static final TestResultAggregator aggregator = new TestResultAggregator();
    
    @Override
    public void onTestSuccess(ITestResult result) {
        addTestResult(result, "PASS", null);
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        String errorMessage = result.getThrowable() != null ? 
            result.getThrowable().getMessage() : "Unknown error";
        addTestResult(result, "FAIL", errorMessage);
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        String errorMessage = result.getThrowable() != null ? 
            result.getThrowable().getMessage() : "Test skipped";
        addTestResult(result, "SKIP", errorMessage);
    }
    
    private void addTestResult(ITestResult result, String status, String errorMessage) {
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName();
        long duration = result.getEndMillis() - result.getStartMillis();
        
        String browser = System.getProperty("browser", "chrome");
        String environment = System.getProperty("environment", "test");
        
        TestResult testResult = new TestResult(testName, className, status, duration, 
                                             errorMessage, browser, environment);
        aggregator.addResult(testResult);
        
        System.out.printf("[%s] %s.%s: %s (%dms)%n", 
            status, className, testName, status, duration);
    }
    
    @Override
    public void onFinish(ISuite suite) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("TEST SUITE COMPLETED: " + suite.getName());
        System.out.println("=".repeat(50));
        
        aggregator.printSummary();
        
        try {
            String reportPath = "target/test-results-report.json";
            aggregator.exportToJson(reportPath);
            System.out.println("\n📊 Detailed report exported to: " + reportPath);
        } catch (Exception e) {
            System.err.println("Failed to export report: " + e.getMessage());
        }
    }
    
    public static TestResultAggregator getAggregator() {
        return aggregator;
    }
}

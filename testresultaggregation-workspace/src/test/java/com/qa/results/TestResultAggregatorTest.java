package com.qa.results;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.List;
import java.util.Map;

public class TestResultAggregatorTest {
    private TestResultAggregator aggregator;
    
    @BeforeMethod
    public void setUp() {
        aggregator = new TestResultAggregator();
        
        // Add sample test results
        aggregator.addResult(new TestResult("test1", "LoginTest", "PASS", 1000, null, "chrome", "test"));
        aggregator.addResult(new TestResult("test2", "LoginTest", "FAIL", 2000, "Login failed", "chrome", "test"));
        aggregator.addResult(new TestResult("test3", "FormTest", "PASS", 1500, null, "firefox", "test"));
        aggregator.addResult(new TestResult("test4", "FormTest", "SKIP", 500, "Test skipped", "firefox", "test"));
        aggregator.addResult(new TestResult("test5", "NavigationTest", "PASS", 800, null, "chrome", "prod"));
    }
    
    @Test
    public void testGenerateSummary() {
        TestSummary summary = aggregator.generateSummary();
        
        Assert.assertEquals(summary.getTotalTests(), 5);
        Assert.assertEquals(summary.getPassedTests(), 3);
        Assert.assertEquals(summary.getFailedTests(), 1);
        Assert.assertEquals(summary.getSkippedTests(), 1);
        Assert.assertEquals(summary.getTotalDuration(), 5800);
        Assert.assertEquals(summary.getPassRate(), 60.0, 0.1);
    }
    
    @Test
    public void testGenerateSummaryByClass() {
        Map<String, TestSummary> summaryByClass = aggregator.generateSummaryByClass();
        
        Assert.assertEquals(summaryByClass.size(), 3);
        Assert.assertTrue(summaryByClass.containsKey("LoginTest"));
        Assert.assertTrue(summaryByClass.containsKey("FormTest"));
        Assert.assertTrue(summaryByClass.containsKey("NavigationTest"));
        
        TestSummary loginSummary = summaryByClass.get("LoginTest");
        Assert.assertEquals(loginSummary.getTotalTests(), 2);
        Assert.assertEquals(loginSummary.getPassedTests(), 1);
        Assert.assertEquals(loginSummary.getFailedTests(), 1);
    }
    
    @Test
    public void testGenerateSummaryByBrowser() {
        Map<String, TestSummary> summaryByBrowser = aggregator.generateSummaryByBrowser();
        
        Assert.assertEquals(summaryByBrowser.size(), 2);
        Assert.assertTrue(summaryByBrowser.containsKey("chrome"));
        Assert.assertTrue(summaryByBrowser.containsKey("firefox"));
        
        TestSummary chromeSummary = summaryByBrowser.get("chrome");
        Assert.assertEquals(chromeSummary.getTotalTests(), 3);
        Assert.assertEquals(chromeSummary.getPassedTests(), 2);
        Assert.assertEquals(chromeSummary.getFailedTests(), 1);
    }
    
    @Test
    public void testGetTopFailures() {
        List<TestResult> topFailures = aggregator.getTopFailures(5);
        
        Assert.assertEquals(topFailures.size(), 1);
        Assert.assertEquals(topFailures.get(0).getTestName(), "test2");
        Assert.assertEquals(topFailures.get(0).getStatus(), "FAIL");
    }
    
    @Test
    public void testGetSlowestTests() {
        List<TestResult> slowestTests = aggregator.getSlowestTests(3);
        
        Assert.assertEquals(slowestTests.size(), 3);
        Assert.assertEquals(slowestTests.get(0).getDuration(), 2000); // test2
        Assert.assertEquals(slowestTests.get(1).getDuration(), 1500); // test3
        Assert.assertEquals(slowestTests.get(2).getDuration(), 1000); // test1
    }
    
    @Test
    public void testExportToJson() {
        try {
            String filePath = "target/test-export.json";
            aggregator.exportToJson(filePath);
            
            // Verify file exists
            java.io.File file = new java.io.File(filePath);
            Assert.assertTrue(file.exists());
            Assert.assertTrue(file.length() > 0);
        } catch (Exception e) {
            Assert.fail("Export to JSON failed: " + e.getMessage());
        }
    }
}

package com.qa.results;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TestResultAggregator {
    private final List<TestResult> testResults = new ArrayList<>();
    private final ObjectMapper objectMapper;
    
    public TestResultAggregator() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    public void addResult(TestResult result) {
        testResults.add(result);
    }
    
    public void addResults(List<TestResult> results) {
        testResults.addAll(results);
    }
    
    public TestSummary generateSummary() {
        int total = testResults.size();
        int passed = (int) testResults.stream().filter(TestResult::isPassed).count();
        int failed = (int) testResults.stream().filter(TestResult::isFailed).count();
        int skipped = (int) testResults.stream().filter(TestResult::isSkipped).count();
        
        long totalDuration = testResults.stream().mapToLong(TestResult::getDuration).sum();
        List<TestResult> failedTests = testResults.stream()
                .filter(TestResult::isFailed)
                .collect(Collectors.toList());
        
        return new TestSummary(total, passed, failed, skipped, totalDuration, failedTests);
    }
    
    public Map<String, TestSummary> generateSummaryByClass() {
        return testResults.stream()
                .collect(Collectors.groupingBy(TestResult::getClassName))
                .entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> {
                        List<TestResult> classResults = entry.getValue();
                        int total = classResults.size();
                        int passed = (int) classResults.stream().filter(TestResult::isPassed).count();
                        int failed = (int) classResults.stream().filter(TestResult::isFailed).count();
                        int skipped = (int) classResults.stream().filter(TestResult::isSkipped).count();
                        long totalDuration = classResults.stream().mapToLong(TestResult::getDuration).sum();
                        List<TestResult> failedTests = classResults.stream()
                                .filter(TestResult::isFailed)
                                .collect(Collectors.toList());
                        return new TestSummary(total, passed, failed, skipped, totalDuration, failedTests);
                    }
                ));
    }
    
    public Map<String, TestSummary> generateSummaryByBrowser() {
        return testResults.stream()
                .filter(r -> r.getBrowser() != null)
                .collect(Collectors.groupingBy(TestResult::getBrowser))
                .entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> {
                        List<TestResult> browserResults = entry.getValue();
                        int total = browserResults.size();
                        int passed = (int) browserResults.stream().filter(TestResult::isPassed).count();
                        int failed = (int) browserResults.stream().filter(TestResult::isFailed).count();
                        int skipped = (int) browserResults.stream().filter(TestResult::isSkipped).count();
                        long totalDuration = browserResults.stream().mapToLong(TestResult::getDuration).sum();
                        List<TestResult> failedTests = browserResults.stream()
                                .filter(TestResult::isFailed)
                                .collect(Collectors.toList());
                        return new TestSummary(total, passed, failed, skipped, totalDuration, failedTests);
                    }
                ));
    }
    
    public List<TestResult> getTopFailures(int limit) {
        return testResults.stream()
                .filter(TestResult::isFailed)
                .sorted((a, b) -> Long.compare(b.getDuration(), a.getDuration()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    public List<TestResult> getSlowestTests(int limit) {
        return testResults.stream()
                .sorted((a, b) -> Long.compare(b.getDuration(), a.getDuration()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    public void exportToJson(String filePath) throws IOException {
        TestSummary summary = generateSummary();
        Map<String, Object> report = new HashMap<>();
        report.put("summary", summary);
        report.put("results", testResults);
        report.put("summaryByClass", generateSummaryByClass());
        report.put("summaryByBrowser", generateSummaryByBrowser());
        report.put("topFailures", getTopFailures(5));
        report.put("slowestTests", getSlowestTests(5));
        
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(filePath), report);
    }
    
    public void printSummary() {
        TestSummary summary = generateSummary();
        System.out.println("\n=== TEST EXECUTION SUMMARY ===");
        System.out.println(summary);
        
        if (summary.getFailedTests() > 0) {
            System.out.println("\n=== FAILED TESTS ===");
            summary.getFailedTestDetails().forEach(test -> 
                System.out.println("❌ " + test + " - " + test.getErrorMessage()));
        }
        
        System.out.println("\n=== SUMMARY BY CLASS ===");
        generateSummaryByClass().forEach((className, classSummary) ->
            System.out.println(className + ": " + classSummary));
        
        Map<String, TestSummary> browserSummary = generateSummaryByBrowser();
        if (!browserSummary.isEmpty()) {
            System.out.println("\n=== SUMMARY BY BROWSER ===");
            browserSummary.forEach((browser, summary2) ->
                System.out.println(browser + ": " + summary2));
        }
        
        List<TestResult> slowestTests = getSlowestTests(3);
        if (!slowestTests.isEmpty()) {
            System.out.println("\n=== SLOWEST TESTS ===");
            slowestTests.forEach(test -> 
                System.out.println("🐌 " + test));
        }
    }
    
    public void clear() {
        testResults.clear();
    }
    
    public List<TestResult> getAllResults() {
        return new ArrayList<>(testResults);
    }
}

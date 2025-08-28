package com.qa.advanced;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Demonstrates Java 8+ features for QA Automation
 * Focus: lambdas, streams, optional, functional interfaces
 */
public class Java8Features {

    // Sample test data
    private static List<TestResult> testResults = Arrays.asList(
            new TestResult("Login_Valid", "PASSED", 2.5, "Chrome"),
            new TestResult("Login_Invalid", "FAILED", 1.8, "Chrome"),
            new TestResult("Registration", "PASSED", 5.2, "Firefox"),
            new TestResult("Checkout", "PASSED", 8.1, "Chrome"),
            new TestResult("Search", "FAILED", 3.3, "Firefox"),
            new TestResult("Profile_Update", "PASSED", 4.7, "Chrome"),
            new TestResult("Password_Reset", "PASSED", 6.2, "Firefox"));

    public static void main(String[] args) {
        demonstrateLambdas();
        demonstrateStreams();
        demonstrateOptional();
        demonstrateFunctionalInterfaces();
        demonstrateCollectors();
        List<TestResult> testData = generateTestData(10);
        System.out.println(testData);
    }

    /**
     * Lambda expressions for cleaner code
     */
    public static void demonstrateLambdas() {
        System.out.println("=== No Lambdas ===");
        System.out.println("\n1. Filtering");
        for (TestResult test : testResults) {
            if ("PASSED".equals(test.getStatus())) {
                System.out.println("  " + test.getName());
            }
        }
        System.out.println("\n2. Sorting");
        testResults.sort(Comparator.comparing(TestResult::getExecutionTime));
        for (TestResult test : testResults) {
            System.out.println("  " + test.getName() + ": " + test.getExecutionTime() + "s");
        }
        System.out.println("");
        System.out.println("=== Lambda Expressions ===");

        // Traditional way vs Lambda way
        System.out.println("1. Filtering with Lambda:");

        // Lambda for filtering passed tests
        List<TestResult> passedTests = testResults.stream()
                .filter(test -> "PASSED".equals(test.getStatus()))
                .toList();

        passedTests.forEach(test -> System.out.println("  " + test.getName()));

        // Lambda for sorting by execution time
        System.out.println("\n2. Sorting with Lambda:");
        testResults.stream()
                .sorted((t1, t2) -> Double.compare(t1.getExecutionTime(), t2.getExecutionTime()))
                .forEach(test -> System.out.println("  " + test.getName() + ": " + test.getExecutionTime() + "s"));

        // Method reference (even cleaner)
        System.out.println("\n3. Method References:");
        testResults.stream()
                .map(TestResult::getName)
                .forEach(System.out::println);

        System.out.println();
    }

    /**
     * Stream API for data processing
     */
    public static void demonstrateStreams() {
        System.out.println("=== Stream API ===");

        // 1. Filter and collect
        System.out.println("1. Failed tests:");

        List<String> failedTestNames = testResults.stream()
                .filter(test -> "FAILED".equals(test.getStatus()))
                .map(TestResult::getName)
                .toList();
        failedTestNames.forEach(name -> System.out.println("  " + name));

        // 2. Group by browser
        System.out.println("\n2. Tests grouped by browser:");
        Map<String, List<TestResult>> testsByBrowser = testResults.stream()
                .collect(Collectors.groupingBy(TestResult::getBrowser));

        testsByBrowser.forEach((browser, tests) -> {
            System.out.println("  " + browser + ": " + tests.size() + " tests");
        });

        // 3. Statistics
        System.out.println("\n3. Execution time statistics:");
        DoubleSummaryStatistics stats = testResults.stream()
                .collect(Collectors.summarizingDouble(TestResult::getExecutionTime));
        // .mapToDouble(TestResult::getExecutionTime)
        // .summaryStatistics();

        System.out.println("  Average: " + String.format("%.2f", stats.getAverage()) + "s");
        System.out.println("  Min: " + stats.getMin() + "s");
        System.out.println("  Max: " + stats.getMax() + "s");
        System.out.println("  Total: " + String.format("%.2f", stats.getSum()) + "s");

        // 4. Parallel processing for large datasets
        System.out.println("\n4. Parallel processing example:");
        long count = testResults.parallelStream()
                .filter(test -> test.getExecutionTime() > 3.0)
                .count();
        System.out.println("  Tests taking more than 3 seconds: " + count);

        // 5. Complex filtering and transformation
        System.out.println("\n5. Complex operations:");
        List<String> slowFailedTests = testResults.stream()
                .filter(test -> "FAILED".equals(test.getStatus()))
                .filter(test -> test.getExecutionTime() > 2.0)
                .map(test -> test.getName() + " (" + test.getExecutionTime() + "s)")
                .toList();
        // .collect(Collectors.toList());

        System.out.println("  Slow failed tests: " + slowFailedTests);

        System.out.println();
    }

    /**
     * Optional for null safety
     */
    public static void demonstrateOptional() {
        System.out.println("=== Optional for Null Safety ===");

        // 1. Finding elements safely
        Optional<TestResult> fastestTest = testResults.stream()
                .min(Comparator.comparing(TestResult::getExecutionTime));

        fastestTest.ifPresent(test -> System.out
                .println("1. Fastest test: " + test.getName() + " (" + test.getExecutionTime() + "s)"));

        // 2. Safe navigation
        Optional<TestResult> specificTest = findTestByName("NonExistentTest");
        String testStatus = specificTest
                .map(TestResult::getStatus)
                .orElse("TEST_NOT_FOUND");
        System.out.println("2. Status of non-existent test: " + testStatus);

        // 3. Chaining operations
        Optional<String> browserOfLongestTest = testResults.stream()
                .max(Comparator.comparing(TestResult::getExecutionTime))
                .map(TestResult::getBrowser);

        browserOfLongestTest.ifPresent(browser -> System.out.println("3. Browser of longest test: " + browser));

        // 4. Optional with filtering
        Optional<TestResult> chromeFailedTest = testResults.stream()
                .filter(test -> "Chrome".equals(test.getBrowser()))
                .filter(test -> "FAILED".equals(test.getStatus()))
                .findFirst();

        System.out.println("4. Chrome failed test exists: " + chromeFailedTest.isPresent());

        System.out.println();
    }

    /**
     * Functional interfaces for flexible code
     */
    public static void demonstrateFunctionalInterfaces() {
        System.out.println("=== Functional Interfaces ===");

        // 1. Predicate for testing conditions
        Predicate<TestResult> isSlowTest = test -> test.getExecutionTime() > 5.0;
        Predicate<TestResult> isPassedTest = test -> "PASSED".equals(test.getStatus());

        // Combining predicates
        Predicate<TestResult> isSlowPassedTest = isSlowTest.and(isPassedTest);

        long slowPassedCount = testResults.stream()
                .filter(isSlowPassedTest)
                .count();
        System.out.println("1. Slow passed tests: " + slowPassedCount);

        // 2. Function for transformation
        Function<TestResult, String> testSummary = test -> test.getName() + " [" + test.getStatus() + "] - "
                + test.getExecutionTime() + "s";

        System.out.println("2. Test summaries:");
        testResults.stream()
                .map(testSummary)
                .forEach(summary -> System.out.println("  " + summary));

        // 3. Consumer for side effects
        Consumer<TestResult> logTestResult = test -> {
            if ("FAILED".equals(test.getStatus())) {
                System.out.println("  ALERT: " + test.getName() + " failed!");
            }
        };

        System.out.println("3. Processing with Consumer:");
        testResults.forEach(logTestResult);

        // 4. Supplier for lazy evaluation
        Supplier<List<TestResult>> failedTestsSupplier = () -> testResults.stream()
                .filter(test -> "FAILED".equals(test.getStatus()))
                .collect(Collectors.toList());

        System.out.println("4. Failed tests (lazy): " + failedTestsSupplier.get().size());

        System.out.println();
    }

    /**
     * Advanced collectors for data aggregation
     */
    public static void demonstrateCollectors() {
        System.out.println("=== Advanced Collectors ===");

        // 1. Partitioning
        Map<Boolean, List<TestResult>> partitionedTests = testResults.stream()
                .collect(Collectors.partitioningBy(test -> "PASSED".equals(test.getStatus())));

        System.out.println("1. Partitioned tests:");
        System.out.println("  Passed: " + partitionedTests.get(true).size());
        System.out.println("  Failed: " + partitionedTests.get(false).size());

        // 2. Grouping with downstream collectors
        Map<String, Double> avgTimeByBrowser = testResults.stream()
                .collect(Collectors.groupingBy(
                        TestResult::getBrowser,
                        Collectors.averagingDouble(TestResult::getExecutionTime)));

        System.out.println("2. Average execution time by browser:");
        avgTimeByBrowser.forEach(
                (browser, avgTime) -> System.out.println("  " + browser + ": " + String.format("%.2f", avgTime) + "s"));

        // 3. Custom collector for test statistics
        TestStatistics stats = testResults.stream()
                .collect(TestStatistics.collector());

        System.out.println("3. Custom test statistics:");
        System.out.println("  " + stats);

        // 4. Joining strings
        String testNames = testResults.stream()
                .map(TestResult::getName)
                .collect(Collectors.joining(", ", "Tests: [", "]"));

        System.out.println("4. All test names: " + testNames);

        System.out.println();
    }

    // Helper method for Optional demonstration
    private static Optional<TestResult> findTestByName(String name) {
        return testResults.stream()
                .filter(test -> test.getName().equals(name))
                .findFirst();
    }

    // Utility methods for creating test data
    public static List<TestResult> generateTestData(int count) {
        Random random = new Random();
        String[] browsers = { "Chrome", "Firefox", "Safari", "Edge" };
        String[] statuses = { "PASSED", "FAILED" };

        return Stream.generate(() -> new TestResult(
                "Test_" + random.nextInt(1000),
                statuses[random.nextInt(statuses.length)],
                1.0 + random.nextDouble() * 10.0,
                browsers[random.nextInt(browsers.length)])).limit(count).toList();
    }
}

// Supporting classes
class TestResult {
    private String name;
    private String status;
    private double executionTime;
    private String browser;

    public TestResult(String name, String status, double executionTime, String browser) {
        this.name = name;
        this.status = status;
        this.executionTime = executionTime;
        this.browser = browser;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public double getExecutionTime() {
        return executionTime;
    }

    public String getBrowser() {
        return browser;
    }

    @Override
    public String toString() {
        return String.format("\nTestResult{name='%s', status='%s', time=%.2f, browser='%s'}",
                name, status, executionTime, browser);
    }
}

// Custom collector example
class TestStatistics {
    private int totalTests;
    private int passedTests;
    private int failedTests;
    private double totalExecutionTime;

    public TestStatistics() {
    }

    public void addTest(TestResult test) {
        totalTests++;
        if ("PASSED".equals(test.getStatus())) {
            passedTests++;
        } else {
            failedTests++;
        }
        totalExecutionTime += test.getExecutionTime();
    }

    public TestStatistics combine(TestStatistics other) {
        TestStatistics combined = new TestStatistics();
        combined.totalTests = this.totalTests + other.totalTests;
        combined.passedTests = this.passedTests + other.passedTests;
        combined.failedTests = this.failedTests + other.failedTests;
        combined.totalExecutionTime = this.totalExecutionTime + other.totalExecutionTime;
        return combined;
    }

    public static Collector<TestResult, TestStatistics, TestStatistics> collector() {
        return Collector.of(
                TestStatistics::new,
                TestStatistics::addTest,
                TestStatistics::combine);
    }

    @Override
    public String toString() {
        double passRate = totalTests > 0 ? (passedTests * 100.0 / totalTests) : 0;
        double avgTime = totalTests > 0 ? (totalExecutionTime / totalTests) : 0;

        return String.format("Total: %d, Passed: %d, Failed: %d, Pass Rate: %.1f%%, Avg Time: %.2fs",
                totalTests, passedTests, failedTests, passRate, avgTime);
    }
}

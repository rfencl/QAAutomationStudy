package com.qa.monitoring;

import com.qa.monitoring.TestMonitoringSystem.*;
import org.testng.annotations.*;
import static org.testng.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Integration tests for the complete dashboard system
 */
public class DashboardIntegrationTest {
    
    private TestMonitoringDashboard dashboard;
    
    @BeforeMethod
    public void setUp() {
        dashboard = new TestMonitoringDashboard();
    }
    
    @Test
    public void testCompleteWorkflow() {
        // Setup alert rules
        setupAlertRules();
        
        // Setup dashboard subscriber
        List<DashboardUpdate> updates = new ArrayList<>();
        dashboard.subscribe(updates::add);
        
        // Setup alert listener
        List<Alert> alerts = new ArrayList<>();
        dashboard.addAlertListener(alerts::add);
        
        // Simulate a test execution session
        simulateTestExecution();
        
        // Verify we received updates
        assertTrue(updates.size() > 0);
        
        // Verify metrics are calculated correctly
        DashboardMetrics metrics = dashboard.getCurrentMetrics("e-commerce-app");
        assertTrue(metrics.getTotalTests() > 0);
        assertTrue(metrics.getPassRate() >= 0 && metrics.getPassRate() <= 100);
        
        // Verify alerts were triggered if conditions met
        // (depends on the specific test results generated)
        
        System.out.println("Final Metrics:");
        System.out.println("Total Tests: " + metrics.getTotalTests());
        System.out.println("Pass Rate: " + metrics.getPassRate() + "%");
        System.out.println("Failure Rate: " + metrics.getFailureRate() + "%");
        System.out.println("Average Execution Time: " + metrics.getAverageExecutionTime() + "ms");
        System.out.println("Test Velocity: " + metrics.getTestVelocity() + " tests/hour");
        System.out.println("Alerts Triggered: " + alerts.size());
    }
    
    @Test
    public void testRealTimeUpdates() throws InterruptedException {
        CountDownLatch updateLatch = new CountDownLatch(5);
        List<DashboardUpdate> receivedUpdates = new ArrayList<>();
        
        // Subscribe to updates
        dashboard.subscribe(update -> {
            receivedUpdates.add(update);
            updateLatch.countDown();
        });
        
        // Publish test results with delays to simulate real-time
        new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    TestResult result = createTestResult("test-" + i, "RealTimeTest" + i, 
                                                       i % 2 == 0 ? TestStatus.PASSED : TestStatus.FAILED);
                    dashboard.publishTestResult(result);
                    Thread.sleep(100); // Small delay between tests
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        // Wait for all updates
        assertTrue(updateLatch.await(5, TimeUnit.SECONDS));
        
        // Verify updates were received in order
        assertEquals(receivedUpdates.size(), 5);
        for (int i = 0; i < 5; i++) {
            assertEquals(receivedUpdates.get(i).getTestResult().getTestName(), "RealTimeTest" + (i + 1));
        }
    }
    
    @Test
    public void testMultiEnvironmentMonitoring() {
        String[] environments = {"dev", "staging", "production"};
        
        // Publish tests across different environments
        for (String env : environments) {
            for (int i = 1; i <= 3; i++) {
                TestResult result = TestResult.builder()
                    .testId(env + "-test-" + i)
                    .testName("Test" + i)
                    .projectId("multi-env-project")
                    .environment(env)
                    .status(i % 2 == 0 ? TestStatus.PASSED : TestStatus.FAILED)
                    .startTime(Instant.now().minus(Duration.ofSeconds(10)))
                    .endTime(Instant.now())
                    .build();
                dashboard.publishTestResult(result);
            }
        }
        
        // Verify environment metrics
        DashboardMetrics metrics = dashboard.getCurrentMetrics("multi-env-project");
        Map<String, Integer> envMetrics = metrics.getEnvironmentMetrics();
        
        assertEquals(envMetrics.size(), 3);
        for (String env : environments) {
            assertEquals(envMetrics.get(env).intValue(), 3);
        }
    }
    
    @Test
    public void testFailureAnalysis() {
        // Create various failure scenarios
        publishFailedTest("test-001", "LoginTest", "Authentication failed");
        publishFailedTest("test-002", "LoginTest", "Timeout waiting for element");
        publishFailedTest("test-003", "LoginTest", "Invalid credentials");
        publishFailedTest("test-004", "CheckoutTest", "Payment processing failed");
        publishFailedTest("test-005", "SearchTest", "No results found");
        publishFailedTest("test-006", "SearchTest", "Search timeout");
        
        DashboardMetrics metrics = dashboard.getCurrentMetrics("failure-analysis-project");
        List<TopFailure> topFailures = metrics.getTopFailures();
        
        // LoginTest should be the top failure (3 occurrences)
        assertEquals(topFailures.get(0).getTestName(), "LoginTest");
        assertEquals(topFailures.get(0).getFailureCount(), 3);
        
        // SearchTest should be second (2 occurrences)
        assertEquals(topFailures.get(1).getTestName(), "SearchTest");
        assertEquals(topFailures.get(1).getFailureCount(), 2);
    }
    
    @Test
    public void testPerformanceMetrics() {
        // Create tests with varying execution times
        publishTestWithExecutionTime("fast-test-1", 100); // 100ms
        publishTestWithExecutionTime("fast-test-2", 150); // 150ms
        publishTestWithExecutionTime("medium-test-1", 500); // 500ms
        publishTestWithExecutionTime("slow-test-1", 2000); // 2s
        publishTestWithExecutionTime("slow-test-2", 3000); // 3s
        
        DashboardMetrics metrics = dashboard.getCurrentMetrics("performance-project");
        
        // Average should be around 1150ms
        double avgTime = metrics.getAverageExecutionTime();
        assertTrue(avgTime > 1000 && avgTime < 1300, "Average execution time: " + avgTime);
    }
    
    @Test
    public void testAlertEscalation() {
        List<Alert> alerts = new ArrayList<>();
        dashboard.addAlertListener(alerts::add);
        
        // Add critical failure rate alert
        AlertRule criticalRule = new AlertRule(
            "critical-001",
            "Critical Failure Rate",
            "critical-project",
            AlertType.FAILURE_RATE,
            80.0, // 80% threshold
            AlertSeverity.CRITICAL,
            true
        );
        dashboard.addAlertRule(criticalRule);
        
        // Publish mostly failing tests
        for (int i = 1; i <= 10; i++) {
            TestStatus status = i <= 9 ? TestStatus.FAILED : TestStatus.PASSED; // 90% failure rate
            publishTestForProject("test-" + i, "Test" + i, status, "critical-project");
        }
        
        // Should trigger critical alert
        assertTrue(alerts.size() >= 1, "Expected at least 1 alert, got: " + alerts.size());
        Alert lastAlert = alerts.get(alerts.size() - 1); // Get the last alert
        assertEquals(lastAlert.getSeverity(), AlertSeverity.CRITICAL);
        assertTrue(lastAlert.getMessage().contains("80"));
    }
    
    // Helper methods
    private void setupAlertRules() {
        // High failure rate alert
        AlertRule failureRateRule = new AlertRule(
            "rule-001",
            "High Failure Rate",
            "e-commerce-app",
            AlertType.FAILURE_RATE,
            25.0,
            AlertSeverity.HIGH,
            true
        );
        dashboard.addAlertRule(failureRateRule);
        
        // Slow execution alert
        AlertRule slowExecutionRule = new AlertRule(
            "rule-002",
            "Slow Test Execution",
            "e-commerce-app",
            AlertType.EXECUTION_TIME,
            5000.0,
            AlertSeverity.MEDIUM,
            true
        );
        dashboard.addAlertRule(slowExecutionRule);
    }
    
    private void simulateTestExecution() {
        String[] testNames = {
            "LoginTest", "LogoutTest", "SearchTest", "CheckoutTest", 
            "ProfileTest", "CartTest", "PaymentTest", "InventoryTest"
        };
        
        String[] environments = {"staging", "production"};
        
        for (String testName : testNames) {
            for (String env : environments) {
                TestStatus status = Math.random() > 0.2 ? TestStatus.PASSED : TestStatus.FAILED; // 80% pass rate
                long executionTime = (long) (Math.random() * 3000 + 500); // 500-3500ms
                
                TestResult result = TestResult.builder()
                    .testId(UUID.randomUUID().toString())
                    .testName(testName)
                    .projectId("e-commerce-app")
                    .environment(env)
                    .status(status)
                    .startTime(Instant.now().minus(Duration.ofMillis(executionTime)))
                    .endTime(Instant.now())
                    .errorMessage(status == TestStatus.FAILED ? "Simulated test failure" : null)
                    .build();
                
                dashboard.publishTestResult(result);
            }
        }
    }
    
    private TestResult createTestResult(String testId, String testName, TestStatus status) {
        return TestResult.builder()
            .testId(testId)
            .testName(testName)
            .projectId("integration-test-project")
            .environment("test")
            .status(status)
            .startTime(Instant.now().minus(Duration.ofSeconds(2)))
            .endTime(Instant.now())
            .errorMessage(status == TestStatus.FAILED ? "Integration test failure" : null)
            .build();
    }
    
    private void publishFailedTest(String testId, String testName, String errorMessage) {
        TestResult result = TestResult.builder()
            .testId(testId)
            .testName(testName)
            .projectId("failure-analysis-project")
            .environment("test")
            .status(TestStatus.FAILED)
            .startTime(Instant.now().minus(Duration.ofSeconds(5)))
            .endTime(Instant.now())
            .errorMessage(errorMessage)
            .build();
        dashboard.publishTestResult(result);
    }
    
    private void publishTestWithExecutionTime(String testName, long executionTimeMs) {
        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(Duration.ofMillis(executionTimeMs));
        
        TestResult result = TestResult.builder()
            .testId(UUID.randomUUID().toString())
            .testName(testName)
            .projectId("performance-project")
            .environment("test")
            .status(TestStatus.PASSED)
            .startTime(startTime)
            .endTime(endTime)
            .build();
        dashboard.publishTestResult(result);
    }
    
    private void publishTestForProject(String testId, String testName, TestStatus status, String projectId) {
        TestResult result = TestResult.builder()
            .testId(testId)
            .testName(testName)
            .projectId(projectId)
            .environment("test")
            .status(status)
            .startTime(Instant.now().minus(Duration.ofSeconds(2)))
            .endTime(Instant.now())
            .errorMessage(status == TestStatus.FAILED ? "Test failure" : null)
            .build();
        dashboard.publishTestResult(result);
    }
}

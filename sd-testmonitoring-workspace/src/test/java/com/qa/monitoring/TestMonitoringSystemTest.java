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
 * Comprehensive test suite for Test Monitoring System
 */
public class TestMonitoringSystemTest {
    
    private TestMonitoringDashboard dashboard;
    private List<DashboardUpdate> receivedUpdates;
    private List<Alert> receivedAlerts;
    
    @BeforeMethod
    public void setUp() {
        dashboard = new TestMonitoringDashboard();
        receivedUpdates = new ArrayList<>();
        receivedAlerts = new ArrayList<>();
        
        // Subscribe to updates
        dashboard.subscribe(update -> receivedUpdates.add(update));
        
        // Subscribe to alerts
        dashboard.addAlertListener(alert -> receivedAlerts.add(alert));
    }
    
    @Test
    public void testBasicTestResultPublishing() {
        TestResult testResult = TestResult.builder()
            .testId("test-001")
            .testName("LoginTest")
            .projectId("project-alpha")
            .environment("staging")
            .status(TestStatus.PASSED)
            .startTime(Instant.now().minus(Duration.ofSeconds(30)))
            .endTime(Instant.now())
            .build();
        
        dashboard.publishTestResult(testResult);
        
        // Verify update was received
        assertEquals(receivedUpdates.size(), 1);
        DashboardUpdate update = receivedUpdates.get(0);
        assertEquals(update.getProjectId(), "project-alpha");
        assertEquals(update.getTestResult().getTestName(), "LoginTest");
        assertEquals(update.getTestResult().getStatus(), TestStatus.PASSED);
        
        // Verify metrics were calculated
        assertNotNull(update.getMetrics());
        assertEquals(update.getMetrics().getTotalTests(), 1);
        assertEquals(update.getMetrics().getPassRate(), 100.0);
        assertEquals(update.getMetrics().getFailureRate(), 0.0);
    }
    
    @Test
    public void testMetricsCalculation() {
        // Publish multiple test results
        publishTestResult("test-001", "LoginTest", TestStatus.PASSED);
        publishTestResult("test-002", "LogoutTest", TestStatus.PASSED);
        publishTestResult("test-003", "SearchTest", TestStatus.FAILED);
        publishTestResult("test-004", "CheckoutTest", TestStatus.FAILED);
        publishTestResult("test-005", "ProfileTest", TestStatus.SKIPPED);
        
        DashboardMetrics metrics = dashboard.getCurrentMetrics("project-alpha");
        
        assertEquals(metrics.getTotalTests(), 5);
        assertEquals(metrics.getPassRate(), 40.0); // 2 out of 5
        assertEquals(metrics.getFailureRate(), 40.0); // 2 out of 5
        assertTrue(metrics.getAverageExecutionTime() > 0);
        
        // Verify top failures
        List<TopFailure> topFailures = metrics.getTopFailures();
        assertEquals(topFailures.size(), 2); // SearchTest and CheckoutTest
        
        // Verify environment metrics
        Map<String, Integer> envMetrics = metrics.getEnvironmentMetrics();
        assertEquals(envMetrics.get("staging").intValue(), 5);
    }
    
    @Test
    public void testAlertingSystem() {
        // Add failure rate alert rule
        AlertRule failureRateRule = new AlertRule(
            "rule-001",
            "High Failure Rate",
            "project-alpha",
            AlertType.FAILURE_RATE,
            30.0, // 30% threshold
            AlertSeverity.HIGH,
            true
        );
        dashboard.addAlertRule(failureRateRule);
        
        // Publish tests that exceed failure rate threshold
        publishTestResult("test-001", "Test1", TestStatus.FAILED);
        publishTestResult("test-002", "Test2", TestStatus.FAILED);
        publishTestResult("test-003", "Test3", TestStatus.PASSED);
        
        // Should trigger alert (66% failure rate > 30% threshold)
        assertTrue(receivedAlerts.size() >= 1, "Expected at least 1 alert, got: " + receivedAlerts.size());
        Alert alert = receivedAlerts.get(receivedAlerts.size() - 1); // Get the last alert
        assertEquals(alert.getProjectId(), "project-alpha");
        assertEquals(alert.getSeverity(), AlertSeverity.HIGH);
        assertTrue(alert.getMessage().contains("Failure rate"));
    }
    
    @Test
    public void testExecutionTimeAlert() {
        // Add execution time alert rule
        AlertRule executionTimeRule = new AlertRule(
            "rule-002",
            "Slow Test Execution",
            "project-alpha",
            AlertType.EXECUTION_TIME,
            5000.0, // 5 seconds threshold
            AlertSeverity.MEDIUM,
            true
        );
        dashboard.addAlertRule(executionTimeRule);
        
        // Publish slow test
        TestResult slowTest = TestResult.builder()
            .testId("test-slow")
            .testName("SlowTest")
            .projectId("project-alpha")
            .environment("staging")
            .status(TestStatus.PASSED)
            .startTime(Instant.now().minus(Duration.ofSeconds(10)))
            .endTime(Instant.now())
            .build();
        
        dashboard.publishTestResult(slowTest);
        
        // Should trigger alert (10 seconds > 5 seconds threshold)
        assertTrue(receivedAlerts.size() >= 1, "Expected at least 1 alert, got: " + receivedAlerts.size());
        Alert alert = receivedAlerts.get(receivedAlerts.size() - 1); // Get the last alert
        assertEquals(alert.getSeverity(), AlertSeverity.MEDIUM);
        assertTrue(alert.getMessage().contains("execution time"));
    }
    
    @Test
    public void testMultipleProjectIsolation() {
        // Publish tests for different projects
        publishTestResultForProject("test-001", "Test1", TestStatus.PASSED, "project-alpha");
        publishTestResultForProject("test-002", "Test2", TestStatus.FAILED, "project-alpha");
        publishTestResultForProject("test-003", "Test3", TestStatus.PASSED, "project-beta");
        
        // Verify project isolation
        DashboardMetrics alphaMetrics = dashboard.getCurrentMetrics("project-alpha");
        DashboardMetrics betaMetrics = dashboard.getCurrentMetrics("project-beta");
        
        assertEquals(alphaMetrics.getTotalTests(), 2);
        assertEquals(alphaMetrics.getPassRate(), 50.0);
        
        assertEquals(betaMetrics.getTotalTests(), 1);
        assertEquals(betaMetrics.getPassRate(), 100.0);
    }
    
    @Test
    public void testConcurrentTestPublishing() throws InterruptedException {
        int numberOfThreads = 10;
        int testsPerThread = 5;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        
        // Publish tests concurrently
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    for (int j = 0; j < testsPerThread; j++) {
                        publishTestResult(
                            "test-" + threadId + "-" + j,
                            "ConcurrentTest" + threadId + "_" + j,
                            j % 2 == 0 ? TestStatus.PASSED : TestStatus.FAILED
                        );
                        Thread.sleep(10); // Small delay
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        // Wait for all threads to complete
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        
        // Verify all tests were processed
        DashboardMetrics metrics = dashboard.getCurrentMetrics("project-alpha");
        assertEquals(metrics.getTotalTests(), numberOfThreads * testsPerThread);
        
        // Verify we received all updates
        assertEquals(receivedUpdates.size(), numberOfThreads * testsPerThread);
    }
    
    @Test
    public void testJsonExport() {
        // Publish some test results
        publishTestResult("test-001", "Test1", TestStatus.PASSED);
        publishTestResult("test-002", "Test2", TestStatus.FAILED);
        
        String json = dashboard.exportMetricsAsJson("project-alpha");
        
        assertNotNull(json);
        assertFalse(json.isEmpty());
        assertTrue(json.contains("totalTests"));
        assertTrue(json.contains("passRate"));
        assertTrue(json.contains("failureRate"));
    }
    
    @Test
    public void testTopFailuresCalculation() {
        // Create multiple failures for the same test
        publishTestResult("test-001", "FlakyTest", TestStatus.FAILED);
        publishTestResult("test-002", "FlakyTest", TestStatus.FAILED);
        publishTestResult("test-003", "FlakyTest", TestStatus.FAILED);
        publishTestResult("test-004", "StableTest", TestStatus.FAILED);
        publishTestResult("test-005", "AnotherTest", TestStatus.PASSED);
        
        DashboardMetrics metrics = dashboard.getCurrentMetrics("project-alpha");
        List<TopFailure> topFailures = metrics.getTopFailures();
        
        assertEquals(topFailures.size(), 2);
        
        // FlakyTest should be first (3 failures)
        TopFailure topFailure = topFailures.get(0);
        assertEquals(topFailure.getTestName(), "FlakyTest");
        assertEquals(topFailure.getFailureCount(), 3);
        
        // StableTest should be second (1 failure)
        TopFailure secondFailure = topFailures.get(1);
        assertEquals(secondFailure.getTestName(), "StableTest");
        assertEquals(secondFailure.getFailureCount(), 1);
    }
    
    @Test
    public void testTestVelocityCalculation() {
        Instant now = Instant.now();
        
        // Publish tests within the last hour
        for (int i = 0; i < 5; i++) {
            TestResult recentTest = TestResult.builder()
                .testId("recent-" + i)
                .testName("RecentTest" + i)
                .projectId("project-alpha")
                .environment("staging")
                .status(TestStatus.PASSED)
                .startTime(now.minus(Duration.ofMinutes(30)))
                .endTime(now.minus(Duration.ofMinutes(29)))
                .build();
            dashboard.publishTestResult(recentTest);
        }
        
        // Publish tests older than one hour
        for (int i = 0; i < 3; i++) {
            TestResult oldTest = TestResult.builder()
                .testId("old-" + i)
                .testName("OldTest" + i)
                .projectId("project-alpha")
                .environment("staging")
                .status(TestStatus.PASSED)
                .startTime(now.minus(Duration.ofHours(2)))
                .endTime(now.minus(Duration.ofHours(2).minus(Duration.ofMinutes(1))))
                .build();
            dashboard.publishTestResult(oldTest);
        }
        
        DashboardMetrics metrics = dashboard.getCurrentMetrics("project-alpha");
        
        // Test velocity should only count recent tests (last hour)
        assertEquals(metrics.getTestVelocity(), 5);
        assertEquals(metrics.getTotalTests(), 8); // All tests
    }
    
    // Helper methods
    private void publishTestResult(String testId, String testName, TestStatus status) {
        publishTestResultForProject(testId, testName, status, "project-alpha");
    }
    
    private void publishTestResultForProject(String testId, String testName, TestStatus status, String projectId) {
        TestResult testResult = TestResult.builder()
            .testId(testId)
            .testName(testName)
            .projectId(projectId)
            .environment("staging")
            .status(status)
            .startTime(Instant.now().minus(Duration.ofSeconds(5)))
            .endTime(Instant.now())
            .errorMessage(status == TestStatus.FAILED ? "Test failed due to assertion error" : null)
            .build();
        
        dashboard.publishTestResult(testResult);
    }
}

package com.qa.monitoring;

import com.qa.monitoring.TestMonitoringSystem.*;
import org.testng.annotations.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Demonstration of the Test Monitoring Dashboard System
 * Shows real-world usage scenarios and capabilities
 */
public class DashboardDemoTest {
    
    private TestMonitoringDashboard dashboard;
    
    @BeforeMethod
    public void setUp() {
        dashboard = new TestMonitoringDashboard();
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST MONITORING DASHBOARD DEMONSTRATION");
        System.out.println("=".repeat(80));
    }
    
    @Test
    public void demonstrateRealTimeMonitoring() {
        System.out.println("\n🔴 DEMO: Real-time Test Monitoring");
        System.out.println("-".repeat(50));
        
        // Setup dashboard subscriber to show real-time updates
        List<DashboardUpdate> updates = new ArrayList<>();
        dashboard.subscribe(update -> {
            System.out.printf("📊 REAL-TIME UPDATE: %s [%s] - %s (Pass Rate: %.1f%%)\n",
                update.getTestResult().getTestName(),
                update.getTestResult().getEnvironment(),
                update.getTestResult().getStatus(),
                update.getMetrics().getPassRate()
            );
            updates.add(update);
        });
        
        // Simulate test execution
        String projectId = "e-commerce-platform";
        simulateTestSuite(projectId, "production");
        
        // Show final metrics
        DashboardMetrics metrics = dashboard.getCurrentMetrics(projectId);
        System.out.println("\n📈 FINAL METRICS:");
        System.out.printf("   Total Tests: %d\n", metrics.getTotalTests());
        System.out.printf("   Pass Rate: %.1f%%\n", metrics.getPassRate());
        System.out.printf("   Failure Rate: %.1f%%\n", metrics.getFailureRate());
        System.out.printf("   Avg Execution Time: %.0fms\n", metrics.getAverageExecutionTime());
        System.out.printf("   Test Velocity: %d tests/hour\n", metrics.getTestVelocity());
        
        System.out.printf("\n✅ Received %d real-time updates\n", updates.size());
    }
    
    @Test
    public void demonstrateAlertingSystem() {
        System.out.println("\n🚨 DEMO: Intelligent Alerting System");
        System.out.println("-".repeat(50));
        
        String projectId = "critical-banking-app";
        List<Alert> alerts = new ArrayList<>();
        
        // Setup alert listener
        dashboard.addAlertListener(alert -> {
            System.out.printf("🚨 ALERT [%s]: %s\n", 
                alert.getSeverity(), alert.getMessage());
            alerts.add(alert);
        });
        
        // Configure alert rules
        setupAlertRules(projectId);
        
        // Simulate problematic test execution
        simulateProblematicTests(projectId);
        
        System.out.printf("\n📊 ALERT SUMMARY: %d alerts triggered\n", alerts.size());
        alerts.forEach(alert -> 
            System.out.printf("   - %s: %s\n", alert.getSeverity(), alert.getMessage())
        );
    }
    
    @Test
    public void demonstrateMultiProjectMonitoring() {
        System.out.println("\n🏢 DEMO: Multi-Project Monitoring");
        System.out.println("-".repeat(50));
        
        String[] projects = {"web-frontend", "mobile-app", "api-backend"};
        String[] environments = {"dev", "staging", "production"};
        
        // Monitor multiple projects simultaneously
        for (String project : projects) {
            for (String env : environments) {
                simulateTestSuite(project, env);
                
                DashboardMetrics metrics = dashboard.getCurrentMetrics(project);
                System.out.printf("📊 %s: %d tests, %.1f%% pass rate\n", 
                    project, metrics.getTotalTests(), metrics.getPassRate());
            }
        }
        
        System.out.println("\n✅ Successfully monitored " + projects.length + " projects across " + 
                          environments.length + " environments");
    }
    
    @Test
    public void demonstrateFailureAnalysis() {
        System.out.println("\n🔍 DEMO: Advanced Failure Analysis");
        System.out.println("-".repeat(50));
        
        String projectId = "qa-automation-suite";
        
        // Create various failure scenarios
        simulateFailureScenarios(projectId);
        
        DashboardMetrics metrics = dashboard.getCurrentMetrics(projectId);
        
        System.out.println("\n🔍 TOP FAILING TESTS:");
        metrics.getTopFailures().forEach(failure -> 
            System.out.printf("   %s: %d failures (Last: %s)\n", 
                failure.getTestName(), 
                failure.getFailureCount(), 
                failure.getLastError())
        );
        
        System.out.println("\n🌍 ENVIRONMENT BREAKDOWN:");
        metrics.getEnvironmentMetrics().forEach((env, count) -> 
            System.out.printf("   %s: %d tests\n", env, count)
        );
    }
    
    @Test
    public void demonstratePerformanceMonitoring() {
        System.out.println("\n⚡ DEMO: Performance Monitoring");
        System.out.println("-".repeat(50));
        
        String projectId = "performance-suite";
        
        // Create tests with varying performance characteristics
        simulatePerformanceTests(projectId);
        
        DashboardMetrics metrics = dashboard.getCurrentMetrics(projectId);
        
        System.out.printf("⚡ PERFORMANCE METRICS:\n");
        System.out.printf("   Average Execution Time: %.0fms\n", metrics.getAverageExecutionTime());
        System.out.printf("   Test Velocity: %d tests/hour\n", metrics.getTestVelocity());
        
        // Export metrics as JSON
        String jsonMetrics = dashboard.exportMetricsAsJson(projectId);
        System.out.println("\n📄 JSON EXPORT (first 200 chars):");
        System.out.println(jsonMetrics.substring(0, Math.min(200, jsonMetrics.length())) + "...");
    }
    
    // Helper methods for simulation
    private void simulateTestSuite(String projectId, String environment) {
        String[] testNames = {
            "LoginTest", "LogoutTest", "SearchTest", "CheckoutTest", "PaymentTest"
        };
        
        for (String testName : testNames) {
            TestStatus status = Math.random() > 0.15 ? TestStatus.PASSED : TestStatus.FAILED; // 85% pass rate
            long executionTime = (long) (Math.random() * 2000 + 500); // 500-2500ms
            
            TestResult result = createTestResult(testName, projectId, environment, status, executionTime);
            dashboard.publishTestResult(result);
            
            // Small delay to simulate real execution
            try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
    
    private void setupAlertRules(String projectId) {
        // High failure rate alert
        AlertRule failureRateRule = new AlertRule(
            UUID.randomUUID().toString(),
            "Critical Failure Rate",
            projectId,
            AlertType.FAILURE_RATE,
            20.0, // 20% threshold
            AlertSeverity.CRITICAL,
            true
        );
        dashboard.addAlertRule(failureRateRule);
        
        // Slow execution alert
        AlertRule slowExecutionRule = new AlertRule(
            UUID.randomUUID().toString(),
            "Slow Test Execution",
            projectId,
            AlertType.EXECUTION_TIME,
            3000.0, // 3 seconds threshold
            AlertSeverity.HIGH,
            true
        );
        dashboard.addAlertRule(slowExecutionRule);
        
        System.out.println("🔧 Configured alert rules: Failure Rate (20%) and Execution Time (3s)");
    }
    
    private void simulateProblematicTests(String projectId) {
        // Create failing tests
        for (int i = 1; i <= 3; i++) {
            TestResult failedTest = createTestResult(
                "CriticalTest" + i, projectId, "production", 
                TestStatus.FAILED, 1000
            );
            dashboard.publishTestResult(failedTest);
        }
        
        // Create slow test
        TestResult slowTest = createTestResult(
            "SlowIntegrationTest", projectId, "production", 
            TestStatus.PASSED, 5000 // 5 seconds - exceeds threshold
        );
        dashboard.publishTestResult(slowTest);
        
        // Create one passing test
        TestResult passingTest = createTestResult(
            "FastUnitTest", projectId, "production", 
            TestStatus.PASSED, 500
        );
        dashboard.publishTestResult(passingTest);
    }
    
    private void simulateFailureScenarios(String projectId) {
        // Flaky test - multiple failures
        for (int i = 0; i < 4; i++) {
            TestResult flakyTest = createTestResult(
                "FlakyUITest", projectId, "staging", 
                TestStatus.FAILED, 2000
            );
            flakyTest.getMetadata().put("errorType", "ElementNotFound");
            dashboard.publishTestResult(flakyTest);
        }
        
        // Environment-specific failures
        TestResult prodFailure = createTestResult(
            "DatabaseConnectionTest", projectId, "production", 
            TestStatus.FAILED, 1500
        );
        dashboard.publishTestResult(prodFailure);
        
        TestResult devFailure = createTestResult(
            "ConfigurationTest", projectId, "development", 
            TestStatus.FAILED, 800
        );
        dashboard.publishTestResult(devFailure);
        
        // Some passing tests
        for (int i = 0; i < 3; i++) {
            TestResult passingTest = createTestResult(
                "StableTest" + i, projectId, "staging", 
                TestStatus.PASSED, 1000
            );
            dashboard.publishTestResult(passingTest);
        }
    }
    
    private void simulatePerformanceTests(String projectId) {
        // Fast tests
        for (int i = 0; i < 5; i++) {
            TestResult fastTest = createTestResult(
                "UnitTest" + i, projectId, "test", 
                TestStatus.PASSED, 100 + (i * 50)
            );
            dashboard.publishTestResult(fastTest);
        }
        
        // Medium tests
        for (int i = 0; i < 3; i++) {
            TestResult mediumTest = createTestResult(
                "IntegrationTest" + i, projectId, "test", 
                TestStatus.PASSED, 1000 + (i * 500)
            );
            dashboard.publishTestResult(mediumTest);
        }
        
        // Slow tests
        for (int i = 0; i < 2; i++) {
            TestResult slowTest = createTestResult(
                "E2ETest" + i, projectId, "test", 
                TestStatus.PASSED, 3000 + (i * 1000)
            );
            dashboard.publishTestResult(slowTest);
        }
    }
    
    private TestResult createTestResult(String testName, String projectId, String environment, 
                                      TestStatus status, long executionTimeMs) {
        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(Duration.ofMillis(executionTimeMs));
        
        return TestResult.builder()
            .testId(UUID.randomUUID().toString())
            .testName(testName)
            .projectId(projectId)
            .environment(environment)
            .status(status)
            .startTime(startTime)
            .endTime(endTime)
            .errorMessage(status == TestStatus.FAILED ? "Simulated test failure" : null)
            .build();
    }
}

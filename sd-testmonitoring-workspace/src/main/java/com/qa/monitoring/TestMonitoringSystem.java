package com.qa.monitoring;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Real-time Test Monitoring Dashboard System
 * 
 * Design Decisions:
 * 1. Event-driven architecture for real-time updates
 * 2. In-memory storage for demonstration (production would use Redis/InfluxDB)
 * 3. Observer pattern for dashboard subscriptions
 * 4. Concurrent collections for thread-safe operations
 * 5. Builder pattern for complex objects
 */
public class TestMonitoringSystem {
    
    private static final Logger logger = LoggerFactory.getLogger(TestMonitoringSystem.class);
    
    // Core Models
    public static class TestResult {
        private String testId;
        private String testName;
        private String projectId;
        private String environment;
        private TestStatus status;
        private Instant startTime;
        private Instant endTime;
        private Duration executionTime;
        private String errorMessage;
        private Map<String, Object> metadata;
        
        public TestResult(String testId, String testName, String projectId, String environment, 
                         TestStatus status, Instant startTime, Instant endTime, String errorMessage) {
            this.testId = testId;
            this.testName = testName;
            this.projectId = projectId;
            this.environment = environment;
            this.status = status;
            this.startTime = startTime;
            this.endTime = endTime;
            this.executionTime = endTime != null ? Duration.between(startTime, endTime) : null;
            this.errorMessage = errorMessage;
            this.metadata = new HashMap<>();
        }
        
        // Getters
        public String getTestId() { return testId; }
        public String getTestName() { return testName; }
        public String getProjectId() { return projectId; }
        public String getEnvironment() { return environment; }
        public TestStatus getStatus() { return status; }
        public Instant getStartTime() { return startTime; }
        public Instant getEndTime() { return endTime; }
        public Duration getExecutionTime() { return executionTime; }
        public String getErrorMessage() { return errorMessage; }
        public Map<String, Object> getMetadata() { return metadata; }
        
        public static TestResultBuilder builder() {
            return new TestResultBuilder();
        }
        
        public static class TestResultBuilder {
            private String testId;
            private String testName;
            private String projectId;
            private String environment;
            private TestStatus status;
            private Instant startTime;
            private Instant endTime;
            private String errorMessage;
            
            public TestResultBuilder testId(String testId) { this.testId = testId; return this; }
            public TestResultBuilder testName(String testName) { this.testName = testName; return this; }
            public TestResultBuilder projectId(String projectId) { this.projectId = projectId; return this; }
            public TestResultBuilder environment(String environment) { this.environment = environment; return this; }
            public TestResultBuilder status(TestStatus status) { this.status = status; return this; }
            public TestResultBuilder startTime(Instant startTime) { this.startTime = startTime; return this; }
            public TestResultBuilder endTime(Instant endTime) { this.endTime = endTime; return this; }
            public TestResultBuilder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
            
            public TestResult build() {
                return new TestResult(testId, testName, projectId, environment, status, startTime, endTime, errorMessage);
            }
        }
    }
    
    public enum TestStatus {
        RUNNING, PASSED, FAILED, SKIPPED
    }
    
    public static class DashboardMetrics {
        private int totalTests;
        private double passRate;
        private double failureRate;
        private double averageExecutionTime;
        private int testVelocity;
        private List<TopFailure> topFailures;
        private Map<String, Integer> environmentMetrics;
        private Instant lastUpdated;
        
        public DashboardMetrics(int totalTests, double passRate, double failureRate, 
                               double averageExecutionTime, int testVelocity, 
                               List<TopFailure> topFailures, Map<String, Integer> environmentMetrics) {
            this.totalTests = totalTests;
            this.passRate = passRate;
            this.failureRate = failureRate;
            this.averageExecutionTime = averageExecutionTime;
            this.testVelocity = testVelocity;
            this.topFailures = topFailures;
            this.environmentMetrics = environmentMetrics;
            this.lastUpdated = Instant.now();
        }
        
        // Getters
        public int getTotalTests() { return totalTests; }
        public double getPassRate() { return passRate; }
        public double getFailureRate() { return failureRate; }
        public double getAverageExecutionTime() { return averageExecutionTime; }
        public int getTestVelocity() { return testVelocity; }
        public List<TopFailure> getTopFailures() { return topFailures; }
        public Map<String, Integer> getEnvironmentMetrics() { return environmentMetrics; }
        public Instant getLastUpdated() { return lastUpdated; }
        
        public static DashboardMetricsBuilder builder() {
            return new DashboardMetricsBuilder();
        }
        
        public static class DashboardMetricsBuilder {
            private int totalTests;
            private double passRate;
            private double failureRate;
            private double averageExecutionTime;
            private int testVelocity;
            private List<TopFailure> topFailures = new ArrayList<>();
            private Map<String, Integer> environmentMetrics = new HashMap<>();
            
            public DashboardMetricsBuilder totalTests(int totalTests) { this.totalTests = totalTests; return this; }
            public DashboardMetricsBuilder passRate(double passRate) { this.passRate = passRate; return this; }
            public DashboardMetricsBuilder failureRate(double failureRate) { this.failureRate = failureRate; return this; }
            public DashboardMetricsBuilder averageExecutionTime(double averageExecutionTime) { this.averageExecutionTime = averageExecutionTime; return this; }
            public DashboardMetricsBuilder testVelocity(int testVelocity) { this.testVelocity = testVelocity; return this; }
            public DashboardMetricsBuilder topFailures(List<TopFailure> topFailures) { this.topFailures = topFailures; return this; }
            public DashboardMetricsBuilder environmentMetrics(Map<String, Integer> environmentMetrics) { this.environmentMetrics = environmentMetrics; return this; }
            
            public DashboardMetrics build() {
                return new DashboardMetrics(totalTests, passRate, failureRate, averageExecutionTime, 
                                          testVelocity, topFailures, environmentMetrics);
            }
        }
    }
    
    public static class TopFailure {
        private String testName;
        private int failureCount;
        private String lastError;
        
        public TopFailure(String testName, int failureCount, String lastError) {
            this.testName = testName;
            this.failureCount = failureCount;
            this.lastError = lastError;
        }
        
        public String getTestName() { return testName; }
        public int getFailureCount() { return failureCount; }
        public String getLastError() { return lastError; }
    }
    
    public static class DashboardUpdate {
        private String projectId;
        private TestResult testResult;
        private DashboardMetrics metrics;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private Instant timestamp;
        
        public DashboardUpdate(String projectId, TestResult testResult, DashboardMetrics metrics) {
            this.projectId = projectId;
            this.testResult = testResult;
            this.metrics = metrics;
            this.timestamp = Instant.now();
        }
        
        public String getProjectId() { return projectId; }
        public TestResult getTestResult() { return testResult; }
        public DashboardMetrics getMetrics() { return metrics; }
        public Instant getTimestamp() { return timestamp; }
        
        public static DashboardUpdateBuilder builder() {
            return new DashboardUpdateBuilder();
        }
        
        public static class DashboardUpdateBuilder {
            private String projectId;
            private TestResult testResult;
            private DashboardMetrics metrics;
            
            public DashboardUpdateBuilder projectId(String projectId) { this.projectId = projectId; return this; }
            public DashboardUpdateBuilder testResult(TestResult testResult) { this.testResult = testResult; return this; }
            public DashboardUpdateBuilder metrics(DashboardMetrics metrics) { this.metrics = metrics; return this; }
            
            public DashboardUpdate build() {
                return new DashboardUpdate(projectId, testResult, metrics);
            }
        }
    }
    
    // Core Services
    public static class RealTimeDataService {
        private final ConcurrentMap<String, List<TestResult>> testResults = new ConcurrentHashMap<>();
        private final List<DashboardSubscriber> subscribers = new CopyOnWriteArrayList<>();
        private final MetricsCalculator metricsCalculator = new MetricsCalculator();
        
        public void publishTestResult(TestResult testResult) {
            logger.info("Publishing test result: {} - {}", testResult.getTestName(), testResult.getStatus());
            
            // Store test result
            testResults.computeIfAbsent(testResult.getProjectId(), k -> new CopyOnWriteArrayList<>())
                      .add(testResult);
            
            // Calculate updated metrics
            DashboardMetrics metrics = metricsCalculator.calculateMetrics(
                testResults.get(testResult.getProjectId()));
            
            // Create dashboard update
            DashboardUpdate update = DashboardUpdate.builder()
                .projectId(testResult.getProjectId())
                .testResult(testResult)
                .metrics(metrics)
                .build();
            
            // Notify all subscribers
            notifySubscribers(update);
        }
        
        public void subscribe(DashboardSubscriber subscriber) {
            subscribers.add(subscriber);
            logger.info("New dashboard subscriber added. Total subscribers: {}", subscribers.size());
        }
        
        public void unsubscribe(DashboardSubscriber subscriber) {
            subscribers.remove(subscriber);
            logger.info("Dashboard subscriber removed. Total subscribers: {}", subscribers.size());
        }
        
        private void notifySubscribers(DashboardUpdate update) {
            subscribers.parallelStream().forEach(subscriber -> {
                try {
                    subscriber.onDashboardUpdate(update);
                } catch (Exception e) {
                    logger.error("Error notifying subscriber", e);
                }
            });
        }
        
        public List<TestResult> getTestResults(String projectId) {
            return testResults.getOrDefault(projectId, new ArrayList<>());
        }
        
        public DashboardMetrics getCurrentMetrics(String projectId) {
            List<TestResult> results = getTestResults(projectId);
            return metricsCalculator.calculateMetrics(results);
        }
    }
    
    public static class MetricsCalculator {
        public DashboardMetrics calculateMetrics(List<TestResult> testResults) {
            if (testResults.isEmpty()) {
                return DashboardMetrics.builder()
                    .totalTests(0)
                    .passRate(0.0)
                    .failureRate(0.0)
                    .averageExecutionTime(0.0)
                    .testVelocity(0)
                    .build();
            }
            
            int totalTests = testResults.size();
            long passedTests = testResults.stream()
                .mapToLong(r -> r.getStatus() == TestStatus.PASSED ? 1 : 0)
                .sum();
            long failedTests = testResults.stream()
                .mapToLong(r -> r.getStatus() == TestStatus.FAILED ? 1 : 0)
                .sum();
            
            double passRate = (double) passedTests / totalTests * 100;
            double failureRate = (double) failedTests / totalTests * 100;
            
            double averageExecutionTime = testResults.stream()
                .filter(r -> r.getExecutionTime() != null)
                .mapToDouble(r -> r.getExecutionTime().toMillis())
                .average()
                .orElse(0.0);
            
            // Calculate test velocity (tests per hour in last hour)
            Instant oneHourAgo = Instant.now().minus(Duration.ofHours(1));
            int testVelocity = (int) testResults.stream()
                .filter(r -> r.getStartTime().isAfter(oneHourAgo))
                .count();
            
            // Calculate top failures
            List<TopFailure> topFailures = testResults.stream()
                .filter(r -> r.getStatus() == TestStatus.FAILED)
                .collect(Collectors.groupingBy(TestResult::getTestName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> new TopFailure(
                    entry.getKey(),
                    entry.getValue().size(),
                    entry.getValue().get(entry.getValue().size() - 1).getErrorMessage()
                ))
                .sorted((a, b) -> Integer.compare(b.getFailureCount(), a.getFailureCount()))
                .limit(5)
                .collect(Collectors.toList());
            
            // Calculate environment metrics
            Map<String, Integer> environmentMetrics = testResults.stream()
                .collect(Collectors.groupingBy(
                    TestResult::getEnvironment,
                    Collectors.collectingAndThen(Collectors.toList(), List::size)
                ));
            
            return DashboardMetrics.builder()
                .totalTests(totalTests)
                .passRate(passRate)
                .failureRate(failureRate)
                .averageExecutionTime(averageExecutionTime)
                .testVelocity(testVelocity)
                .topFailures(topFailures)
                .environmentMetrics(environmentMetrics)
                .build();
        }
    }
    
    public static class AlertingService {
        private final List<AlertRule> alertRules = new CopyOnWriteArrayList<>();
        private final List<AlertListener> alertListeners = new CopyOnWriteArrayList<>();
        
        public void addAlertRule(AlertRule rule) {
            alertRules.add(rule);
            logger.info("Added alert rule: {}", rule.getName());
        }
        
        public void addAlertListener(AlertListener listener) {
            alertListeners.add(listener);
        }
        
        public void evaluateAlerts(TestResult testResult, DashboardMetrics metrics) {
            for (AlertRule rule : alertRules) {
                if (rule.getProjectId().equals(testResult.getProjectId()) && rule.isEnabled()) {
                    if (evaluateRule(rule, testResult, metrics)) {
                        triggerAlert(rule, testResult, metrics);
                    }
                }
            }
        }
        
        private boolean evaluateRule(AlertRule rule, TestResult testResult, DashboardMetrics metrics) {
            switch (rule.getType()) {
                case FAILURE_RATE:
                    return metrics.getFailureRate() > rule.getThreshold();
                case CONSECUTIVE_FAILURES:
                    return testResult.getStatus() == TestStatus.FAILED;
                case EXECUTION_TIME:
                    return testResult.getExecutionTime() != null && 
                           testResult.getExecutionTime().toMillis() > rule.getThreshold();
                default:
                    return false;
            }
        }
        
        private void triggerAlert(AlertRule rule, TestResult testResult, DashboardMetrics metrics) {
            Alert alert = new Alert(
                rule.getId(),
                rule.getProjectId(),
                rule.getSeverity(),
                generateAlertMessage(rule, testResult, metrics),
                Instant.now()
            );
            
            alertListeners.forEach(listener -> listener.onAlert(alert));
            logger.warn("Alert triggered: {}", alert.getMessage());
        }
        
        private String generateAlertMessage(AlertRule rule, TestResult testResult, DashboardMetrics metrics) {
            switch (rule.getType()) {
                case FAILURE_RATE:
                    return String.format("Failure rate %.2f%% exceeds threshold %.2f%% for project %s", 
                                       metrics.getFailureRate(), rule.getThreshold(), rule.getProjectId());
                case CONSECUTIVE_FAILURES:
                    return String.format("Test %s failed in project %s", 
                                       testResult.getTestName(), rule.getProjectId());
                case EXECUTION_TIME:
                    return String.format("Test %s execution time %dms exceeds threshold %.0fms", 
                                       testResult.getTestName(), 
                                       testResult.getExecutionTime().toMillis(), 
                                       rule.getThreshold());
                default:
                    return "Unknown alert condition";
            }
        }
    }
    
    // Supporting Classes
    public interface DashboardSubscriber {
        void onDashboardUpdate(DashboardUpdate update);
    }
    
    public interface AlertListener {
        void onAlert(Alert alert);
    }
    
    public static class AlertRule {
        private String id;
        private String name;
        private String projectId;
        private AlertType type;
        private double threshold;
        private AlertSeverity severity;
        private boolean enabled;
        
        public AlertRule(String id, String name, String projectId, AlertType type, 
                        double threshold, AlertSeverity severity, boolean enabled) {
            this.id = id;
            this.name = name;
            this.projectId = projectId;
            this.type = type;
            this.threshold = threshold;
            this.severity = severity;
            this.enabled = enabled;
        }
        
        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getProjectId() { return projectId; }
        public AlertType getType() { return type; }
        public double getThreshold() { return threshold; }
        public AlertSeverity getSeverity() { return severity; }
        public boolean isEnabled() { return enabled; }
    }
    
    public enum AlertType {
        FAILURE_RATE, CONSECUTIVE_FAILURES, EXECUTION_TIME
    }
    
    public enum AlertSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public static class Alert {
        private String ruleId;
        private String projectId;
        private AlertSeverity severity;
        private String message;
        private Instant triggeredAt;
        
        public Alert(String ruleId, String projectId, AlertSeverity severity, String message, Instant triggeredAt) {
            this.ruleId = ruleId;
            this.projectId = projectId;
            this.severity = severity;
            this.message = message;
            this.triggeredAt = triggeredAt;
        }
        
        // Getters
        public String getRuleId() { return ruleId; }
        public String getProjectId() { return projectId; }
        public AlertSeverity getSeverity() { return severity; }
        public String getMessage() { return message; }
        public Instant getTriggeredAt() { return triggeredAt; }
    }
    
    // Main Dashboard System
    public static class TestMonitoringDashboard {
        private final RealTimeDataService realTimeService;
        private final AlertingService alertingService;
        private final ObjectMapper objectMapper;
        
        public TestMonitoringDashboard() {
            this.realTimeService = new RealTimeDataService();
            this.alertingService = new AlertingService();
            this.objectMapper = new ObjectMapper();
            this.objectMapper.registerModule(new JavaTimeModule());
            
            // Connect alerting to real-time service
            realTimeService.subscribe(update -> {
                alertingService.evaluateAlerts(update.getTestResult(), update.getMetrics());
            });
        }
        
        public void publishTestResult(TestResult testResult) {
            realTimeService.publishTestResult(testResult);
        }
        
        public void subscribe(DashboardSubscriber subscriber) {
            realTimeService.subscribe(subscriber);
        }
        
        public void addAlertRule(AlertRule rule) {
            alertingService.addAlertRule(rule);
        }
        
        public void addAlertListener(AlertListener listener) {
            alertingService.addAlertListener(listener);
        }
        
        public DashboardMetrics getCurrentMetrics(String projectId) {
            return realTimeService.getCurrentMetrics(projectId);
        }
        
        public List<TestResult> getTestResults(String projectId) {
            return realTimeService.getTestResults(projectId);
        }
        
        public String exportMetricsAsJson(String projectId) {
            try {
                DashboardMetrics metrics = getCurrentMetrics(projectId);
                return objectMapper.writeValueAsString(metrics);
            } catch (Exception e) {
                logger.error("Error exporting metrics as JSON", e);
                return "{}";
            }
        }
    }
}

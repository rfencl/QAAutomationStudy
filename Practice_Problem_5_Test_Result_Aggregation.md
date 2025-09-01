# Practice Problem 5: Design a Test Result Aggregation System

## Problem Statement
Create a comprehensive test result aggregation system that collects, processes, and analyzes test results from multiple sources (unit tests, integration tests, UI tests), provides real-time analytics, generates detailed reports, and supports trend analysis for continuous improvement.

## Requirements

### Functional Requirements
1. **Multi-Source Collection**: Aggregate results from TestNG, JUnit, Selenium, API tests
2. **Real-Time Processing**: Process results as they arrive
3. **Analytics Engine**: Calculate metrics, trends, and insights
4. **Report Generation**: Create detailed HTML/PDF reports
5. **Dashboard Interface**: Real-time web dashboard
6. **Alerting System**: Notify on failures and threshold breaches
7. **Historical Analysis**: Track trends over time

### Non-Functional Requirements
1. **Performance**: Handle 10,000+ test results per minute
2. **Scalability**: Support multiple projects and teams
3. **Reliability**: 99.9% uptime with data persistence
4. **Extensibility**: Easy to add new result sources and metrics

## System Architecture

```java
@Component
public class TestResultAggregationSystem {
    private final ResultCollector resultCollector;
    private final ResultProcessor resultProcessor;
    private final AnalyticsEngine analyticsEngine;
    private final ReportGenerator reportGenerator;
    private final DashboardService dashboardService;
    private final AlertingService alertingService;
}
```

## Core Components

### 1. Result Collector
```java
@Service
public class TestResultCollector {
    private final Map<String, ResultParser> parsers;
    private final MessageQueue resultQueue;
    private final ResultRepository repository;
    
    @EventListener
    public void handleTestCompletion(TestCompletionEvent event) {
        TestResult result = parseResult(event);
        resultQueue.publish(result);
        repository.save(result);
    }
    
    public void collectFromFile(String filePath, ResultFormat format) {
        ResultParser parser = parsers.get(format.name());
        List<TestResult> results = parser.parse(filePath);
        
        results.forEach(result -> {
            enrichResult(result);
            resultQueue.publish(result);
            repository.save(result);
        });
    }
    
    public void collectFromApi(String endpoint, Map<String, String> headers) {
        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach(httpHeaders::set);
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        
        ResponseEntity<TestResultsResponse> response = restTemplate.exchange(
            endpoint, HttpMethod.GET, entity, TestResultsResponse.class);
            
        if (response.getStatusCode().is2xxSuccessful()) {
            response.getBody().getResults().forEach(result -> {
                enrichResult(result);
                resultQueue.publish(result);
                repository.save(result);
            });
        }
    }
    
    private void enrichResult(TestResult result) {
        result.setTimestamp(Instant.now());
        result.setBuildId(getCurrentBuildId());
        result.setEnvironment(getCurrentEnvironment());
        result.setVersion(getCurrentVersion());
    }
}
```

### 2. Result Processor
```java
@Service
public class TestResultProcessor {
    private final AnalyticsEngine analyticsEngine;
    private final AlertingService alertingService;
    
    @RabbitListener(queues = "test.results")
    public void processResult(TestResult result) {
        try {
            // Validate result
            validateResult(result);
            
            // Update real-time metrics
            analyticsEngine.updateMetrics(result);
            
            // Check alert conditions
            checkAlertConditions(result);
            
            // Update dashboard
            updateDashboard(result);
            
        } catch (Exception e) {
            handleProcessingError(result, e);
        }
    }
    
    private void checkAlertConditions(TestResult result) {
        if (result.getStatus() == TestStatus.FAILED) {
            alertingService.checkFailureAlerts(result);
        }
        
        // Check performance thresholds
        if (result.getExecutionTime() > getPerformanceThreshold(result.getTestType())) {
            alertingService.sendPerformanceAlert(result);
        }
        
        // Check failure rate
        double failureRate = analyticsEngine.getFailureRate(result.getProject(), Duration.ofHours(1));
        if (failureRate > 0.1) { // 10% failure rate threshold
            alertingService.sendFailureRateAlert(result.getProject(), failureRate);
        }
    }
}
```

### 3. Analytics Engine
```java
@Service
public class TestAnalyticsEngine {
    private final TestResultRepository repository;
    private final MetricsCalculator metricsCalculator;
    private final TrendAnalyzer trendAnalyzer;
    
    public TestMetrics calculateMetrics(String projectId, Duration timeWindow) {
        Instant startTime = Instant.now().minus(timeWindow);
        List<TestResult> results = repository.findByProjectIdAndTimestampAfter(projectId, startTime);
        
        return TestMetrics.builder()
            .totalTests(results.size())
            .passedTests(countByStatus(results, TestStatus.PASSED))
            .failedTests(countByStatus(results, TestStatus.FAILED))
            .skippedTests(countByStatus(results, TestStatus.SKIPPED))
            .passRate(calculatePassRate(results))
            .averageExecutionTime(calculateAverageExecutionTime(results))
            .testsByType(groupByTestType(results))
            .testsByEnvironment(groupByEnvironment(results))
            .build();
    }
    
    public List<TestTrend> analyzeTrends(String projectId, Duration period) {
        return trendAnalyzer.analyzeTrends(projectId, period);
    }
    
    public List<TestResult> identifyFlakyTests(String projectId, Duration timeWindow) {
        List<TestResult> results = repository.findByProjectIdAndTimestampAfter(
            projectId, Instant.now().minus(timeWindow));
            
        Map<String, List<TestResult>> testGroups = results.stream()
            .collect(Collectors.groupingBy(TestResult::getTestName));
            
        return testGroups.entrySet().stream()
            .filter(entry -> isFlakyTest(entry.getValue()))
            .map(entry -> entry.getValue().get(0))
            .collect(Collectors.toList());
    }
    
    private boolean isFlakyTest(List<TestResult> testResults) {
        if (testResults.size() < 5) return false; // Need minimum executions
        
        long passCount = testResults.stream()
            .mapToLong(r -> r.getStatus() == TestStatus.PASSED ? 1 : 0)
            .sum();
            
        double passRate = (double) passCount / testResults.size();
        return passRate > 0.2 && passRate < 0.8; // Flaky if pass rate between 20-80%
    }
}
```

### 4. Report Generator
```java
@Service
public class TestReportGenerator {
    private final TemplateEngine templateEngine;
    private final PdfGenerator pdfGenerator;
    private final ChartGenerator chartGenerator;
    
    public TestReport generateReport(ReportRequest request) {
        TestMetrics metrics = analyticsEngine.calculateMetrics(
            request.getProjectId(), request.getTimeWindow());
            
        List<TestTrend> trends = analyticsEngine.analyzeTrends(
            request.getProjectId(), request.getTimeWindow());
            
        List<TestResult> flakyTests = analyticsEngine.identifyFlakyTests(
            request.getProjectId(), request.getTimeWindow());
            
        ReportData reportData = ReportData.builder()
            .metrics(metrics)
            .trends(trends)
            .flakyTests(flakyTests)
            .topFailures(getTopFailures(request))
            .performanceMetrics(getPerformanceMetrics(request))
            .build();
            
        return createReport(reportData, request.getFormat());
    }
    
    private TestReport createReport(ReportData data, ReportFormat format) {
        switch (format) {
            case HTML:
                return generateHtmlReport(data);
            case PDF:
                return generatePdfReport(data);
            case JSON:
                return generateJsonReport(data);
            default:
                throw new UnsupportedReportFormatException("Format not supported: " + format);
        }
    }
    
    private TestReport generateHtmlReport(ReportData data) {
        // Generate charts
        String passRateChart = chartGenerator.generatePassRateChart(data.getMetrics());
        String trendChart = chartGenerator.generateTrendChart(data.getTrends());
        String executionTimeChart = chartGenerator.generateExecutionTimeChart(data.getMetrics());
        
        // Prepare template context
        Map<String, Object> context = new HashMap<>();
        context.put("metrics", data.getMetrics());
        context.put("trends", data.getTrends());
        context.put("flakyTests", data.getFlakyTests());
        context.put("passRateChart", passRateChart);
        context.put("trendChart", trendChart);
        context.put("executionTimeChart", executionTimeChart);
        
        String htmlContent = templateEngine.process("test-report-template", context);
        
        return TestReport.builder()
            .format(ReportFormat.HTML)
            .content(htmlContent)
            .generatedAt(Instant.now())
            .build();
    }
}
```

### 5. Dashboard Service
```java
@RestController
@RequestMapping("/api/dashboard")
public class TestDashboardController {
    private final TestAnalyticsEngine analyticsEngine;
    private final SimpMessagingTemplate messagingTemplate;
    
    @GetMapping("/metrics/{projectId}")
    public ResponseEntity<TestMetrics> getMetrics(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "24h") String timeWindow) {
        
        Duration duration = parseDuration(timeWindow);
        TestMetrics metrics = analyticsEngine.calculateMetrics(projectId, duration);
        
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/trends/{projectId}")
    public ResponseEntity<List<TestTrend>> getTrends(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "7d") String period) {
        
        Duration duration = parseDuration(period);
        List<TestTrend> trends = analyticsEngine.analyzeTrends(projectId, duration);
        
        return ResponseEntity.ok(trends);
    }
    
    @EventListener
    public void handleTestResult(TestResult result) {
        // Send real-time updates to dashboard
        DashboardUpdate update = DashboardUpdate.builder()
            .projectId(result.getProjectId())
            .testResult(result)
            .timestamp(Instant.now())
            .build();
            
        messagingTemplate.convertAndSend("/topic/dashboard/" + result.getProjectId(), update);
    }
}

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS();
    }
}
```

## Data Models

### Test Result
```java
@Entity
@Table(name = "test_results")
@Data
@Builder
public class TestResult {
    @Id
    private String id;
    
    private String projectId;
    private String buildId;
    private String testName;
    private String className;
    private String methodName;
    
    @Enumerated(EnumType.STRING)
    private TestStatus status;
    
    @Enumerated(EnumType.STRING)
    private TestType testType;
    
    private String environment;
    private String version;
    private Instant timestamp;
    private long executionTimeMs;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(columnDefinition = "TEXT")
    private String stackTrace;
    
    private String screenshotPath;
    
    @ElementCollection
    @MapKeyColumn(name = "metric_name")
    @Column(name = "metric_value")
    private Map<String, String> customMetrics;
}
```

### Test Metrics
```java
@Data
@Builder
public class TestMetrics {
    private int totalTests;
    private int passedTests;
    private int failedTests;
    private int skippedTests;
    private double passRate;
    private double averageExecutionTime;
    private Map<TestType, Integer> testsByType;
    private Map<String, Integer> testsByEnvironment;
    private List<TopFailure> topFailures;
    private PerformanceMetrics performanceMetrics;
}
```

## Implementation Examples

### 1. TestNG Integration
```java
public class TestResultAggregationListener implements ITestListener {
    private final TestResultCollector collector;
    
    @Override
    public void onTestSuccess(ITestResult result) {
        TestResult testResult = convertToTestResult(result, TestStatus.PASSED);
        collector.collectResult(testResult);
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        TestResult testResult = convertToTestResult(result, TestStatus.FAILED);
        testResult.setErrorMessage(result.getThrowable().getMessage());
        testResult.setStackTrace(getStackTrace(result.getThrowable()));
        collector.collectResult(testResult);
    }
    
    private TestResult convertToTestResult(ITestResult result, TestStatus status) {
        return TestResult.builder()
            .id(UUID.randomUUID().toString())
            .testName(result.getMethod().getMethodName())
            .className(result.getTestClass().getName())
            .status(status)
            .executionTimeMs(result.getEndMillis() - result.getStartMillis())
            .timestamp(Instant.ofEpochMilli(result.getStartMillis()))
            .build();
    }
}
```

### 2. CI/CD Integration
```yaml
# Jenkins Pipeline
pipeline {
    agent any
    
    stages {
        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    // Publish results to aggregation system
                    script {
                        def results = readFile('target/surefire-reports/TEST-*.xml')
                        httpRequest(
                            httpMode: 'POST',
                            url: "${AGGREGATION_API}/results/upload",
                            requestBody: results,
                            contentType: 'APPLICATION_XML'
                        )
                    }
                }
            }
        }
    }
}
```

### 3. Real-time Dashboard (JavaScript)
```javascript
// Dashboard WebSocket connection
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to project updates
    stompClient.subscribe('/topic/dashboard/' + projectId, function(message) {
        const update = JSON.parse(message.body);
        updateDashboard(update);
    });
});

function updateDashboard(update) {
    // Update metrics
    document.getElementById('total-tests').textContent = update.metrics.totalTests;
    document.getElementById('pass-rate').textContent = update.metrics.passRate + '%';
    
    // Update charts
    updatePassRateChart(update.metrics);
    updateTrendChart(update.trends);
    
    // Add latest test result
    addTestResultToTable(update.testResult);
}

function generateReport() {
    fetch('/api/reports/generate', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            projectId: projectId,
            timeWindow: '24h',
            format: 'HTML'
        })
    })
    .then(response => response.blob())
    .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'test-report.html';
        a.click();
    });
}
```

## Success Criteria
1. **Data Collection**: 100% capture rate of test results
2. **Real-time Processing**: Results processed within 5 seconds
3. **Dashboard Performance**: Sub-second response times
4. **Report Generation**: Reports generated within 30 seconds
5. **Scalability**: Handle 10,000+ results per minute
6. **Reliability**: 99.9% uptime with zero data loss

## Extension Points
1. **Machine Learning**: Predictive analytics for test failures
2. **Integration**: Support for additional test frameworks
3. **Visualization**: Advanced charting and visualization options
4. **Export**: Integration with external reporting tools
5. **API**: RESTful API for external integrations

## Deliverables
1. `TestResultCollector.java` - Result collection service
2. `TestAnalyticsEngine.java` - Analytics and metrics calculation
3. `TestReportGenerator.java` - Report generation service
4. `DashboardController.java` - REST API for dashboard
5. `dashboard.html` - Real-time web dashboard
6. `TestResultAggregationTest.java` - Comprehensive tests
7. `README.md` - Setup and configuration guide

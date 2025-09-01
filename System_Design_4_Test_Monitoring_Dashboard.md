# System Design 4: Real-time Test Result Monitoring Dashboard

## Problem Statement
Design a comprehensive real-time monitoring dashboard that provides live visibility into test execution across multiple projects, environments, and test types, with advanced analytics, alerting capabilities, and historical trend analysis for continuous improvement.

## System Requirements

### Functional Requirements
1. **Real-time Updates**: Live test execution status and results
2. **Multi-Project Support**: Monitor multiple projects simultaneously
3. **Advanced Analytics**: Failure analysis, performance metrics, trends
4. **Custom Dashboards**: Configurable dashboards for different stakeholders
5. **Alerting System**: Configurable alerts for failures and thresholds
6. **Historical Analysis**: Long-term trend analysis and reporting
7. **Integration APIs**: RESTful APIs for external system integration

### Non-Functional Requirements
1. **Performance**: Sub-second dashboard updates
2. **Scalability**: Support 1000+ concurrent users
3. **Reliability**: 99.9% uptime with real-time data accuracy
4. **Responsiveness**: Mobile-friendly responsive design

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                     Test Monitoring Dashboard System                           │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐             │
│  │   Web          │    │   Mobile        │    │   API           │             │
│  │   Dashboard     │    │   App           │    │   Gateway       │             │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘             │
│           │                       │                       │                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐ │
│  │                        Dashboard Backend                                   │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐         │ │
│  │  │   Real-time │ │   Analytics │ │   Alerting  │ │   User      │         │ │
│  │  │   Service   │ │   Engine    │ │   Service   │ │   Management│         │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘         │ │
│  └─────────────────────────────────────────────────────────────────────────────┘ │
│           │                       │                       │                     │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐             │
│  │   Message       │    │   Time Series   │    │   Configuration │             │
│  │   Broker        │    │   Database      │    │   Service       │             │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘             │
└─────────────────────────────────────────────────────────────────────────────────┘
```

## Core Components

### 1. Real-time Data Service
```java
@Service
public class RealTimeDataService {
    private final SimpMessagingTemplate messagingTemplate;
    private final TestResultRepository repository;
    private final MetricsCalculator metricsCalculator;
    
    @EventListener
    public void handleTestResult(TestResultEvent event) {
        TestResult result = event.getTestResult();
        
        // Update real-time metrics
        RealTimeMetrics metrics = metricsCalculator.updateMetrics(result);
        
        // Broadcast to dashboard subscribers
        DashboardUpdate update = DashboardUpdate.builder()
            .projectId(result.getProjectId())
            .testResult(result)
            .metrics(metrics)
            .timestamp(Instant.now())
            .build();
            
        messagingTemplate.convertAndSend("/topic/dashboard/" + result.getProjectId(), update);
        messagingTemplate.convertAndSend("/topic/global", update);
    }
    
    @MessageMapping("/subscribe")
    public void handleSubscription(DashboardSubscription subscription, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        
        // Send current state to new subscriber
        DashboardState currentState = getCurrentDashboardState(subscription.getProjectId());
        messagingTemplate.convertAndSendToUser(sessionId, "/queue/initial-state", currentState);
    }
}
```

### 2. Analytics Engine
```java
@Service
public class DashboardAnalyticsEngine {
    private final TimeSeriesDatabase timeSeriesDb;
    private final TrendAnalyzer trendAnalyzer;
    
    public DashboardMetrics calculateDashboardMetrics(String projectId, Duration timeWindow) {
        Instant startTime = Instant.now().minus(timeWindow);
        
        List<TestResult> results = timeSeriesDb.queryTestResults(projectId, startTime, Instant.now());
        
        return DashboardMetrics.builder()
            .totalTests(results.size())
            .passRate(calculatePassRate(results))
            .failureRate(calculateFailureRate(results))
            .averageExecutionTime(calculateAverageExecutionTime(results))
            .testVelocity(calculateTestVelocity(results, timeWindow))
            .topFailures(identifyTopFailures(results))
            .performanceMetrics(calculatePerformanceMetrics(results))
            .environmentMetrics(calculateEnvironmentMetrics(results))
            .build();
    }
    
    public List<TrendData> calculateTrends(String projectId, Duration period) {
        return trendAnalyzer.calculateTrends(projectId, period, Arrays.asList(
            TrendType.PASS_RATE,
            TrendType.EXECUTION_TIME,
            TrendType.TEST_COUNT,
            TrendType.FAILURE_RATE
        ));
    }
    
    public FailureAnalysis analyzeFailures(String projectId, Duration timeWindow) {
        List<TestResult> failures = timeSeriesDb.queryFailedTests(projectId, timeWindow);
        
        Map<String, Long> failuresByCategory = failures.stream()
            .collect(Collectors.groupingBy(
                this::categorizeFailure,
                Collectors.counting()
            ));
            
        Map<String, Long> failuresByEnvironment = failures.stream()
            .collect(Collectors.groupingBy(
                TestResult::getEnvironment,
                Collectors.counting()
            ));
            
        return FailureAnalysis.builder()
            .totalFailures(failures.size())
            .failuresByCategory(failuresByCategory)
            .failuresByEnvironment(failuresByEnvironment)
            .topFailingTests(getTopFailingTests(failures))
            .flakyTests(identifyFlakyTests(failures))
            .build();
    }
}
```

### 3. Dashboard Controller
```java
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardAnalyticsEngine analyticsEngine;
    private final AlertingService alertingService;
    
    @GetMapping("/projects/{projectId}/overview")
    public ResponseEntity<ProjectOverview> getProjectOverview(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "24h") String timeWindow) {
        
        Duration duration = parseDuration(timeWindow);
        
        DashboardMetrics metrics = analyticsEngine.calculateDashboardMetrics(projectId, duration);
        List<TrendData> trends = analyticsEngine.calculateTrends(projectId, duration);
        FailureAnalysis failureAnalysis = analyticsEngine.analyzeFailures(projectId, duration);
        
        ProjectOverview overview = ProjectOverview.builder()
            .projectId(projectId)
            .metrics(metrics)
            .trends(trends)
            .failureAnalysis(failureAnalysis)
            .lastUpdated(Instant.now())
            .build();
            
        return ResponseEntity.ok(overview);
    }
    
    @GetMapping("/projects/{projectId}/live-tests")
    public ResponseEntity<List<LiveTestExecution>> getLiveTests(@PathVariable String projectId) {
        List<LiveTestExecution> liveTests = testExecutionService.getLiveExecutions(projectId);
        return ResponseEntity.ok(liveTests);
    }
    
    @PostMapping("/projects/{projectId}/alerts")
    public ResponseEntity<AlertRule> createAlert(
            @PathVariable String projectId,
            @RequestBody CreateAlertRequest request) {
        
        AlertRule rule = alertingService.createAlertRule(projectId, request);
        return ResponseEntity.ok(rule);
    }
}
```

### 4. Alerting Service
```java
@Service
public class DashboardAlertingService {
    private final AlertRuleRepository alertRuleRepository;
    private final NotificationService notificationService;
    
    @EventListener
    public void evaluateAlerts(TestResultEvent event) {
        TestResult result = event.getTestResult();
        List<AlertRule> rules = alertRuleRepository.findByProjectId(result.getProjectId());
        
        for (AlertRule rule : rules) {
            if (rule.isEnabled() && evaluateRule(rule, result)) {
                triggerAlert(rule, result);
            }
        }
    }
    
    private boolean evaluateRule(AlertRule rule, TestResult result) {
        switch (rule.getType()) {
            case FAILURE_RATE:
                return evaluateFailureRateRule(rule, result);
            case EXECUTION_TIME:
                return evaluateExecutionTimeRule(rule, result);
            case CONSECUTIVE_FAILURES:
                return evaluateConsecutiveFailuresRule(rule, result);
            default:
                return false;
        }
    }
    
    private void triggerAlert(AlertRule rule, TestResult result) {
        Alert alert = Alert.builder()
            .ruleId(rule.getId())
            .projectId(result.getProjectId())
            .severity(rule.getSeverity())
            .message(generateAlertMessage(rule, result))
            .triggeredAt(Instant.now())
            .build();
            
        notificationService.sendAlert(alert);
    }
}
```

### 5. Dashboard Frontend (React)
```javascript
// Dashboard.jsx
import React, { useState, useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const Dashboard = ({ projectId }) => {
    const [metrics, setMetrics] = useState(null);
    const [liveTests, setLiveTests] = useState([]);
    const [stompClient, setStompClient] = useState(null);
    
    useEffect(() => {
        // Initialize WebSocket connection
        const socket = new SockJS('/ws');
        const client = new Client({
            webSocketFactory: () => socket,
            onConnect: () => {
                console.log('Connected to WebSocket');
                
                // Subscribe to project updates
                client.subscribe(`/topic/dashboard/${projectId}`, (message) => {
                    const update = JSON.parse(message.body);
                    handleDashboardUpdate(update);
                });
                
                // Subscribe to initial state
                client.subscribe('/user/queue/initial-state', (message) => {
                    const state = JSON.parse(message.body);
                    setMetrics(state.metrics);
                    setLiveTests(state.liveTests);
                });
                
                // Request initial state
                client.publish({
                    destination: '/app/subscribe',
                    body: JSON.stringify({ projectId })
                });
            }
        });
        
        client.activate();
        setStompClient(client);
        
        return () => {
            if (client) {
                client.deactivate();
            }
        };
    }, [projectId]);
    
    const handleDashboardUpdate = (update) => {
        // Update metrics
        setMetrics(prevMetrics => ({
            ...prevMetrics,
            ...update.metrics
        }));
        
        // Update live tests
        if (update.testResult) {
            setLiveTests(prevTests => {
                const updatedTests = [...prevTests];
                const index = updatedTests.findIndex(t => t.id === update.testResult.id);
                
                if (index >= 0) {
                    updatedTests[index] = update.testResult;
                } else {
                    updatedTests.push(update.testResult);
                }
                
                return updatedTests;
            });
        }
    };
    
    return (
        <div className="dashboard">
            <MetricsOverview metrics={metrics} />
            <LiveTestsTable tests={liveTests} />
            <TrendsChart projectId={projectId} />
            <FailureAnalysis projectId={projectId} />
        </div>
    );
};
```

### 6. Custom Dashboard Configuration
```javascript
// DashboardBuilder.jsx
const DashboardBuilder = () => {
    const [widgets, setWidgets] = useState([]);
    const [availableWidgets] = useState([
        { type: 'metrics-overview', name: 'Metrics Overview' },
        { type: 'pass-rate-chart', name: 'Pass Rate Chart' },
        { type: 'execution-time-chart', name: 'Execution Time Chart' },
        { type: 'failure-analysis', name: 'Failure Analysis' },
        { type: 'live-tests', name: 'Live Test Executions' },
        { type: 'environment-status', name: 'Environment Status' }
    ]);
    
    const addWidget = (widgetType) => {
        const widget = {
            id: generateId(),
            type: widgetType,
            position: { x: 0, y: 0 },
            size: { width: 4, height: 3 },
            config: {}
        };
        
        setWidgets([...widgets, widget]);
    };
    
    const saveDashboard = () => {
        const dashboard = {
            name: dashboardName,
            widgets: widgets,
            layout: 'grid'
        };
        
        fetch('/api/dashboards', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dashboard)
        });
    };
    
    return (
        <div className="dashboard-builder">
            <WidgetPalette widgets={availableWidgets} onAddWidget={addWidget} />
            <DashboardCanvas widgets={widgets} onUpdateWidget={updateWidget} />
            <DashboardControls onSave={saveDashboard} />
        </div>
    );
};
```

## Data Models

### Dashboard Metrics
```java
@Data
@Builder
public class DashboardMetrics {
    private int totalTests;
    private double passRate;
    private double failureRate;
    private double averageExecutionTime;
    private int testVelocity; // tests per hour
    private List<TopFailure> topFailures;
    private Map<String, Double> performanceMetrics;
    private Map<String, Integer> environmentMetrics;
    private Instant lastUpdated;
}
```

### Live Test Execution
```java
@Data
@Builder
public class LiveTestExecution {
    private String testId;
    private String testName;
    private String environment;
    private TestStatus status;
    private Instant startTime;
    private Duration estimatedDuration;
    private String executionNode;
    private Map<String, Object> progress;
}
```

## Success Criteria
1. **Real-time Updates**: Dashboard updates within 1 second of test completion
2. **Performance**: Support 1000+ concurrent dashboard users
3. **Accuracy**: 100% accurate real-time data representation
4. **Responsiveness**: Mobile-friendly responsive design
5. **Customization**: Flexible dashboard configuration options
6. **Alerting**: Reliable alert delivery within 30 seconds

## Technology Stack
- **Backend**: Spring Boot, WebSocket, Redis, InfluxDB
- **Frontend**: React, D3.js, WebSocket, Material-UI
- **Database**: PostgreSQL (metadata), InfluxDB (time series)
- **Message Broker**: Redis Pub/Sub or Apache Kafka
- **Monitoring**: Prometheus, Grafana

## Deliverables
1. **Backend Services**: Real-time data service, analytics engine, alerting service
2. **Frontend Application**: React-based dashboard with real-time updates
3. **API Documentation**: RESTful API documentation
4. **Configuration**: Dashboard and alert configuration interfaces
5. **Deployment**: Docker containers and Kubernetes manifests

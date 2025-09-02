package com.qa.distributed.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents the result of a test execution.
 * Design Decision: Immutable result object with comprehensive execution metadata
 * for proper tracking and reporting in distributed environment.
 */
public class TestResult {
    public enum Status {
        PASSED, FAILED, SKIPPED, ERROR
    }
    
    private final String taskId;
    private final String nodeId;
    private final Status status;
    private final String message;
    private final String stackTrace;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final long executionTimeMs;
    
    @JsonCreator
    public TestResult(
            @JsonProperty("taskId") String taskId,
            @JsonProperty("nodeId") String nodeId,
            @JsonProperty("status") Status status,
            @JsonProperty("message") String message,
            @JsonProperty("stackTrace") String stackTrace,
            @JsonProperty("startTime") LocalDateTime startTime,
            @JsonProperty("endTime") LocalDateTime endTime,
            @JsonProperty("executionTimeMs") long executionTimeMs) {
        this.taskId = Objects.requireNonNull(taskId, "Task ID cannot be null");
        this.nodeId = Objects.requireNonNull(nodeId, "Node ID cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.message = message;
        this.stackTrace = stackTrace;
        this.startTime = startTime;
        this.endTime = endTime;
        this.executionTimeMs = executionTimeMs;
    }
    
    public String getTaskId() { return taskId; }
    public String getNodeId() { return nodeId; }
    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public String getStackTrace() { return stackTrace; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    
    public boolean isSuccess() {
        return status == Status.PASSED;
    }
    
    @Override
    public String toString() {
        return String.format("TestResult{taskId='%s', nodeId='%s', status=%s, time=%dms}", 
                           taskId, nodeId, status, executionTimeMs);
    }
}

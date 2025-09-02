package com.qa.distributed.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a test task that can be executed on a distributed node.
 * Design Decision: Immutable value object to ensure thread safety and prevent
 * accidental modifications during distribution across nodes.
 */
public class TestTask {
    private final String taskId;
    private final String testClass;
    private final String testMethod;
    private final Map<String, String> parameters;
    private final int priority;
    
    @JsonCreator
    public TestTask(
            @JsonProperty("taskId") String taskId,
            @JsonProperty("testClass") String testClass,
            @JsonProperty("testMethod") String testMethod,
            @JsonProperty("parameters") Map<String, String> parameters,
            @JsonProperty("priority") int priority) {
        this.taskId = Objects.requireNonNull(taskId, "Task ID cannot be null");
        this.testClass = Objects.requireNonNull(testClass, "Test class cannot be null");
        this.testMethod = testMethod; // Can be null for class-level execution
        this.parameters = parameters != null ? Map.copyOf(parameters) : Map.of();
        this.priority = priority;
    }
    
    public String getTaskId() { return taskId; }
    public String getTestClass() { return testClass; }
    public String getTestMethod() { return testMethod; }
    public Map<String, String> getParameters() { return parameters; }
    public int getPriority() { return priority; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestTask)) return false;
        TestTask testTask = (TestTask) o;
        return Objects.equals(taskId, testTask.taskId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }
    
    @Override
    public String toString() {
        return String.format("TestTask{id='%s', class='%s', method='%s', priority=%d}", 
                           taskId, testClass, testMethod, priority);
    }
}

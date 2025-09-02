package com.qa.distributed.coordinator;

import com.qa.distributed.common.TestTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Comparator;
import java.util.Map;

/**
 * Thread-safe task queue with priority support for distributed test execution.
 * Design Decision: Priority queue ensures high-priority tests run first.
 * Blocking queue provides thread-safe operations for concurrent access.
 */
public class TaskQueue {
    private final BlockingQueue<TestTask> pendingTasks;
    private final Map<String, TestTask> assignedTasks;
    private final Map<String, TestTask> completedTasks;
    
    public TaskQueue() {
        // Priority queue orders tasks by priority (higher number = higher priority)
        this.pendingTasks = new PriorityBlockingQueue<>(100, 
            Comparator.comparingInt(TestTask::getPriority).reversed());
        this.assignedTasks = new ConcurrentHashMap<>();
        this.completedTasks = new ConcurrentHashMap<>();
    }
    
    /**
     * Add task to pending queue.
     * Design Decision: Non-blocking add operation to prevent coordinator slowdown.
     */
    public void addTask(TestTask task) {
        pendingTasks.offer(task);
        System.out.println("Added task to queue: " + task);
    }
    
    /**
     * Get next task from queue (blocking operation).
     * Design Decision: Blocking take() ensures coordinator waits for tasks
     * rather than busy-waiting, improving CPU efficiency.
     */
    public TestTask getNextTask() throws InterruptedException {
        TestTask task = pendingTasks.take();
        assignedTasks.put(task.getTaskId(), task);
        return task;
    }
    
    /**
     * Mark task as completed and move to completed tasks.
     */
    public void completeTask(String taskId) {
        TestTask task = assignedTasks.remove(taskId);
        if (task != null) {
            completedTasks.put(taskId, task);
            System.out.println("Task completed: " + taskId);
        }
    }
    
    /**
     * Return task to pending queue if execution failed.
     * Design Decision: Failed tasks are re-queued for retry on different nodes.
     */
    public void requeueTask(String taskId) {
        TestTask task = assignedTasks.remove(taskId);
        if (task != null) {
            pendingTasks.offer(task);
            System.out.println("Task requeued: " + taskId);
        }
    }
    
    public int getPendingCount() {
        return pendingTasks.size();
    }
    
    public int getAssignedCount() {
        return assignedTasks.size();
    }
    
    public int getCompletedCount() {
        return completedTasks.size();
    }
    
    public boolean isEmpty() {
        return pendingTasks.isEmpty() && assignedTasks.isEmpty();
    }
    
    /**
     * Get queue statistics for monitoring.
     */
    public String getQueueStats() {
        return String.format("Queue Stats - Pending: %d, Assigned: %d, Completed: %d", 
                           getPendingCount(), getAssignedCount(), getCompletedCount());
    }
}

package com.qa.distributed.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents information about a test execution node.
 * Design Decision: Contains both static node configuration and dynamic status
 * to enable intelligent task distribution and load balancing.
 */
public class NodeInfo {
    public enum Status {
        AVAILABLE, BUSY, OFFLINE, ERROR
    }
    
    private final String nodeId;
    private final String hostname;
    private final int port;
    private final int maxConcurrentTasks;
    private final Status status;
    private final int currentTasks;
    private final LocalDateTime lastHeartbeat;
    
    @JsonCreator
    public NodeInfo(
            @JsonProperty("nodeId") String nodeId,
            @JsonProperty("hostname") String hostname,
            @JsonProperty("port") int port,
            @JsonProperty("maxConcurrentTasks") int maxConcurrentTasks,
            @JsonProperty("status") Status status,
            @JsonProperty("currentTasks") int currentTasks,
            @JsonProperty("lastHeartbeat") LocalDateTime lastHeartbeat) {
        this.nodeId = Objects.requireNonNull(nodeId, "Node ID cannot be null");
        this.hostname = Objects.requireNonNull(hostname, "Hostname cannot be null");
        this.port = port;
        this.maxConcurrentTasks = maxConcurrentTasks;
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.currentTasks = currentTasks;
        this.lastHeartbeat = lastHeartbeat;
    }
    
    public String getNodeId() { return nodeId; }
    public String getHostname() { return hostname; }
    public int getPort() { return port; }
    public int getMaxConcurrentTasks() { return maxConcurrentTasks; }
    public Status getStatus() { return status; }
    public int getCurrentTasks() { return currentTasks; }
    public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
    
    public boolean canAcceptTask() {
        return status == Status.AVAILABLE && currentTasks < maxConcurrentTasks;
    }
    
    public double getLoadPercentage() {
        return maxConcurrentTasks > 0 ? (double) currentTasks / maxConcurrentTasks * 100 : 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeInfo)) return false;
        NodeInfo nodeInfo = (NodeInfo) o;
        return Objects.equals(nodeId, nodeInfo.nodeId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }
    
    @Override
    public String toString() {
        return String.format("NodeInfo{id='%s', host='%s:%d', status=%s, load=%d/%d}", 
                           nodeId, hostname, port, status, currentTasks, maxConcurrentTasks);
    }
}

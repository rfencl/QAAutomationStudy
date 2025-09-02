package com.qa.distributed.coordinator;

import com.qa.distributed.common.NodeInfo;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Registry for managing distributed test execution nodes.
 * Design Decision: Thread-safe registry with automatic node health monitoring
 * and intelligent load balancing for optimal task distribution.
 */
public class NodeRegistry {
    private final Map<String, NodeInfo> nodes;
    private final int heartbeatTimeoutSeconds;
    
    public NodeRegistry(int heartbeatTimeoutSeconds) {
        this.nodes = new ConcurrentHashMap<>();
        this.heartbeatTimeoutSeconds = heartbeatTimeoutSeconds;
    }
    
    /**
     * Register a new node in the system.
     * Design Decision: Automatic registration allows dynamic scaling
     * without manual configuration changes.
     */
    public void registerNode(NodeInfo nodeInfo) {
        nodes.put(nodeInfo.getNodeId(), nodeInfo);
        System.out.println("Node registered: " + nodeInfo);
    }
    
    /**
     * Update node status (typically from heartbeat).
     */
    public void updateNodeStatus(String nodeId, NodeInfo.Status status, int currentTasks) {
        NodeInfo existingNode = nodes.get(nodeId);
        if (existingNode != null) {
            NodeInfo updatedNode = new NodeInfo(
                existingNode.getNodeId(),
                existingNode.getHostname(),
                existingNode.getPort(),
                existingNode.getMaxConcurrentTasks(),
                status,
                currentTasks,
                LocalDateTime.now()
            );
            nodes.put(nodeId, updatedNode);
        }
    }
    
    /**
     * Get best available node for task assignment.
     * Design Decision: Load-based selection ensures even distribution
     * and prevents node overloading.
     */
    public NodeInfo getBestAvailableNode() {
        return nodes.values().stream()
            .filter(NodeInfo::canAcceptTask)
            .filter(this::isNodeHealthy)
            .min((n1, n2) -> Double.compare(n1.getLoadPercentage(), n2.getLoadPercentage()))
            .orElse(null);
    }
    
    /**
     * Get all available nodes for monitoring.
     */
    public List<NodeInfo> getAvailableNodes() {
        return nodes.values().stream()
            .filter(NodeInfo::canAcceptTask)
            .filter(this::isNodeHealthy)
            .collect(Collectors.toList());
    }
    
    /**
     * Check if node is healthy based on heartbeat timeout.
     * Design Decision: Automatic health detection prevents task assignment
     * to unresponsive nodes.
     */
    private boolean isNodeHealthy(NodeInfo node) {
        if (node.getLastHeartbeat() == null) {
            return false;
        }
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(heartbeatTimeoutSeconds);
        return node.getLastHeartbeat().isAfter(cutoff);
    }
    
    /**
     * Remove unhealthy nodes from registry.
     */
    public void cleanupUnhealthyNodes() {
        List<String> unhealthyNodes = nodes.values().stream()
            .filter(node -> !isNodeHealthy(node))
            .map(NodeInfo::getNodeId)
            .collect(Collectors.toList());
            
        unhealthyNodes.forEach(nodeId -> {
            nodes.remove(nodeId);
            System.out.println("Removed unhealthy node: " + nodeId);
        });
    }
    
    public NodeInfo getNode(String nodeId) {
        return nodes.get(nodeId);
    }
    
    public int getNodeCount() {
        return nodes.size();
    }
    
    public int getAvailableNodeCount() {
        return getAvailableNodes().size();
    }
    
    /**
     * Get registry statistics for monitoring.
     */
    public String getRegistryStats() {
        long availableCount = nodes.values().stream()
            .filter(NodeInfo::canAcceptTask)
            .filter(this::isNodeHealthy)
            .count();
            
        return String.format("Node Registry - Total: %d, Available: %d, Healthy: %d", 
                           nodes.size(), availableCount, 
                           nodes.values().stream().mapToInt(n -> isNodeHealthy(n) ? 1 : 0).sum());
    }
}

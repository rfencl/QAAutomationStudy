package com.qa.distributed.coordinator;

import com.qa.distributed.common.*;
import com.qa.distributed.communication.*;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.List;

/**
 * Central coordinator for distributed test execution system.
 * Design Decision: Single coordinator simplifies architecture while providing
 * centralized task distribution, node management, and result aggregation.
 */
public class TestCoordinator {
    private final String coordinatorId;
    private final TaskQueue taskQueue;
    private final NodeRegistry nodeRegistry;
    private final HttpCommunicationService communicationService;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutor;
    private final ConcurrentHashMap<String, TestResult> results;
    private HttpServer httpServer;
    private volatile boolean running;
    
    public TestCoordinator(int port) {
        this.coordinatorId = "coordinator-" + UUID.randomUUID().toString().substring(0, 8);
        this.taskQueue = new TaskQueue();
        this.nodeRegistry = new NodeRegistry(30); // 30 second heartbeat timeout
        this.communicationService = new HttpCommunicationService();
        this.executorService = Executors.newFixedThreadPool(10);
        this.scheduledExecutor = Executors.newScheduledThreadPool(5);
        this.results = new ConcurrentHashMap<>();
        this.running = false;
        
        try {
            setupHttpServer(port);
        } catch (IOException e) {
            throw new RuntimeException("Failed to start coordinator server", e);
        }
    }
    
    /**
     * Setup HTTP server for receiving messages from nodes.
     * Design Decision: HTTP server provides simple, standardized communication
     * protocol that works across different platforms and networks.
     */
    private void setupHttpServer(int port) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Handle node registration
        httpServer.createContext("/register", new NodeRegistrationHandler());
        
        // Handle test results
        httpServer.createContext("/result", new TestResultHandler());
        
        // Handle heartbeats
        httpServer.createContext("/heartbeat", new HeartbeatHandler());
        
        // Health check endpoint
        httpServer.createContext("/health", exchange -> {
            String response = "Coordinator healthy - " + taskQueue.getQueueStats();
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });
        
        httpServer.setExecutor(executorService);
    }
    
    /**
     * Start the coordinator and begin task distribution.
     */
    public void start() {
        if (running) return;
        
        running = true;
        httpServer.start();
        
        // Start task distribution loop
        executorService.submit(this::taskDistributionLoop);
        
        // Start periodic cleanup of unhealthy nodes
        scheduledExecutor.scheduleAtFixedRate(
            nodeRegistry::cleanupUnhealthyNodes, 30, 30, TimeUnit.SECONDS);
        
        // Start periodic status reporting
        scheduledExecutor.scheduleAtFixedRate(
            this::printStatus, 10, 10, TimeUnit.SECONDS);
        
        System.out.println("Test Coordinator started on port " + httpServer.getAddress().getPort());
    }
    
    /**
     * Main task distribution loop.
     * Design Decision: Continuous loop with blocking queue operations
     * ensures immediate task distribution when nodes become available.
     */
    private void taskDistributionLoop() {
        while (running) {
            try {
                TestTask task = taskQueue.getNextTask();
                NodeInfo bestNode = nodeRegistry.getBestAvailableNode();
                
                if (bestNode != null) {
                    assignTaskToNode(task, bestNode);
                } else {
                    // No available nodes, requeue task
                    taskQueue.requeueTask(task.getTaskId());
                    Thread.sleep(1000); // Wait before retrying
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Assign task to specific node.
     */
    private void assignTaskToNode(TestTask task, NodeInfo node) {
        String url = String.format("http://%s:%d/execute", node.getHostname(), node.getPort());
        
        Message<TestTask> message = new Message<>(
            UUID.randomUUID().toString(),
            Message.Type.TASK_ASSIGNMENT,
            coordinatorId,
            node.getNodeId(),
            task,
            LocalDateTime.now()
        );
        
        communicationService.sendMessageAsync(url, message)
            .thenAccept(success -> {
                if (!success) {
                    System.err.println("Failed to assign task " + task.getTaskId() + " to node " + node.getNodeId());
                    taskQueue.requeueTask(task.getTaskId());
                } else {
                    System.out.println("Assigned task " + task.getTaskId() + " to node " + node.getNodeId());
                }
            });
    }
    
    /**
     * Add test task to execution queue.
     */
    public void submitTask(TestTask task) {
        taskQueue.addTask(task);
    }
    
    /**
     * Add multiple test tasks.
     */
    public void submitTasks(List<TestTask> tasks) {
        tasks.forEach(this::submitTask);
    }
    
    /**
     * Get all test results.
     */
    public List<TestResult> getResults() {
        return List.copyOf(results.values());
    }
    
    /**
     * Wait for all tasks to complete with timeout.
     */
    public boolean waitForCompletion(long timeoutSeconds) {
        long endTime = System.currentTimeMillis() + (timeoutSeconds * 1000);
        
        while (System.currentTimeMillis() < endTime) {
            if (taskQueue.isEmpty()) {
                return true;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }
    
    private void printStatus() {
        System.out.println("=== Coordinator Status ===");
        System.out.println(taskQueue.getQueueStats());
        System.out.println(nodeRegistry.getRegistryStats());
        System.out.println("Results collected: " + results.size());
        System.out.println("========================");
    }
    
    public void shutdown() {
        running = false;
        httpServer.stop(5);
        executorService.shutdown();
        scheduledExecutor.shutdown();
        communicationService.shutdown();
        System.out.println("Test Coordinator shutdown complete");
    }
    
    // HTTP Handlers
    private class NodeRegistrationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                try (InputStream is = exchange.getRequestBody()) {
                    String body = new String(is.readAllBytes());
                    Message<NodeInfo> message = communicationService.parseMessage(body, NodeInfo.class);
                    nodeRegistry.registerNode(message.getPayload());
                    
                    exchange.sendResponseHeaders(200, 0);
                } catch (Exception e) {
                    exchange.sendResponseHeaders(400, 0);
                }
            } else {
                exchange.sendResponseHeaders(405, 0);
            }
            exchange.close();
        }
    }
    
    private class TestResultHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                try (InputStream is = exchange.getRequestBody()) {
                    String body = new String(is.readAllBytes());
                    Message<TestResult> message = communicationService.parseMessage(body, TestResult.class);
                    TestResult result = message.getPayload();
                    
                    results.put(result.getTaskId(), result);
                    taskQueue.completeTask(result.getTaskId());
                    
                    System.out.println("Received result: " + result);
                    exchange.sendResponseHeaders(200, 0);
                } catch (Exception e) {
                    exchange.sendResponseHeaders(400, 0);
                }
            } else {
                exchange.sendResponseHeaders(405, 0);
            }
            exchange.close();
        }
    }
    
    private class HeartbeatHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                try (InputStream is = exchange.getRequestBody()) {
                    String body = new String(is.readAllBytes());
                    Message<NodeInfo> message = communicationService.parseMessage(body, NodeInfo.class);
                    NodeInfo nodeInfo = message.getPayload();
                    
                    nodeRegistry.updateNodeStatus(
                        nodeInfo.getNodeId(), 
                        nodeInfo.getStatus(), 
                        nodeInfo.getCurrentTasks()
                    );
                    
                    exchange.sendResponseHeaders(200, 0);
                } catch (Exception e) {
                    exchange.sendResponseHeaders(400, 0);
                }
            } else {
                exchange.sendResponseHeaders(405, 0);
            }
            exchange.close();
        }
    }
}

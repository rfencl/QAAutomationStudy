package com.qa.distributed.node;

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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Distributed test execution node that receives and executes test tasks.
 * Design Decision: Autonomous node with self-registration, heartbeat,
 * and load management for scalable distributed testing.
 */
public class TestNode {
    private final String nodeId;
    private final String hostname;
    private final int port;
    private final int maxConcurrentTasks;
    private final String coordinatorUrl;
    
    private final TestExecutor testExecutor;
    private final HttpCommunicationService communicationService;
    private final AtomicInteger currentTasks;
    private final ScheduledExecutorService scheduledExecutor;
    private HttpServer httpServer;
    private volatile boolean running;
    
    public TestNode(String hostname, int port, int maxConcurrentTasks, String coordinatorUrl) {
        this.nodeId = "node-" + UUID.randomUUID().toString().substring(0, 8);
        this.hostname = hostname;
        this.port = port;
        this.maxConcurrentTasks = maxConcurrentTasks;
        this.coordinatorUrl = coordinatorUrl;
        
        this.testExecutor = new TestExecutor(nodeId, maxConcurrentTasks);
        this.communicationService = new HttpCommunicationService();
        this.currentTasks = new AtomicInteger(0);
        this.scheduledExecutor = Executors.newScheduledThreadPool(3);
        this.running = false;
        
        try {
            setupHttpServer();
        } catch (IOException e) {
            throw new RuntimeException("Failed to start node server", e);
        }
    }
    
    /**
     * Setup HTTP server for receiving task assignments.
     */
    private void setupHttpServer() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(hostname, port), 0);
        
        // Handle task execution requests
        httpServer.createContext("/execute", new TaskExecutionHandler());
        
        // Health check endpoint
        httpServer.createContext("/health", exchange -> {
            String response = String.format("Node %s healthy - Tasks: %d/%d", 
                                          nodeId, currentTasks.get(), maxConcurrentTasks);
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });
        
        httpServer.setExecutor(Executors.newFixedThreadPool(5));
    }
    
    /**
     * Start the node and register with coordinator.
     */
    public void start() {
        if (running) return;
        
        running = true;
        httpServer.start();
        
        // Register with coordinator
        registerWithCoordinator();
        
        // Start periodic heartbeat
        scheduledExecutor.scheduleAtFixedRate(
            this::sendHeartbeat, 5, 10, TimeUnit.SECONDS);
        
        System.out.println("Test Node " + nodeId + " started on " + hostname + ":" + port);
    }
    
    /**
     * Register node with coordinator.
     * Design Decision: Self-registration enables dynamic node discovery
     * and automatic scaling without manual configuration.
     */
    private void registerWithCoordinator() {
        NodeInfo nodeInfo = new NodeInfo(
            nodeId, hostname, port, maxConcurrentTasks,
            NodeInfo.Status.AVAILABLE, 0, LocalDateTime.now()
        );
        
        Message<NodeInfo> message = new Message<>(
            UUID.randomUUID().toString(),
            Message.Type.NODE_REGISTRATION,
            nodeId,
            "coordinator",
            nodeInfo,
            LocalDateTime.now()
        );
        
        try {
            boolean success = communicationService.sendMessage(coordinatorUrl + "/register", message);
            if (success) {
                System.out.println("Successfully registered with coordinator");
            } else {
                System.err.println("Failed to register with coordinator");
            }
        } catch (Exception e) {
            System.err.println("Error registering with coordinator: " + e.getMessage());
        }
    }
    
    /**
     * Send periodic heartbeat to coordinator.
     * Design Decision: Regular heartbeats enable coordinator to detect
     * node failures and adjust task distribution accordingly.
     */
    private void sendHeartbeat() {
        NodeInfo.Status status = currentTasks.get() < maxConcurrentTasks ? 
            NodeInfo.Status.AVAILABLE : NodeInfo.Status.BUSY;
            
        NodeInfo nodeInfo = new NodeInfo(
            nodeId, hostname, port, maxConcurrentTasks,
            status, currentTasks.get(), LocalDateTime.now()
        );
        
        Message<NodeInfo> message = new Message<>(
            UUID.randomUUID().toString(),
            Message.Type.HEARTBEAT,
            nodeId,
            "coordinator",
            nodeInfo,
            LocalDateTime.now()
        );
        
        communicationService.sendMessageAsync(coordinatorUrl + "/heartbeat", message)
            .thenAccept(success -> {
                if (!success) {
                    System.err.println("Failed to send heartbeat to coordinator");
                }
            });
    }
    
    /**
     * Execute test task and send result back to coordinator.
     */
    private void executeTask(TestTask task) {
        currentTasks.incrementAndGet();
        
        testExecutor.executeTaskAsync(task)
            .thenAccept(result -> {
                currentTasks.decrementAndGet();
                sendResultToCoordinator(result);
            })
            .exceptionally(throwable -> {
                currentTasks.decrementAndGet();
                System.err.println("Error executing task " + task.getTaskId() + ": " + throwable.getMessage());
                
                // Send error result
                TestResult errorResult = new TestResult(
                    task.getTaskId(), nodeId, TestResult.Status.ERROR,
                    "Node execution error: " + throwable.getMessage(),
                    throwable.toString(),
                    LocalDateTime.now(), LocalDateTime.now(), 0
                );
                sendResultToCoordinator(errorResult);
                return null;
            });
    }
    
    /**
     * Send test result back to coordinator.
     */
    private void sendResultToCoordinator(TestResult result) {
        Message<TestResult> message = new Message<>(
            UUID.randomUUID().toString(),
            Message.Type.TASK_RESULT,
            nodeId,
            "coordinator",
            result,
            LocalDateTime.now()
        );
        
        communicationService.sendMessageAsync(coordinatorUrl + "/result", message)
            .thenAccept(success -> {
                if (success) {
                    System.out.println("Sent result for task " + result.getTaskId());
                } else {
                    System.err.println("Failed to send result for task " + result.getTaskId());
                }
            });
    }
    
    public void shutdown() {
        running = false;
        httpServer.stop(5);
        scheduledExecutor.shutdown();
        testExecutor.shutdown();
        communicationService.shutdown();
        System.out.println("Test Node " + nodeId + " shutdown complete");
    }
    
    public String getNodeId() {
        return nodeId;
    }
    
    public int getCurrentTaskCount() {
        return currentTasks.get();
    }
    
    // HTTP Handler for task execution
    private class TaskExecutionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                try (InputStream is = exchange.getRequestBody()) {
                    String body = new String(is.readAllBytes());
                    Message<TestTask> message = communicationService.parseMessage(body, TestTask.class);
                    TestTask task = message.getPayload();
                    
                    if (currentTasks.get() < maxConcurrentTasks) {
                        executeTask(task);
                        exchange.sendResponseHeaders(200, 0);
                        System.out.println("Accepted task: " + task.getTaskId());
                    } else {
                        exchange.sendResponseHeaders(503, 0); // Service Unavailable
                        System.out.println("Rejected task (overloaded): " + task.getTaskId());
                    }
                } catch (Exception e) {
                    System.err.println("Error handling task execution: " + e.getMessage());
                    exchange.sendResponseHeaders(400, 0);
                }
            } else {
                exchange.sendResponseHeaders(405, 0);
            }
            exchange.close();
        }
    }
}

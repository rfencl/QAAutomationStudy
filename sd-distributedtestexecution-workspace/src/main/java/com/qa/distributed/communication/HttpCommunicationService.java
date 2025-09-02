package com.qa.distributed.communication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HTTP-based communication service for distributed test execution.
 * Design Decision: Uses HTTP for simplicity and wide compatibility.
 * Asynchronous operations prevent blocking during network communication.
 */
public class HttpCommunicationService {
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;
    private final ExecutorService executorService;
    
    public HttpCommunicationService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.httpClient = HttpClients.createDefault();
        this.executorService = Executors.newFixedThreadPool(10);
    }
    
    /**
     * Send message asynchronously to avoid blocking the caller.
     * Design Decision: Async communication prevents coordinator/node blocking
     * and improves overall system throughput.
     */
    public <T> CompletableFuture<Boolean> sendMessageAsync(String url, Message<T> message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return sendMessage(url, message);
            } catch (Exception e) {
                System.err.println("Failed to send message: " + e.getMessage());
                return false;
            }
        }, executorService);
    }
    
    /**
     * Synchronous message sending for cases where immediate response is needed.
     */
    public <T> boolean sendMessage(String url, Message<T> message) throws IOException {
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/json");
        
        String jsonMessage = objectMapper.writeValueAsString(message);
        post.setEntity(new StringEntity(jsonMessage));
        
        try (CloseableHttpResponse response = httpClient.execute(post)) {
            int statusCode = response.getStatusLine().getStatusCode();
            return statusCode >= 200 && statusCode < 300;
        }
    }
    
    /**
     * Parse incoming message from JSON string.
     * Design Decision: Centralized JSON parsing with proper error handling
     * ensures consistent message format across the system.
     */
    public <T> Message<T> parseMessage(String jsonMessage, Class<T> payloadType) throws IOException {
        return objectMapper.readValue(jsonMessage, 
            objectMapper.getTypeFactory().constructParametricType(Message.class, payloadType));
    }
    
    /**
     * Convert message to JSON string for transmission.
     */
    public <T> String serializeMessage(Message<T> message) throws IOException {
        return objectMapper.writeValueAsString(message);
    }
    
    public void shutdown() {
        try {
            executorService.shutdown();
            httpClient.close();
        } catch (IOException e) {
            System.err.println("Error shutting down communication service: " + e.getMessage());
        }
    }
}

package com.qa.microservices.virtualization;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Service virtualization using WireMock for isolated testing.
 * Design Decision: Provides controlled, predictable responses for dependent services.
 * This enables testing of edge cases, error conditions, and performance scenarios.
 */
public class ServiceVirtualization {
    private static final Logger logger = LoggerFactory.getLogger(ServiceVirtualization.class);
    
    private WireMockServer userServiceMock;
    private WireMockServer orderServiceMock;
    
    /**
     * Start virtual services with predefined stubs.
     * Design Decision: Separate ports for different services to simulate real microservices architecture.
     */
    public void startServices() {
        // User Service Mock on port 8081
        userServiceMock = new WireMockServer(WireMockConfiguration.options().port(8081));
        userServiceMock.start();
        WireMock.configureFor("localhost", 8081);
        setupUserServiceStubs();
        
        // Order Service Mock on port 8082
        orderServiceMock = new WireMockServer(WireMockConfiguration.options().port(8082));
        orderServiceMock.start();
        WireMock.configureFor("localhost", 8082);
        setupOrderServiceStubs();
        
        logger.info("Virtual services started - User Service: 8081, Order Service: 8082");
    }
    
    /**
     * Setup User Service stubs with various scenarios.
     */
    private void setupUserServiceStubs() {
        // Successful user retrieval
        stubFor(get(urlPathMatching("/users/user-123"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\n" +
                    "  \"id\": \"user-123\",\n" +
                    "  \"username\": \"testuser\",\n" +
                    "  \"email\": \"test@example.com\",\n" +
                    "  \"status\": \"ACTIVE\"\n" +
                    "}")));
        
        // User not found scenario
        stubFor(get(urlPathMatching("/users/user-404"))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\": \"User not found\"}")));
        
        // Service error scenario
        stubFor(get(urlPathMatching("/users/user-error"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\": \"Internal server error\"}")));
        
        // Slow response scenario for performance testing
        stubFor(get(urlPathMatching("/users/user-slow"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\n" +
                    "  \"id\": \"user-slow\",\n" +
                    "  \"username\": \"slowuser\",\n" +
                    "  \"email\": \"slow@example.com\",\n" +
                    "  \"status\": \"ACTIVE\"\n" +
                    "}")
                .withFixedDelay(3000))); // 3 second delay
        
        // Health check
        stubFor(get(urlEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("{\"status\": \"UP\"}")));
    }
    
    /**
     * Setup Order Service stubs with business logic scenarios.
     */
    private void setupOrderServiceStubs() {
        // Successful order creation
        stubFor(post(urlEqualTo("/orders"))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody("{\n" +
                    "  \"orderId\": \"order-123\",\n" +
                    "  \"userId\": \"user-123\",\n" +
                    "  \"totalAmount\": 99.99,\n" +
                    "  \"status\": \"PENDING\",\n" +
                    "  \"createdAt\": \"2023-01-01T10:00:00\"\n" +
                    "}")));
        
        // Order retrieval
        stubFor(get(urlPathMatching("/orders/order-123"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\n" +
                    "  \"orderId\": \"order-123\",\n" +
                    "  \"userId\": \"user-123\",\n" +
                    "  \"totalAmount\": 99.99,\n" +
                    "  \"status\": \"PENDING\",\n" +
                    "  \"createdAt\": \"2023-01-01T10:00:00\"\n" +
                    "}")));
        
        // Order status update
        stubFor(patch(urlPathMatching("/orders/.*/status"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\n" +
                    "  \"orderId\": \"order-123\",\n" +
                    "  \"userId\": \"user-123\",\n" +
                    "  \"totalAmount\": 99.99,\n" +
                    "  \"status\": \"CONFIRMED\",\n" +
                    "  \"createdAt\": \"2023-01-01T10:00:00\"\n" +
                    "}")));
        
        // Health check
        stubFor(get(urlEqualTo("/health"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("{\"status\": \"UP\"}")));
    }
    
    /**
     * Stop all virtual services and clean up resources.
     */
    public void stopServices() {
        if (userServiceMock != null && userServiceMock.isRunning()) {
            userServiceMock.stop();
            logger.info("User Service mock stopped");
        }
        
        if (orderServiceMock != null && orderServiceMock.isRunning()) {
            orderServiceMock.stop();
            logger.info("Order Service mock stopped");
        }
    }
    
    /**
     * Reset all stubs for clean test state.
     */
    public void resetStubs() {
        if (userServiceMock != null) {
            userServiceMock.resetAll();
        }
        if (orderServiceMock != null) {
            orderServiceMock.resetAll();
        }
    }
}

package com.qa.microservices.client;

import com.qa.microservices.models.Order;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Client for Order Service interactions.
 * Design Decision: Handles complex business operations and cross-service dependencies.
 */
public class OrderServiceClient {
    private final String baseUrl;
    
    public OrderServiceClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    /**
     * Create order with dependency on User Service.
     */
    public Order createOrder(Order order) {
        RequestSpecification request = RestAssured.given()
            .baseUri(baseUrl)
            .header("Content-Type", "application/json")
            .body(order);
            
        Response response = request.post("/orders");
        
        if (response.getStatusCode() == 201) {
            return response.as(Order.class);
        } else {
            throw new RuntimeException("Failed to create order: " + response.getStatusCode());
        }
    }
    
    /**
     * Get order by ID for verification testing.
     */
    public Order getOrderById(String orderId) {
        RequestSpecification request = RestAssured.given()
            .baseUri(baseUrl)
            .pathParam("orderId", orderId);
            
        Response response = request.get("/orders/{orderId}");
        
        if (response.getStatusCode() == 200) {
            return response.as(Order.class);
        } else if (response.getStatusCode() == 404) {
            return null;
        } else {
            throw new RuntimeException("Failed to get order: " + response.getStatusCode());
        }
    }
    
    /**
     * Update order status for workflow testing.
     */
    public Order updateOrderStatus(String orderId, String status) {
        RequestSpecification request = RestAssured.given()
            .baseUri(baseUrl)
            .header("Content-Type", "application/json")
            .pathParam("orderId", orderId)
            .body("{\"status\":\"" + status + "\"}");
            
        Response response = request.patch("/orders/{orderId}/status");
        
        if (response.getStatusCode() == 200) {
            return response.as(Order.class);
        } else {
            throw new RuntimeException("Failed to update order status: " + response.getStatusCode());
        }
    }
    
    public boolean isServiceHealthy() {
        try {
            Response response = RestAssured.given()
                .baseUri(baseUrl)
                .get("/health");
            return response.getStatusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}

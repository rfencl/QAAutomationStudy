package com.qa.microservices.client;

import com.qa.microservices.models.User;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Client for User Service interactions.
 * Design Decision: Encapsulates REST API calls with proper error handling and response parsing.
 * This abstraction allows for easy mocking and testing of service interactions.
 */
public class UserServiceClient {
    private final String baseUrl;
    
    public UserServiceClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    /**
     * Get user by ID with comprehensive error handling.
     */
    public User getUserById(String userId) {
        RequestSpecification request = RestAssured.given()
            .baseUri(baseUrl)
            .header("Content-Type", "application/json")
            .pathParam("userId", userId);
            
        Response response = request.get("/users/{userId}");
        
        if (response.getStatusCode() == 200) {
            return response.as(User.class);
        } else if (response.getStatusCode() == 404) {
            return null;
        } else {
            throw new RuntimeException("Failed to get user: " + response.getStatusCode());
        }
    }
    
    /**
     * Create new user with validation.
     */
    public User createUser(User user) {
        RequestSpecification request = RestAssured.given()
            .baseUri(baseUrl)
            .header("Content-Type", "application/json")
            .body(user);
            
        Response response = request.post("/users");
        
        if (response.getStatusCode() == 201) {
            return response.as(User.class);
        } else {
            throw new RuntimeException("Failed to create user: " + response.getStatusCode());
        }
    }
    
    /**
     * Update user status for workflow testing.
     */
    public User updateUserStatus(String userId, String status) {
        RequestSpecification request = RestAssured.given()
            .baseUri(baseUrl)
            .header("Content-Type", "application/json")
            .pathParam("userId", userId)
            .body("{\"status\":\"" + status + "\"}");
            
        Response response = request.patch("/users/{userId}/status");
        
        if (response.getStatusCode() == 200) {
            return response.as(User.class);
        } else {
            throw new RuntimeException("Failed to update user status: " + response.getStatusCode());
        }
    }
    
    /**
     * Health check for service availability testing.
     */
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

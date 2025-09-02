package com.qa.microservices.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User domain model for microservices testing.
 * Design Decision: Simple POJO with Jackson annotations for JSON serialization.
 * This represents the contract between services and ensures consistent data structure.
 */
public class User {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("status")
    private String status;
    
    // Default constructor for Jackson
    public User() {}
    
    public User(String id, String username, String email, String status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.status = status;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return String.format("User{id='%s', username='%s', email='%s', status='%s'}", 
            id, username, email, status);
    }
}

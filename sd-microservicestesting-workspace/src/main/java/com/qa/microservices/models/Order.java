package com.qa.microservices.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order domain model for microservices testing.
 * Design Decision: Represents complex business entity with relationships to other services.
 */
public class Order {
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("items")
    private List<OrderItem> items;
    
    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    public Order() {}
    
    public Order(String orderId, String userId, List<OrderItem> items, 
                 BigDecimal totalAmount, String status, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return String.format("Order{orderId='%s', userId='%s', totalAmount=%s, status='%s'}", 
            orderId, userId, totalAmount, status);
    }
}

class OrderItem {
    @JsonProperty("productId")
    private String productId;
    
    @JsonProperty("quantity")
    private int quantity;
    
    @JsonProperty("price")
    private BigDecimal price;
    
    public OrderItem() {}
    
    public OrderItem(String productId, int quantity, BigDecimal price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }
    
    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}

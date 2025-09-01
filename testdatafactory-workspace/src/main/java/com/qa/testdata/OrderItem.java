package com.qa.testdata;

import java.math.BigDecimal;

public class OrderItem {
    private String id;
    private String productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
    
    // Constructors
    public OrderItem() {}
    
    public OrderItem(String id, String productId, String productName, int quantity, BigDecimal price) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    @Override
    public String toString() {
        return String.format("OrderItem{id='%s', productName='%s', quantity=%d, price=%s}", 
            id, productName, quantity, price);
    }
}

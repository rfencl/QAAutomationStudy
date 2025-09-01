package com.qa.testdata;

import com.github.javafaker.Faker;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderBuilder {
    private final Order order = new Order();
    private final Faker faker = new Faker();
    private final TestDataFactory factory;
    
    public OrderBuilder(TestDataFactory factory) {
        this.factory = factory;
        // Set default values
        order.setId(UUID.randomUUID().toString());
        order.setStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());
        order.setItems(new ArrayList<>());
        order.setTotalAmount(BigDecimal.ZERO);
    }
    
    public OrderBuilder forUser(User user) {
        order.setUserId(user.getId());
        return this;
    }
    
    public OrderBuilder forUserId(String userId) {
        order.setUserId(userId);
        return this;
    }
    
    public OrderBuilder withStatus(String status) {
        order.setStatus(status);
        return this;
    }
    
    public OrderBuilder withItems(int count) {
        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        
        for (int i = 0; i < count; i++) {
            OrderItem item = createRandomOrderItem();
            items.add(item);
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        
        order.setItems(items);
        order.setTotalAmount(total);
        return this;
    }
    
    public OrderBuilder withTotalAmount(BigDecimal amount) {
        order.setTotalAmount(amount);
        return this;
    }
    
    public OrderBuilder withOrderDate(LocalDateTime orderDate) {
        order.setOrderDate(orderDate);
        return this;
    }
    
    private OrderItem createRandomOrderItem() {
        return new OrderItem(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            faker.commerce().productName(),
            faker.number().numberBetween(1, 5),
            BigDecimal.valueOf(faker.number().randomDouble(2, 10, 100))
        );
    }
    
    public Order build() {
        validateOrder();
        return order;
    }
    
    public Order save() {
        Order builtOrder = build();
        return factory.save(builtOrder);
    }
    
    private void validateOrder() {
        if (order.getUserId() == null || order.getUserId().isEmpty()) {
            throw new IllegalStateException("Order userId is required");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalStateException("Order must have at least one item");
        }
    }
}

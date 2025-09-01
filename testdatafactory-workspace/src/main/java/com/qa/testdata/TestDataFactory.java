package com.qa.testdata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TestDataFactory {
    private final ConcurrentMap<Class<?>, List<Object>> createdEntities = new ConcurrentHashMap<>();
    
    public UserBuilder createUser() {
        return new UserBuilder(this);
    }
    
    public OrderBuilder createOrder() {
        return new OrderBuilder(this);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T save(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        
        Class<?> entityClass = entity.getClass();
        createdEntities.computeIfAbsent(entityClass, k -> new ArrayList<>()).add(entity);
        
        System.out.printf("[TEST DATA] Saved %s: %s%n", entityClass.getSimpleName(), entity);
        return entity;
    }
    
    @SuppressWarnings("unchecked")
    public <T> List<T> getCreatedEntities(Class<T> entityClass) {
        List<Object> entities = createdEntities.getOrDefault(entityClass, new ArrayList<>());
        return (List<T>) entities;
    }
    
    public void cleanup() {
        int totalEntities = createdEntities.values().stream()
            .mapToInt(List::size)
            .sum();
            
        System.out.printf("[TEST DATA] Cleaning up %d entities%n", totalEntities);
        
        createdEntities.forEach((entityClass, entities) -> {
            System.out.printf("[TEST DATA] Cleaning up %d %s entities%n", 
                entities.size(), entityClass.getSimpleName());
        });
        
        createdEntities.clear();
    }
    
    public int getTotalCreatedEntities() {
        return createdEntities.values().stream()
            .mapToInt(List::size)
            .sum();
    }
    
    public boolean hasCreatedEntities(Class<?> entityClass) {
        return createdEntities.containsKey(entityClass) && 
               !createdEntities.get(entityClass).isEmpty();
    }
}

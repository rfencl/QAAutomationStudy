package com.qa.db;

import java.sql.Connection;
import java.time.LocalDateTime;

public class PooledConnection {
    private final Connection connection;
    private final LocalDateTime createdAt;
    private LocalDateTime lastUsed;
    private boolean inUse;
    
    public PooledConnection(Connection connection) {
        this.connection = connection;
        this.createdAt = LocalDateTime.now();
        this.lastUsed = LocalDateTime.now();
        this.inUse = false;
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getLastUsed() {
        return lastUsed;
    }
    
    public void setLastUsed(LocalDateTime lastUsed) {
        this.lastUsed = lastUsed;
    }
    
    public boolean isInUse() {
        return inUse;
    }
    
    public void setInUse(boolean inUse) {
        this.inUse = inUse;
        if (inUse) {
            this.lastUsed = LocalDateTime.now();
        }
    }
    
    public boolean isExpired(int maxLifetimeMs) {
        return createdAt.plusNanos(maxLifetimeMs * 1_000_000L).isBefore(LocalDateTime.now());
    }
    
    public boolean isIdle(int idleTimeoutMs) {
        return !inUse && lastUsed.plusNanos(idleTimeoutMs * 1_000_000L).isBefore(LocalDateTime.now());
    }
    
    public boolean isValid() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(1);
        } catch (Exception e) {
            return false;
        }
    }
}

package com.qa.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class DatabaseConnectionPool {
    private final DatabaseConfig config;
    private final BlockingQueue<PooledConnection> availableConnections;
    private final BlockingQueue<PooledConnection> usedConnections;
    private final AtomicInteger totalConnections;
    private final ReentrantLock lock;
    private volatile boolean shutdown;
    
    public DatabaseConnectionPool(DatabaseConfig config) {
        this.config = config;
        this.availableConnections = new LinkedBlockingQueue<>();
        this.usedConnections = new LinkedBlockingQueue<>();
        this.totalConnections = new AtomicInteger(0);
        this.lock = new ReentrantLock();
        this.shutdown = false;
        
        initializePool();
        startMaintenanceThread();
    }
    
    private void initializePool() {
        try {
            Class.forName(config.getDriverClassName());
            for (int i = 0; i < config.getMinPoolSize(); i++) {
                createConnection();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize connection pool", e);
        }
    }
    
    private PooledConnection createConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(
            config.getUrl(), config.getUsername(), config.getPassword());
        PooledConnection pooledConn = new PooledConnection(conn);
        availableConnections.offer(pooledConn);
        totalConnections.incrementAndGet();
        return pooledConn;
    }
    
    public Connection getConnection() throws SQLException {
        if (shutdown) {
            throw new SQLException("Connection pool is shutdown");
        }
        
        try {
            PooledConnection pooledConn = availableConnections.poll(
                config.getConnectionTimeout(), TimeUnit.MILLISECONDS);
            
            if (pooledConn == null) {
                lock.lock();
                try {
                    if (totalConnections.get() < config.getMaxPoolSize()) {
                        pooledConn = createConnection();
                        availableConnections.poll(); // Remove it immediately
                    } else {
                        throw new SQLException("Connection pool exhausted");
                    }
                } finally {
                    lock.unlock();
                }
            }
            
            if (pooledConn != null && pooledConn.isValid()) {
                pooledConn.setInUse(true);
                usedConnections.offer(pooledConn);
                return pooledConn.getConnection();
            } else {
                throw new SQLException("Unable to get valid connection");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for connection", e);
        }
    }
    
    public void releaseConnection(Connection connection) {
        if (connection == null) return;
        
        PooledConnection pooledConn = findPooledConnection(connection);
        if (pooledConn != null) {
            pooledConn.setInUse(false);
            usedConnections.remove(pooledConn);
            
            if (pooledConn.isValid() && !shutdown) {
                availableConnections.offer(pooledConn);
            } else {
                closeConnection(pooledConn);
            }
        }
    }
    
    private PooledConnection findPooledConnection(Connection connection) {
        return usedConnections.stream()
            .filter(pc -> pc.getConnection() == connection)
            .findFirst()
            .orElse(null);
    }
    
    private void closeConnection(PooledConnection pooledConn) {
        try {
            pooledConn.getConnection().close();
            totalConnections.decrementAndGet();
        } catch (SQLException e) {
            // Log error but continue
        }
    }
    
    private void startMaintenanceThread() {
        Thread maintenanceThread = new Thread(() -> {
            while (!shutdown) {
                try {
                    Thread.sleep(30000); // Run every 30 seconds
                    performMaintenance();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        maintenanceThread.setDaemon(true);
        maintenanceThread.start();
    }
    
    private void performMaintenance() {
        // Remove expired and idle connections
        availableConnections.removeIf(pc -> {
            if (pc.isExpired(config.getMaxLifetime()) || 
                pc.isIdle(config.getIdleTimeout()) || 
                !pc.isValid()) {
                closeConnection(pc);
                return true;
            }
            return false;
        });
        
        // Ensure minimum pool size
        lock.lock();
        try {
            while (totalConnections.get() < config.getMinPoolSize()) {
                try {
                    createConnection();
                } catch (SQLException e) {
                    break; // Stop trying if we can't create connections
                }
            }
        } finally {
            lock.unlock();
        }
    }
    
    public void shutdown() {
        shutdown = true;
        
        // Close all available connections
        PooledConnection pc;
        while ((pc = availableConnections.poll()) != null) {
            closeConnection(pc);
        }
        
        // Close all used connections (force close)
        while ((pc = usedConnections.poll()) != null) {
            closeConnection(pc);
        }
    }
    
    public int getAvailableConnections() {
        return availableConnections.size();
    }
    
    public int getUsedConnections() {
        return usedConnections.size();
    }
    
    public int getTotalConnections() {
        return totalConnections.get();
    }
}

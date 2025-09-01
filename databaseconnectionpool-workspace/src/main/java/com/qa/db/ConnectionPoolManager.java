package com.qa.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionPoolManager {
    private static final ConnectionPoolManager INSTANCE = new ConnectionPoolManager();
    private final ConcurrentHashMap<String, DatabaseConnectionPool> pools;
    
    private ConnectionPoolManager() {
        this.pools = new ConcurrentHashMap<>();
    }
    
    public static ConnectionPoolManager getInstance() {
        return INSTANCE;
    }
    
    public void createPool(String poolName, DatabaseConfig config) {
        if (pools.containsKey(poolName)) {
            throw new IllegalArgumentException("Pool with name '" + poolName + "' already exists");
        }
        pools.put(poolName, new DatabaseConnectionPool(config));
    }
    
    public Connection getConnection(String poolName) throws SQLException {
        DatabaseConnectionPool pool = pools.get(poolName);
        if (pool == null) {
            throw new SQLException("Pool '" + poolName + "' not found");
        }
        return pool.getConnection();
    }
    
    public void releaseConnection(String poolName, Connection connection) {
        DatabaseConnectionPool pool = pools.get(poolName);
        if (pool != null) {
            pool.releaseConnection(connection);
        }
    }
    
    public void shutdownPool(String poolName) {
        DatabaseConnectionPool pool = pools.remove(poolName);
        if (pool != null) {
            pool.shutdown();
        }
    }
    
    public void shutdownAllPools() {
        pools.values().forEach(DatabaseConnectionPool::shutdown);
        pools.clear();
    }
    
    public DatabaseConnectionPool getPool(String poolName) {
        return pools.get(poolName);
    }
    
    public boolean poolExists(String poolName) {
        return pools.containsKey(poolName);
    }
}

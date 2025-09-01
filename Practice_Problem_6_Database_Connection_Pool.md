# Practice Problem 6: Create a Database Connection Pool Manager

## Problem Statement
Design and implement a robust database connection pool manager that efficiently manages database connections for test automation, supports multiple databases, provides connection health monitoring, and ensures optimal resource utilization with proper cleanup.

## Requirements

### Functional Requirements
1. **Connection Pooling**: Maintain pool of reusable database connections
2. **Multi-Database Support**: Handle multiple database types (MySQL, PostgreSQL, H2)
3. **Health Monitoring**: Monitor connection health and replace stale connections
4. **Load Balancing**: Distribute connections across multiple database instances
5. **Configuration Management**: Support environment-specific configurations
6. **Metrics Collection**: Track pool usage, performance, and health metrics
7. **Graceful Shutdown**: Proper cleanup during application shutdown

### Non-Functional Requirements
1. **Performance**: Sub-millisecond connection acquisition
2. **Reliability**: 99.9% connection availability
3. **Scalability**: Support 100+ concurrent connections
4. **Thread Safety**: Safe for multi-threaded test execution

## Implementation Approach

### Core Components
```java
public class DatabaseConnectionPoolManager {
    private final Map<String, ConnectionPool> connectionPools;
    private final PoolConfiguration configuration;
    private final HealthMonitor healthMonitor;
    private final MetricsCollector metricsCollector;
}
```

### Connection Pool Implementation
```java
@Component
public class ConnectionPool {
    private final BlockingQueue<PooledConnection> availableConnections;
    private final Set<PooledConnection> activeConnections;
    private final AtomicInteger totalConnections = new AtomicInteger(0);
    private final PoolConfiguration config;
    
    public Connection getConnection() throws SQLException {
        PooledConnection connection = availableConnections.poll(
            config.getConnectionTimeout(), TimeUnit.MILLISECONDS);
            
        if (connection == null) {
            if (totalConnections.get() < config.getMaxPoolSize()) {
                connection = createNewConnection();
            } else {
                throw new SQLException("Connection pool exhausted");
            }
        }
        
        if (!isConnectionValid(connection)) {
            connection = createNewConnection();
        }
        
        activeConnections.add(connection);
        return connection.getConnection();
    }
    
    public void releaseConnection(Connection connection) {
        PooledConnection pooledConnection = findPooledConnection(connection);
        if (pooledConnection != null) {
            activeConnections.remove(pooledConnection);
            
            if (isConnectionValid(pooledConnection)) {
                availableConnections.offer(pooledConnection);
            } else {
                closeConnection(pooledConnection);
                totalConnections.decrementAndGet();
            }
        }
    }
}
```

## Success Criteria
1. **Connection Reuse**: 95%+ connection reuse rate
2. **Acquisition Speed**: <1ms average connection acquisition time
3. **Pool Efficiency**: Maintain optimal pool size based on usage
4. **Health Monitoring**: Detect and replace unhealthy connections
5. **Resource Cleanup**: Zero connection leaks

## Deliverables
1. `ConnectionPoolManager.java` - Main pool manager
2. `PooledConnection.java` - Connection wrapper
3. `HealthMonitor.java` - Connection health monitoring
4. `PoolConfiguration.java` - Configuration management
5. `ConnectionPoolTest.java` - Comprehensive tests

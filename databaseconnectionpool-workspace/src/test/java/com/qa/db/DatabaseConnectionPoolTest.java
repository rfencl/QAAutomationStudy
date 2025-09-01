package com.qa.db;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DatabaseConnectionPoolTest {
    private DatabaseConnectionPool pool;
    private DatabaseConfig config;
    
    @BeforeMethod
    public void setUp() {
        config = new DatabaseConfig(
            "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
            "sa",
            "",
            "org.h2.Driver"
        );
        config.setMinPoolSize(3);
        config.setMaxPoolSize(10);
        config.setConnectionTimeout(5000);
        
        pool = new DatabaseConnectionPool(config);
    }
    
    @AfterMethod
    public void tearDown() {
        if (pool != null) {
            pool.shutdown();
        }
    }
    
    @Test
    public void testPoolInitialization() {
        Assert.assertEquals(pool.getTotalConnections(), 3);
        Assert.assertEquals(pool.getAvailableConnections(), 3);
        Assert.assertEquals(pool.getUsedConnections(), 0);
    }
    
    @Test
    public void testGetConnection() throws SQLException {
        Connection conn = pool.getConnection();
        Assert.assertNotNull(conn);
        Assert.assertFalse(conn.isClosed());
        Assert.assertEquals(pool.getAvailableConnections(), 2);
        Assert.assertEquals(pool.getUsedConnections(), 1);
        
        pool.releaseConnection(conn);
    }
    
    @Test
    public void testReleaseConnection() throws SQLException {
        Connection conn = pool.getConnection();
        pool.releaseConnection(conn);
        
        Assert.assertEquals(pool.getAvailableConnections(), 3);
        Assert.assertEquals(pool.getUsedConnections(), 0);
    }
    
    @Test
    public void testMultipleConnections() throws SQLException {
        List<Connection> connections = new ArrayList<>();
        
        // Get 5 connections
        for (int i = 0; i < 5; i++) {
            connections.add(pool.getConnection());
        }
        
        Assert.assertEquals(pool.getUsedConnections(), 5);
        Assert.assertEquals(pool.getTotalConnections(), 5);
        
        // Release all connections
        for (Connection conn : connections) {
            pool.releaseConnection(conn);
        }
        
        Assert.assertEquals(pool.getUsedConnections(), 0);
        Assert.assertEquals(pool.getAvailableConnections(), 5);
    }
    
    @Test
    public void testPoolExpansion() throws SQLException {
        List<Connection> connections = new ArrayList<>();
        
        // Get more connections than initial pool size
        for (int i = 0; i < 8; i++) {
            connections.add(pool.getConnection());
        }
        
        Assert.assertEquals(pool.getTotalConnections(), 8);
        Assert.assertEquals(pool.getUsedConnections(), 8);
        Assert.assertEquals(pool.getAvailableConnections(), 0);
        
        // Release connections
        for (Connection conn : connections) {
            pool.releaseConnection(conn);
        }
    }
    
    @Test(expectedExceptions = SQLException.class)
    public void testPoolExhaustion() throws SQLException {
        List<Connection> connections = new ArrayList<>();
        
        // Try to get more than max pool size
        for (int i = 0; i < config.getMaxPoolSize() + 1; i++) {
            connections.add(pool.getConnection());
        }
    }
    
    @Test
    public void testConcurrentAccess() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    Connection conn = pool.getConnection();
                    Thread.sleep(100); // Simulate work
                    pool.releaseConnection(conn);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        
        // All connections should be back in the pool
        Assert.assertEquals(pool.getUsedConnections(), 0);
        Assert.assertTrue(pool.getAvailableConnections() >= config.getMinPoolSize());
    }
    
    @Test
    public void testConnectionValidation() throws SQLException {
        Connection conn = pool.getConnection();
        Assert.assertTrue(conn.isValid(1));
        pool.releaseConnection(conn);
    }
    
    @Test
    public void testShutdown() throws SQLException {
        Connection conn = pool.getConnection();
        pool.shutdown();
        
        Assert.assertEquals(pool.getTotalConnections(), 0);
        
        // Should throw exception after shutdown
        try {
            pool.getConnection();
            Assert.fail("Should throw SQLException after shutdown");
        } catch (SQLException e) {
            Assert.assertTrue(e.getMessage().contains("shutdown"));
        }
    }
}

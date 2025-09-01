package com.qa.db;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionPoolPerformanceTest {
    private DatabaseConnectionPool pool;
    private DatabaseConfig config;
    
    @BeforeMethod
    public void setUp() {
        config = new DatabaseConfig(
            "jdbc:h2:mem:perftest;DB_CLOSE_DELAY=-1",
            "sa",
            "",
            "org.h2.Driver"
        );
        config.setMinPoolSize(5);
        config.setMaxPoolSize(20);
        config.setConnectionTimeout(10000);
        
        pool = new DatabaseConnectionPool(config);
    }
    
    @AfterMethod
    public void tearDown() {
        if (pool != null) {
            pool.shutdown();
        }
    }
    
    @Test
    public void testHighConcurrency() throws InterruptedException {
        int threadCount = 50;
        int operationsPerThread = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        Connection conn = pool.getConnection();
                        Thread.sleep(10); // Simulate database work
                        pool.releaseConnection(conn);
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("Performance Test Results:");
        System.out.println("Total operations: " + (threadCount * operationsPerThread));
        System.out.println("Successful operations: " + successCount.get());
        System.out.println("Failed operations: " + errorCount.get());
        System.out.println("Duration: " + duration + "ms");
        System.out.println("Operations per second: " + (successCount.get() * 1000.0 / duration));
        
        Assert.assertTrue(successCount.get() > 0);
        Assert.assertEquals(pool.getUsedConnections(), 0);
    }
    
    @Test
    public void testConnectionReuse() throws SQLException, InterruptedException {
        // Get and release connections multiple times
        for (int i = 0; i < 100; i++) {
            Connection conn = pool.getConnection();
            Assert.assertNotNull(conn);
            pool.releaseConnection(conn);
        }
        
        // Pool should not grow beyond reasonable limits
        Assert.assertTrue(pool.getTotalConnections() <= config.getMaxPoolSize());
        Assert.assertEquals(pool.getUsedConnections(), 0);
    }
    
    @Test
    public void testPoolGrowthAndShrinkage() throws SQLException, InterruptedException {
        // Simulate load spike
        Connection[] connections = new Connection[15];
        for (int i = 0; i < 15; i++) {
            connections[i] = pool.getConnection();
        }
        
        int peakConnections = pool.getTotalConnections();
        Assert.assertTrue(peakConnections >= 15);
        
        // Release all connections
        for (Connection conn : connections) {
            pool.releaseConnection(conn);
        }
        
        // Wait for maintenance to potentially clean up idle connections
        Thread.sleep(2000);
        
        Assert.assertEquals(pool.getUsedConnections(), 0);
        System.out.println("Peak connections: " + peakConnections);
        System.out.println("Final connections: " + pool.getTotalConnections());
    }
}

package com.qa.db;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPoolManagerTest {
    private ConnectionPoolManager manager;
    private DatabaseConfig config;
    
    @BeforeMethod
    public void setUp() {
        manager = ConnectionPoolManager.getInstance();
        config = new DatabaseConfig(
            "jdbc:h2:mem:testdb2;DB_CLOSE_DELAY=-1",
            "sa",
            "",
            "org.h2.Driver"
        );
    }
    
    @AfterMethod
    public void tearDown() {
        manager.shutdownAllPools();
    }
    
    @Test
    public void testCreatePool() {
        manager.createPool("testPool", config);
        Assert.assertTrue(manager.poolExists("testPool"));
        Assert.assertNotNull(manager.getPool("testPool"));
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreateDuplicatePool() {
        manager.createPool("testPool", config);
        manager.createPool("testPool", config); // Should throw exception
    }
    
    @Test
    public void testGetConnection() throws SQLException {
        manager.createPool("testPool", config);
        Connection conn = manager.getConnection("testPool");
        Assert.assertNotNull(conn);
        Assert.assertFalse(conn.isClosed());
        
        manager.releaseConnection("testPool", conn);
    }
    
    @Test(expectedExceptions = SQLException.class)
    public void testGetConnectionFromNonExistentPool() throws SQLException {
        manager.getConnection("nonExistentPool");
    }
    
    @Test
    public void testReleaseConnection() throws SQLException {
        manager.createPool("testPool", config);
        Connection conn = manager.getConnection("testPool");
        
        DatabaseConnectionPool pool = manager.getPool("testPool");
        int initialAvailable = pool.getAvailableConnections();
        
        manager.releaseConnection("testPool", conn);
        
        Assert.assertEquals(pool.getAvailableConnections(), initialAvailable + 1);
    }
    
    @Test
    public void testMultiplePools() throws SQLException {
        DatabaseConfig config2 = new DatabaseConfig(
            "jdbc:h2:mem:testdb3;DB_CLOSE_DELAY=-1",
            "sa",
            "",
            "org.h2.Driver"
        );
        
        manager.createPool("pool1", config);
        manager.createPool("pool2", config2);
        
        Assert.assertTrue(manager.poolExists("pool1"));
        Assert.assertTrue(manager.poolExists("pool2"));
        
        Connection conn1 = manager.getConnection("pool1");
        Connection conn2 = manager.getConnection("pool2");
        
        Assert.assertNotNull(conn1);
        Assert.assertNotNull(conn2);
        
        manager.releaseConnection("pool1", conn1);
        manager.releaseConnection("pool2", conn2);
    }
    
    @Test
    public void testShutdownPool() throws SQLException {
        manager.createPool("testPool", config);
        Connection conn = manager.getConnection("testPool");
        
        manager.shutdownPool("testPool");
        
        Assert.assertFalse(manager.poolExists("testPool"));
        Assert.assertNull(manager.getPool("testPool"));
    }
    
    @Test
    public void testShutdownAllPools() throws SQLException {
        manager.createPool("pool1", config);
        manager.createPool("pool2", config);
        
        manager.shutdownAllPools();
        
        Assert.assertFalse(manager.poolExists("pool1"));
        Assert.assertFalse(manager.poolExists("pool2"));
    }
    
    @Test
    public void testSingletonBehavior() {
        ConnectionPoolManager manager1 = ConnectionPoolManager.getInstance();
        ConnectionPoolManager manager2 = ConnectionPoolManager.getInstance();
        
        Assert.assertSame(manager1, manager2);
    }
}

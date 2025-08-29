package com.qa.integration.database;

import org.apache.commons.dbcp2.BasicDataSource;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Database Manager for integration testing
 * Exercise: Using JDBC in Java to connect to a database
 * Exercise: Querying the DB in test scripts to validate UI actions
 */
public class DatabaseManager {
    
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    private static DatabaseManager instance;
    private BasicDataSource dataSource;
    
    // Database configuration
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;
    
    private DatabaseManager() {
        initializeDataSource();
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Initialize database connection pool
     */
    private void initializeDataSource() {
        // Load configuration from properties or use defaults
        loadDatabaseConfig();
        
        dataSource = new BasicDataSource();
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        
        // Connection pool settings
        dataSource.setInitialSize(5);
        dataSource.setMaxTotal(20);
        dataSource.setMaxIdle(10);
        dataSource.setMinIdle(5);
        dataSource.setMaxWaitMillis(30000);
        
        // Validation settings
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestOnBorrow(true);
        dataSource.setTestWhileIdle(true);
        
        logger.info("Database connection pool initialized");
    }
    
    /**
     * Load database configuration
     */
    private void loadDatabaseConfig() {
        // Default configuration for H2 in-memory database (for testing)
        jdbcUrl = System.getProperty("db.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        username = System.getProperty("db.username", "sa");
        password = System.getProperty("db.password", "");
        driverClassName = System.getProperty("db.driver", "org.h2.Driver");
        
        // For MySQL (uncomment and configure as needed)
        // jdbcUrl = "jdbc:mysql://localhost:3306/qa_test_db";
        // username = "qa_user";
        // password = "qa_password";
        // driverClassName = "com.mysql.cj.jdbc.Driver";
    }
    
    /**
     * Get database connection
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    /**
     * Execute SELECT query and return results as List of Maps
     */
    public List<Map<String, Object>> executeQuery(String sql, Object... parameters) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Set parameters
            for (int i = 0; i < parameters.length; i++) {
                stmt.setObject(i + 1, parameters[i]);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    results.add(row);
                }
            }
            
            logger.info("Query executed successfully. Returned " + results.size() + " rows");
            
        } catch (SQLException e) {
            logger.severe("Error executing query: " + sql + " - " + e.getMessage());
            throw new RuntimeException("Database query failed", e);
        }
        
        return results;
    }
    
    /**
     * Execute INSERT, UPDATE, DELETE queries
     */
    public int executeUpdate(String sql, Object... parameters) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Set parameters
            for (int i = 0; i < parameters.length; i++) {
                stmt.setObject(i + 1, parameters[i]);
            }
            
            int rowsAffected = stmt.executeUpdate();
            logger.info("Update executed successfully. Rows affected: " + rowsAffected);
            return rowsAffected;
            
        } catch (SQLException e) {
            logger.severe("Error executing update: " + sql + " - " + e.getMessage());
            throw new RuntimeException("Database update failed", e);
        }
    }
    
    /**
     * Execute INSERT and return generated key
     */
    public long executeInsertAndGetKey(String sql, Object... parameters) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Set parameters
            for (int i = 0; i < parameters.length; i++) {
                stmt.setObject(i + 1, parameters[i]);
            }
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long generatedKey = generatedKeys.getLong(1);
                        logger.info("Insert executed successfully. Generated key: " + generatedKey);
                        return generatedKey;
                    }
                }
            }
            
            throw new RuntimeException("No generated key returned");
            
        } catch (SQLException e) {
            logger.severe("Error executing insert: " + sql + " - " + e.getMessage());
            throw new RuntimeException("Database insert failed", e);
        }
    }
    
    /**
     * Execute batch operations
     */
    public int[] executeBatch(String sql, List<Object[]> parametersList) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (Object[] parameters : parametersList) {
                for (int i = 0; i < parameters.length; i++) {
                    stmt.setObject(i + 1, parameters[i]);
                }
                stmt.addBatch();
            }
            
            int[] results = stmt.executeBatch();
            logger.info("Batch executed successfully. Operations: " + results.length);
            return results;
            
        } catch (SQLException e) {
            logger.severe("Error executing batch: " + sql + " - " + e.getMessage());
            throw new RuntimeException("Database batch operation failed", e);
        }
    }
    
    /**
     * Get single value from query result
     */
    public Object getSingleValue(String sql, Object... parameters) {
        List<Map<String, Object>> results = executeQuery(sql, parameters);
        if (results.isEmpty()) {
            return null;
        }
        
        Map<String, Object> firstRow = results.get(0);
        return firstRow.values().iterator().next();
    }
    
    /**
     * Check if record exists
     */
    public boolean recordExists(String tableName, String whereClause, Object... parameters) {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + whereClause;
        Object count = getSingleValue(sql, parameters);
        return count != null && ((Number) count).intValue() > 0;
    }
    
    /**
     * Get record count
     */
    public int getRecordCount(String tableName, String whereClause, Object... parameters) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            sql += " WHERE " + whereClause;
        }
        
        Object count = getSingleValue(sql, parameters);
        return count != null ? ((Number) count).intValue() : 0;
    }
    
    /**
     * Validate data integrity
     */
    public boolean validateDataIntegrity(String sql, Object expectedValue, Object... parameters) {
        Object actualValue = getSingleValue(sql, parameters);
        return Objects.equals(expectedValue, actualValue);
    }
    
    /**
     * Clean up test data
     */
    public void cleanupTestData(String tableName, String whereClause, Object... parameters) {
        String sql = "DELETE FROM " + tableName + " WHERE " + whereClause;
        executeUpdate(sql, parameters);
        logger.info("Test data cleaned up from table: " + tableName);
    }
    
    /**
     * Execute SQL script from file
     */
    public void executeScript(String scriptContent) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Split script into individual statements
            String[] statements = scriptContent.split(";");
            
            for (String statement : statements) {
                String trimmedStatement = statement.trim();
                if (!trimmedStatement.isEmpty() && !trimmedStatement.startsWith("--")) {
                    stmt.execute(trimmedStatement);
                }
            }
            
            logger.info("SQL script executed successfully");
            
        } catch (SQLException e) {
            logger.severe("Error executing script: " + e.getMessage());
            throw new RuntimeException("Script execution failed", e);
        }
    }
    
    /**
     * Begin transaction
     */
    public Connection beginTransaction() throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
        return conn;
    }
    
    /**
     * Commit transaction
     */
    public void commitTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.commit();
            conn.setAutoCommit(true);
            conn.close();
        }
    }
    
    /**
     * Rollback transaction
     */
    public void rollbackTransaction(Connection conn) {
        try {
            if (conn != null) {
                conn.rollback();
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException e) {
            logger.severe("Error rolling back transaction: " + e.getMessage());
        }
    }
    
    /**
     * Close data source
     */
    public void closeDataSource() {
        try {
            if (dataSource != null) {
                dataSource.close();
                logger.info("Database connection pool closed");
            }
        } catch (SQLException e) {
            logger.severe("Error closing data source: " + e.getMessage());
        }
    }
    
    /**
     * Get database metadata information
     */
    public Map<String, Object> getDatabaseInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try (Connection conn = getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            info.put("databaseProductName", metaData.getDatabaseProductName());
            info.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            info.put("driverName", metaData.getDriverName());
            info.put("driverVersion", metaData.getDriverVersion());
            info.put("url", metaData.getURL());
            info.put("userName", metaData.getUserName());
            
        } catch (SQLException e) {
            logger.severe("Error getting database info: " + e.getMessage());
        }
        
        return info;
    }
}

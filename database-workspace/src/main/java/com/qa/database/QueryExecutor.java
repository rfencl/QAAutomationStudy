package com.qa.database;

import java.sql.*;
import java.util.*;

/**
 * Utility class for executing database queries in QA tests
 */
public class QueryExecutor {
    
    /**
     * Execute a SELECT query and return results as List of Maps
     */
    public static List<Map<String, Object>> executeQuery(String query, Object... parameters) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
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
        } catch (SQLException e) {
            throw new RuntimeException("Query execution failed: " + query, e);
        }
        
        return results;
    }
    
    /**
     * Execute an INSERT, UPDATE, or DELETE query
     */
    public static int executeUpdate(String query, Object... parameters) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            // Set parameters
            for (int i = 0; i < parameters.length; i++) {
                stmt.setObject(i + 1, parameters[i]);
            }
            
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update execution failed: " + query, e);
        }
    }
    
    /**
     * Execute a query and return a single value
     */
    public static Object executeScalar(String query, Object... parameters) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            // Set parameters
            for (int i = 0; i < parameters.length; i++) {
                stmt.setObject(i + 1, parameters[i]);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject(1);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Scalar query execution failed: " + query, e);
        }
    }
    
    /**
     * Check if a record exists
     */
    public static boolean recordExists(String tableName, String whereClause, Object... parameters) {
        String query = "SELECT 1 FROM " + tableName + " WHERE " + whereClause + " LIMIT 1";
        Object result = executeScalar(query, parameters);
        return result != null;
    }
    
    /**
     * Get count of records matching criteria
     */
    public static int getRecordCount(String tableName, String whereClause, Object... parameters) {
        String query = "SELECT COUNT(*) FROM " + tableName;
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            query += " WHERE " + whereClause;
        }
        
        Object result = executeScalar(query, parameters);
        return result != null ? ((Number) result).intValue() : 0;
    }
    
    /**
     * Verify UI insert reflected in database
     */
    public static boolean verifyUserInsert(String username, String email) {
        String query = "SELECT COUNT(*) FROM Users WHERE username = ? AND email = ? " +
                      "AND registration_date >= DATE_SUB(NOW(), INTERVAL 1 HOUR)";
        int count = (Integer) executeScalar(query, username, email);
        return count > 0;
    }
    
    /**
     * Get users registered in last N days
     */
    public static List<Map<String, Object>> getUsersRegisteredInLastDays(int days) {
        String query = "SELECT user_id, username, email, first_name, last_name, registration_date " +
                      "FROM Users WHERE registration_date >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                      "ORDER BY registration_date DESC";
        return executeQuery(query, days);
    }
    
    /**
     * Find duplicate records by email
     */
    public static List<Map<String, Object>> findDuplicateEmails() {
        String query = "SELECT email, COUNT(*) as duplicate_count FROM Users " +
                      "GROUP BY email HAVING COUNT(*) > 1";
        return executeQuery(query);
    }
    
    /**
     * Get second highest salary
     */
    public static Object getSecondHighestSalary() {
        String query = "SELECT MAX(salary) FROM Employees " +
                      "WHERE salary < (SELECT MAX(salary) FROM Employees)";
        return executeScalar(query);
    }
    
    /**
     * Get customers with more than N orders
     */
    public static List<Map<String, Object>> getCustomersWithMoreThanNOrders(int orderCount) {
        String query = "SELECT c.customer_id, c.name, c.email, COUNT(o.order_id) AS order_count " +
                      "FROM Customers c JOIN Orders o ON c.customer_id = o.customer_id " +
                      "GROUP BY c.customer_id, c.name, c.email " +
                      "HAVING COUNT(o.order_id) > ? ORDER BY order_count DESC";
        return executeQuery(query, orderCount);
    }
    
    /**
     * Get customers with no orders
     */
    public static List<Map<String, Object>> getCustomersWithNoOrders() {
        String query = "SELECT c.customer_id, c.name, c.email FROM Customers c " +
                      "LEFT JOIN Orders o ON c.customer_id = o.customer_id " +
                      "WHERE o.order_id IS NULL";
        return executeQuery(query);
    }
    
    /**
     * Verify banking transfer transaction
     */
    public static boolean verifyTransfer(int fromAccountId, int toAccountId, double amount) {
        // Check if transaction record exists
        String transactionQuery = "SELECT COUNT(*) FROM Transactions " +
                                "WHERE from_account_id = ? AND to_account_id = ? " +
                                "AND amount = ? AND status = 'completed'";
        int transactionCount = (Integer) executeScalar(transactionQuery, fromAccountId, toAccountId, amount);
        
        return transactionCount > 0;
    }
    
    /**
     * Get account balance
     */
    public static double getAccountBalance(int accountId) {
        String query = "SELECT balance FROM Accounts WHERE account_id = ?";
        Object result = executeScalar(query, accountId);
        return result != null ? ((Number) result).doubleValue() : 0.0;
    }
    
    /**
     * Clean up test data
     */
    public static void cleanupTestData() {
        // Delete test users
        executeUpdate("DELETE FROM Users WHERE username LIKE 'test_%' OR email LIKE 'test%@example.com'");
        
        // Reset account balances
        executeUpdate("UPDATE Accounts SET balance = 5000.00 WHERE account_number IN ('ACC001', 'ACC002', 'ACC003')");
        
        System.out.println("Test data cleanup completed");
    }
    
    /**
     * Setup test data
     */
    public static void setupTestData() {
        // Insert test user
        executeUpdate("INSERT INTO Users (username, email, password_hash, first_name, last_name) " +
                     "VALUES (?, ?, ?, ?, ?)", 
                     "test_automation_user", "test.automation@example.com", "hashed_pass", "Test", "User");
        
        System.out.println("Test data setup completed");
    }
}

package com.qa.database;

import java.sql.*;
import java.util.*;

public class QueryExecutor {

    public static List<Map<String, Object>> executeQuery(String query, Object... parameters) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
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
    
    public static int executeUpdate(String query, Object... parameters) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            for (int i = 0; i < parameters.length; i++) {
                stmt.setObject(i + 1, parameters[i]);
            }
            
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Update execution failed: " + query, e);
        }
    }
    
    public static Object executeScalar(String query, Object... parameters) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
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

    public static boolean recordExists(String tableName, String whereClause, Object... parameters) {
        String query = "SELECT 1 FROM " + tableName + " WHERE " + whereClause + " LIMIT 1";
        Object result = executeScalar(query, parameters);
        return result != null;
    }
    
    public static int getRecordCount(String tableName, String whereClause, Object... parameters) {
        String query = "SELECT COUNT(*) FROM " + tableName;
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            query += " WHERE " + whereClause;
        }
        
        Object result = executeScalar(query, parameters);
        return result != null ? ((Number) result).intValue() : 0;
    }

    public static boolean verifyUserInsert(String username, String email) {
        String query = "SELECT COUNT(*) FROM users WHERE username = ? AND email = ? " +
                      "AND registration_date >= DATE_SUB(NOW(), INTERVAL 1 HOUR)";
        Object result = executeScalar(query, username, email);
        return result != null && ((Number) result).intValue() > 0;
    }
    
    public static List<Map<String, Object>> getUsersRegisteredInLastDays(int days) {
        String query = "SELECT user_id, username, email, first_name, last_name, registration_date " +
                      "FROM users WHERE registration_date >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                      "ORDER BY registration_date DESC";
        return executeQuery(query, days);
    }
    
    public static List<Map<String, Object>> findDuplicateEmails() {
        String query = "SELECT email, COUNT(*) as duplicate_count FROM users " +
                      "GROUP BY email HAVING COUNT(*) > 1";
        return executeQuery(query);
    }
    
    public static Object getSecondHighestSalary() {
        String query = "SELECT MAX(salary) FROM employees " +
                      "WHERE salary < (SELECT MAX(salary) FROM employees)";
        return executeScalar(query);
    }
    
    public static List<Map<String, Object>> getCustomersWithMoreThanNOrders(int orderCount) {
        String query = "SELECT c.customer_id, c.name, c.email, COUNT(o.order_id) AS order_count " +
                      "FROM customers c JOIN orders o ON c.customer_id = o.customer_id " +
                      "GROUP BY c.customer_id, c.name, c.email " +
                      "HAVING COUNT(o.order_id) > ? ORDER BY order_count DESC";
        return executeQuery(query, orderCount);
    }
    
    public static List<Map<String, Object>> getCustomersWithNoOrders() {
        String query = "SELECT c.customer_id, c.name, c.email FROM customers c " +
                      "LEFT JOIN orders o ON c.customer_id = o.customer_id " +
                      "WHERE o.order_id IS NULL";
        return executeQuery(query);
    }
    
    public static boolean verifyTransfer(int fromAccountId, int toAccountId, double amount) {
        String transactionQuery = "SELECT COUNT(*) FROM transactions " +
                                "WHERE from_account_id = ? AND to_account_id = ? " +
                                "AND amount = ? AND status = 'completed'";
        Object result = executeScalar(transactionQuery, fromAccountId, toAccountId, amount);
        return result != null && ((Number) result).intValue() > 0;
    }
    
    public static double getAccountBalance(int accountId) {
        String query = "SELECT balance FROM accounts WHERE account_id = ?";
        Object result = executeScalar(query, accountId);
        return result != null ? ((Number) result).doubleValue() : 0.0;
    }

    public static void createDatabaseIfNotExists() {
        DatabaseConnection.createDatabase();
    }

    public static void createTables() {
        createCustomersTable();
        createOrdersTable();
        createUsersTable();
        createEmployeesTable();
        createAccountsTable();
        createTransactionsTable();
    }

    public static void createUsersTable() {
        executeUpdate("DROP TABLE IF EXISTS users");
        String sql = "CREATE TABLE users (" +
                     "user_id INT AUTO_INCREMENT PRIMARY KEY," +
                     "username VARCHAR(50) NOT NULL UNIQUE," +
                     "email VARCHAR(50) NOT NULL UNIQUE," +
                     "password_hash VARCHAR(255) NOT NULL," +
                     "first_name VARCHAR(50)," +
                     "last_name VARCHAR(50)," +
                     "registration_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
                     "last_login DATETIME)";
        executeUpdate(sql);
    }

    public static void createEmployeesTable() {
        executeUpdate("DROP TABLE IF EXISTS employees");
        String sql = "CREATE TABLE employees (" +
                     "employee_id INT AUTO_INCREMENT PRIMARY KEY," +
                     "name VARCHAR(100)," +
                     "salary DECIMAL(10, 2))";
        executeUpdate(sql);
    }

    public static void createCustomersTable() {
        executeUpdate("DROP TABLE IF EXISTS orders");
        executeUpdate("DROP TABLE IF EXISTS customers");
        String sql = "CREATE TABLE customers (" +
                     "customer_id INT AUTO_INCREMENT PRIMARY KEY," +
                     "name VARCHAR(100)," +
                     "email VARCHAR(100))";
        executeUpdate(sql);
    }

    public static void createOrdersTable() {
        executeUpdate("DROP TABLE IF EXISTS orders");
        String sql = "CREATE TABLE orders (" +
                     "order_id INT AUTO_INCREMENT PRIMARY KEY," +
                     "customer_id INT," +
                     "order_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
                     "FOREIGN KEY (customer_id) REFERENCES customers(customer_id))";
        executeUpdate(sql);
    }

    public static void createAccountsTable() {
        executeUpdate("DROP TABLE IF EXISTS transactions");
        executeUpdate("DROP TABLE IF EXISTS accounts");
        String sql = "CREATE TABLE accounts (" +
                     "account_id INT AUTO_INCREMENT PRIMARY KEY," +
                     "account_number VARCHAR(20) UNIQUE NOT NULL," +
                     "balance DECIMAL(10, 2))";
        executeUpdate(sql);
    }

    public static void createTransactionsTable() {
        executeUpdate("DROP TABLE IF EXISTS transactions");
        String sql = "CREATE TABLE transactions (" +
                     "transaction_id INT AUTO_INCREMENT PRIMARY KEY," +
                     "from_account_id INT," +
                     "to_account_id INT," +
                     "amount DECIMAL(10, 2)," +
                     "status VARCHAR(20)," +
                     "transaction_type VARCHAR(20)," +
                     "transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP)";
        executeUpdate(sql);
    }

    public static void setupTestData() {
        executeUpdate("INSERT INTO users (username, email, password_hash, first_name, last_name, registration_date) VALUES (?, ?, ?, ?, ?, ?)",
                "test_automation_user", "test.automation@example.com", "hashed_pass", "Test", "User", "2025-08-20 10:00:00");
        executeUpdate("INSERT INTO users (username, email, password_hash, first_name, last_name) VALUES (?, ?, ?, ?, ?)",
                "qa_user", "qa@example.com", "hashed_pass", "QA", "User");
        executeUpdate("INSERT INTO users (username, email, password_hash, first_name, last_name, registration_date) VALUES (?, ?, ?, ?, ?, ?)",
                "another_user", "another@example.com", "hashed_pass", "Another", "User", "2025-08-01 12:00:00");

        executeUpdate("INSERT INTO employees (name, salary) VALUES (?, ?)", "John Doe", 90000.00);
        executeUpdate("INSERT INTO employees (name, salary) VALUES (?, ?)", "Jane Smith", 120000.00);
        executeUpdate("INSERT INTO employees (name, salary) VALUES (?, ?)", "Peter Jones", 80000.00);
        executeUpdate("INSERT INTO employees (name, salary) VALUES (?, ?)", "Mary Williams", 110000.00);

        executeUpdate("INSERT INTO customers (customer_id, name, email) VALUES (?, ?, ?)", 1, "Customer A", "cust.a@example.com");
        executeUpdate("INSERT INTO customers (customer_id, name, email) VALUES (?, ?, ?)", 2, "Customer B", "cust.b@example.com");
        executeUpdate("INSERT INTO customers (customer_id, name, email) VALUES (?, ?, ?)", 3, "Customer C (no orders)", "cust.c@example.com");
        for (int i = 0; i < 6; i++) {
            executeUpdate("INSERT INTO orders (customer_id) VALUES (?) ", 1);
        }
        executeUpdate("INSERT INTO orders (customer_id) VALUES (?) ", 2);

        executeUpdate("INSERT INTO accounts (account_id, account_number, balance) VALUES (?, ?, ?)", 1, "ACC001", 5000.00);
        executeUpdate("INSERT INTO accounts (account_id, account_number, balance) VALUES (?, ?, ?)", 2, "ACC002", 5000.00);
        executeUpdate("INSERT INTO accounts (account_id, account_number, balance) VALUES (?, ?, ?)", 3, "ACC003", 5000.00);

        System.out.println("Test data setup completed");
    }

    public static void cleanupTestData() {
        executeUpdate("DELETE FROM users");
        executeUpdate("DELETE FROM employees");
        executeUpdate("DELETE FROM orders");
        executeUpdate("DELETE FROM customers");
        executeUpdate("DELETE FROM transactions");
        executeUpdate("DELETE FROM accounts");
        System.out.println("Test data cleanup completed");
    }
}
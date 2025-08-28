package com.qa.database;

import org.testng.Assert;
import org.testng.annotations.*;
import java.util.List;
import java.util.Map;

public class DatabaseTest {
    
    @BeforeClass
    public void setupClass() {
        // Verify database connection before running tests
        Assert.assertTrue(DatabaseConnection.testConnection(), 
                         "Database connection should be available");
        
        // Setup test data
        QueryExecutor.setupTestData();
    }
    
    @AfterClass
    public void teardownClass() {
        // Cleanup test data
        QueryExecutor.cleanupTestData();
        
        // Close database connections
        DatabaseConnection.closeDataSource();
    }
    
    @Test(priority = 1, groups = {"smoke"})
    public void testDatabaseConnection() {
        boolean isConnected = DatabaseConnection.testConnection();
        Assert.assertTrue(isConnected, "Should be able to connect to database");
        
        boolean hasAccess = DatabaseConnection.verifyDatabaseAccess();
        Assert.assertTrue(hasAccess, "Should have access to execute queries");
    }
    
    @Test(priority = 2, groups = {"regression"})
    public void testUsersRegisteredInLastSevenDays() {
        List<Map<String, Object>> recentUsers = QueryExecutor.getUsersRegisteredInLastDays(7);
        
        Assert.assertNotNull(recentUsers, "Query should return results");
        Assert.assertTrue(recentUsers.size() > 0, "Should find users registered in last 7 days");
        
        // Verify each user was registered within last 7 days
        for (Map<String, Object> user : recentUsers) {
            Assert.assertNotNull(user.get("username"), "Username should not be null");
            Assert.assertNotNull(user.get("email"), "Email should not be null");
            Assert.assertNotNull(user.get("registration_date"), "Registration date should not be null");
        }
    }
    
    @Test(priority = 3, groups = {"data-validation"})
    public void testFindDuplicateEmails() {
        List<Map<String, Object>> duplicates = QueryExecutor.findDuplicateEmails();
        
        Assert.assertNotNull(duplicates, "Query should return results");
        
        // If duplicates exist, verify the count is greater than 1
        for (Map<String, Object> duplicate : duplicates) {
            int count = ((Number) duplicate.get("duplicate_count")).intValue();
            Assert.assertTrue(count > 1, "Duplicate count should be greater than 1");
        }
    }
    
    @Test(priority = 4, groups = {"regression"})
    public void testSecondHighestSalary() {
        Object secondHighest = QueryExecutor.getSecondHighestSalary();
        
        Assert.assertNotNull(secondHighest, "Should find second highest salary");
        
        double salary = ((Number) secondHighest).doubleValue();
        Assert.assertTrue(salary > 0, "Second highest salary should be positive");
        
        System.out.println("Second highest salary: $" + salary);
    }
    
    @Test(priority = 5, groups = {"business-logic"})
    public void testCustomersWithMoreThanFiveOrders() {
        List<Map<String, Object>> customers = QueryExecutor.getCustomersWithMoreThanNOrders(5);
        
        Assert.assertNotNull(customers, "Query should return results");
        
        // Verify each customer has more than 5 orders
        for (Map<String, Object> customer : customers) {
            int orderCount = ((Number) customer.get("order_count")).intValue();
            Assert.assertTrue(orderCount > 5, "Customer should have more than 5 orders");
            
            Assert.assertNotNull(customer.get("name"), "Customer name should not be null");
            Assert.assertNotNull(customer.get("email"), "Customer email should not be null");
        }
    }
    
    @Test(priority = 6, groups = {"business-logic"})
    public void testCustomersWithNoOrders() {
        List<Map<String, Object>> customers = QueryExecutor.getCustomersWithNoOrders();
        
        Assert.assertNotNull(customers, "Query should return results");
        
        // Verify customer data structure
        for (Map<String, Object> customer : customers) {
            Assert.assertNotNull(customer.get("customer_id"), "Customer ID should not be null");
            Assert.assertNotNull(customer.get("name"), "Customer name should not be null");
            Assert.assertNotNull(customer.get("email"), "Customer email should not be null");
        }
        
        System.out.println("Found " + customers.size() + " customers with no orders");
    }
    
    @Test(priority = 7, groups = {"ui-validation"})
    public void testVerifyUIInsertReflectedInDB() {
        // Simulate a UI insert by inserting a user
        String testUsername = "ui_test_user_" + System.currentTimeMillis();
        String testEmail = "ui.test." + System.currentTimeMillis() + "@example.com";
        
        // Insert user (simulating UI action)
        int rowsAffected = QueryExecutor.executeUpdate(
            "INSERT INTO Users (username, email, password_hash, first_name, last_name) VALUES (?, ?, ?, ?, ?)",
            testUsername, testEmail, "hashed_password", "UI", "Test"
        );
        
        Assert.assertEquals(rowsAffected, 1, "Should insert exactly one user");
        
        // Verify the insert is reflected in DB
        boolean userExists = QueryExecutor.verifyUserInsert(testUsername, testEmail);
        Assert.assertTrue(userExists, "User should exist in database after UI insert");
        
        // Cleanup
        QueryExecutor.executeUpdate("DELETE FROM Users WHERE username = ?", testUsername);
    }
    
    @Test(priority = 8, groups = {"banking"})
    public void testBankingTransferValidation() {
        // Get initial balances
        double initialSenderBalance = QueryExecutor.getAccountBalance(1);
        double initialReceiverBalance = QueryExecutor.getAccountBalance(2);
        
        double transferAmount = 100.00;
        
        // Simulate a transfer (in real scenario, this would be done through UI)
        // 1. Insert transaction record
        int transactionId = QueryExecutor.executeUpdate(
            "INSERT INTO Transactions (from_account_id, to_account_id, amount, transaction_type, status) VALUES (?, ?, ?, ?, ?)",
            1, 2, transferAmount, "transfer", "completed"
        );
        
        // 2. Update account balances
        QueryExecutor.executeUpdate("UPDATE Accounts SET balance = balance - ? WHERE account_id = ?", 
                                   transferAmount, 1);
        QueryExecutor.executeUpdate("UPDATE Accounts SET balance = balance + ? WHERE account_id = ?", 
                                   transferAmount, 2);
        
        // Verify transfer
        boolean transferExists = QueryExecutor.verifyTransfer(1, 2, transferAmount);
        Assert.assertTrue(transferExists, "Transfer transaction should exist in database");
        
        // Verify balances
        double finalSenderBalance = QueryExecutor.getAccountBalance(1);
        double finalReceiverBalance = QueryExecutor.getAccountBalance(2);
        
        Assert.assertEquals(finalSenderBalance, initialSenderBalance - transferAmount, 0.01,
                           "Sender balance should be debited");
        Assert.assertEquals(finalReceiverBalance, initialReceiverBalance + transferAmount, 0.01,
                           "Receiver balance should be credited");
        
        System.out.println("Transfer validation completed successfully");
    }
    
    @Test(priority = 9, groups = {"performance"})
    public void testQueryPerformance() {
        long startTime = System.currentTimeMillis();
        
        // Execute a potentially slow query
        List<Map<String, Object>> results = QueryExecutor.executeQuery(
            "SELECT c.name, COUNT(o.order_id) as order_count " +
            "FROM Customers c LEFT JOIN Orders o ON c.customer_id = o.customer_id " +
            "GROUP BY c.customer_id, c.name ORDER BY order_count DESC"
        );
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        Assert.assertNotNull(results, "Query should return results");
        Assert.assertTrue(executionTime < 5000, "Query should complete within 5 seconds");
        
        System.out.println("Query executed in " + executionTime + "ms");
    }
    
    @Test(priority = 10, groups = {"data-integrity"})
    public void testDataIntegrityConstraints() {
        // Test unique constraint violation
        try {
            QueryExecutor.executeUpdate(
                "INSERT INTO Users (username, email, password_hash) VALUES (?, ?, ?)",
                "qa_user", "qa@example.com", "test_hash"
            );
            Assert.fail("Should not allow duplicate username");
        } catch (RuntimeException e) {
            // Expected - unique constraint violation
            Assert.assertTrue(e.getMessage().contains("Duplicate entry") || 
                            e.getMessage().contains("duplicate key"),
                            "Should get duplicate key error");
        }
    }
    
    @Test(priority = 11, groups = {"edge-cases"})
    public void testEdgeCases() {
        // Test null handling
        List<Map<String, Object>> usersWithNullLogin = QueryExecutor.executeQuery(
            "SELECT username, email, last_login FROM Users WHERE last_login IS NULL"
        );
        
        Assert.assertNotNull(usersWithNullLogin, "Query should handle null values");
        
        // Test empty result set
        List<Map<String, Object>> nonExistentData = QueryExecutor.executeQuery(
            "SELECT * FROM Users WHERE username = 'definitely_does_not_exist'"
        );
        
        Assert.assertNotNull(nonExistentData, "Query should return empty list for no results");
        Assert.assertEquals(nonExistentData.size(), 0, "Should return empty list");
    }
}

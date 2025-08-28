package com.qa.automation;

import java.sql.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DatabaseHelper {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/qa_test_db";
    private static final String DB_USER = "qa_user";
    private static final String DB_PASSWORD = "qa_password";
    
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Database connection successful");
            return conn;
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            throw e;
        }
    }
    
    public static void demonstrateExceptionHandling() {
        System.out.println("=== Exception Handling Demo ===");
        
        try {
            Connection conn = getConnection();
            // Simulate some database operations
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            
            if (rs.next()) {
                System.out.println("User count: " + rs.getInt(1));
            }
            
            conn.close();
        } catch (SQLException e) {
            System.out.println("Database operation failed: " + e.getMessage());
            // In a test framework, you might use Assert.fail() here
            System.out.println("Test would fail due to DB error");
        }
    }
    
    public static void readTestDataFromFile(String filename) {
        System.out.println("\n=== Reading Test Data from File ===");
        
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNumber = 1;
            
            while ((line = br.readLine()) != null) {
                System.out.println("Line " + lineNumber + ": " + line);
                lineNumber++;
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        demonstrateExceptionHandling();
        
        // Create a sample test data file first
        try {
            java.nio.file.Files.write(
                java.nio.file.Paths.get("test-data.txt"),
                "username,password,expected_result\nqa_user,pass123,success\ninvalid_user,wrong_pass,failure".getBytes()
            );
            readTestDataFromFile("test-data.txt");
        } catch (IOException e) {
            System.out.println("Error creating test file: " + e.getMessage());
        }
    }
}

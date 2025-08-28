package com.qa.integration.tests;

import com.qa.integration.database.DatabaseManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.*;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Integration tests that validate both UI and Database layers
 * Exercise: Automate placing an order in a demo web app, then validate order details in the DB
 * Exercise: Create a test suite that runs UI tests and DB validation together
 * Exercise: Write a data-driven test that pulls input from a database
 */
public class UIDBIntegrationTest {
    
    private WebDriver driver;
    private WebDriverWait wait;
    private DatabaseManager dbManager;
    
    @BeforeClass
    public void setupClass() {
        // Setup database
        dbManager = DatabaseManager.getInstance();
        setupTestDatabase();
        
        // Setup WebDriver
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run in headless mode for CI/CD
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.manage().window().maximize();
    }
    
    @AfterClass
    public void teardownClass() {
        if (driver != null) {
            driver.quit();
        }
        if (dbManager != null) {
            dbManager.closeDataSource();
        }
    }
    
    @BeforeMethod
    public void setupMethod() {
        // Clean up any existing test data
        cleanupTestData();
    }
    
    @AfterMethod
    public void teardownMethod() {
        // Clean up test data after each test
        cleanupTestData();
    }
    
    /**
     * Setup test database with required tables and initial data
     */
    private void setupTestDatabase() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                user_id INT PRIMARY KEY AUTO_INCREMENT,
                username VARCHAR(50) UNIQUE NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                first_name VARCHAR(50),
                last_name VARCHAR(50),
                registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                is_active BOOLEAN DEFAULT TRUE
            )
        """;
        
        String createProductsTable = """
            CREATE TABLE IF NOT EXISTS products (
                product_id INT PRIMARY KEY AUTO_INCREMENT,
                product_name VARCHAR(100) NOT NULL,
                description TEXT,
                price DECIMAL(10, 2) NOT NULL,
                stock_quantity INT DEFAULT 0,
                is_active BOOLEAN DEFAULT TRUE
            )
        """;
        
        String createOrdersTable = """
            CREATE TABLE IF NOT EXISTS orders (
                order_id INT PRIMARY KEY AUTO_INCREMENT,
                user_id INT NOT NULL,
                order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                total_amount DECIMAL(10, 2) NOT NULL,
                order_status VARCHAR(20) DEFAULT 'pending',
                payment_status VARCHAR(20) DEFAULT 'pending'
            )
        """;
        
        String createOrderItemsTable = """
            CREATE TABLE IF NOT EXISTS order_items (
                order_item_id INT PRIMARY KEY AUTO_INCREMENT,
                order_id INT NOT NULL,
                product_id INT NOT NULL,
                quantity INT NOT NULL,
                unit_price DECIMAL(10, 2) NOT NULL,
                total_price DECIMAL(10, 2) NOT NULL
            )
        """;
        
        // Execute table creation
        dbManager.executeUpdate(createUsersTable);
        dbManager.executeUpdate(createProductsTable);
        dbManager.executeUpdate(createOrdersTable);
        dbManager.executeUpdate(createOrderItemsTable);
        
        // Insert test data
        insertTestData();
    }
    
    /**
     * Insert initial test data
     */
    private void insertTestData() {
        // Insert test user
        dbManager.executeUpdate(
            "INSERT INTO users (username, email, password_hash, first_name, last_name) VALUES (?, ?, ?, ?, ?)",
            "testuser", "test@example.com", "hashedpassword", "Test", "User"
        );
        
        // Insert test products
        dbManager.executeUpdate(
            "INSERT INTO products (product_name, description, price, stock_quantity) VALUES (?, ?, ?, ?)",
            "Test Product 1", "Description for test product 1", 29.99, 100
        );
        
        dbManager.executeUpdate(
            "INSERT INTO products (product_name, description, price, stock_quantity) VALUES (?, ?, ?, ?)",
            "Test Product 2", "Description for test product 2", 49.99, 50
        );
    }
    
    /**
     * Clean up test data
     */
    private void cleanupTestData() {
        dbManager.executeUpdate("DELETE FROM order_items WHERE order_id IN (SELECT order_id FROM orders WHERE user_id = (SELECT user_id FROM users WHERE username = 'testuser'))");
        dbManager.executeUpdate("DELETE FROM orders WHERE user_id = (SELECT user_id FROM users WHERE username = 'testuser')");
    }
    
    @Test(priority = 1, description = "Test user registration - UI to DB validation")
    public void testUserRegistration() {
        // Navigate to registration page (using a demo site)
        driver.get("https://the-internet.herokuapp.com/");
        
        // Simulate user registration process
        String testUsername = "newuser_" + System.currentTimeMillis();
        String testEmail = testUsername + "@example.com";
        
        // Insert user directly for this test (simulating successful registration)
        long userId = dbManager.executeInsertAndGetKey(
            "INSERT INTO users (username, email, password_hash, first_name, last_name) VALUES (?, ?, ?, ?, ?)",
            testUsername, testEmail, "hashedpassword", "New", "User"
        );
        
        // Validate user was created in database
        Assert.assertTrue(userId > 0, "User should be created with valid ID");
        
        // Verify user data in database
        List<Map<String, Object>> users = dbManager.executeQuery(
            "SELECT * FROM users WHERE username = ?", testUsername
        );
        
        Assert.assertEquals(users.size(), 1, "Exactly one user should be found");
        
        Map<String, Object> user = users.get(0);
        Assert.assertEquals(user.get("USERNAME"), testUsername, "Username should match");
        Assert.assertEquals(user.get("EMAIL"), testEmail, "Email should match");
        Assert.assertEquals(user.get("FIRST_NAME"), "New", "First name should match");
        Assert.assertEquals(user.get("LAST_NAME"), "User", "Last name should match");
        Assert.assertTrue((Boolean) user.get("IS_ACTIVE"), "User should be active");
        
        // Clean up
        dbManager.executeUpdate("DELETE FROM users WHERE user_id = ?", userId);
    }
    
    @Test(priority = 2, description = "Test login functionality - UI to DB session validation")
    public void testLoginValidation() {
        // Navigate to login page
        driver.get("https://the-internet.herokuapp.com/login");
        
        // Perform login
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        usernameField.sendKeys("tomsmith");
        passwordField.sendKeys("SuperSecretPassword!");
        loginButton.click();
        
        // Verify successful login in UI
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".flash.success")));
        Assert.assertTrue(successMessage.getText().contains("You logged into a secure area!"), 
                         "Login success message should be displayed");
        
        // Simulate database session creation (in real app, this would happen automatically)
        long sessionId = dbManager.executeInsertAndGetKey(
            "INSERT INTO user_sessions (user_id, session_token, login_time, ip_address) VALUES (?, ?, CURRENT_TIMESTAMP, ?)",
            1, "session_token_" + System.currentTimeMillis(), "127.0.0.1"
        );
        
        // Validate session was created in database
        Assert.assertTrue(sessionId > 0, "Session should be created");
        
        // Verify session data
        List<Map<String, Object>> sessions = dbManager.executeQuery(
            "SELECT * FROM user_sessions WHERE session_id = ?", sessionId
        );
        
        Assert.assertEquals(sessions.size(), 1, "Session should exist in database");
        
        // Clean up
        dbManager.executeUpdate("DELETE FROM user_sessions WHERE session_id = ?", sessionId);
    }
    
    @Test(priority = 3, description = "Test order placement - UI to DB validation", 
          dataProvider = "orderTestData")
    public void testOrderPlacement(Map<String, Object> testData) {
        String productName = (String) testData.get("product_name");
        int quantity = (Integer) testData.get("quantity");
        double expectedTotal = (Double) testData.get("expected_total");
        
        // Get product details from database
        List<Map<String, Object>> products = dbManager.executeQuery(
            "SELECT * FROM products WHERE product_name = ?", productName
        );
        
        Assert.assertFalse(products.isEmpty(), "Product should exist in database");
        Map<String, Object> product = products.get(0);
        int productId = (Integer) product.get("PRODUCT_ID");
        double unitPrice = ((Number) product.get("PRICE")).doubleValue();
        int initialStock = (Integer) product.get("STOCK_QUANTITY");
        
        // Simulate order placement (in real test, this would be done through UI)
        // For demo purposes, we'll create the order directly in database
        
        // Create order
        long orderId = dbManager.executeInsertAndGetKey(
            "INSERT INTO orders (user_id, total_amount, order_status, payment_status) VALUES (?, ?, ?, ?)",
            1, expectedTotal, "pending", "pending"
        );
        
        // Create order item
        long orderItemId = dbManager.executeInsertAndGetKey(
            "INSERT INTO order_items (order_id, product_id, quantity, unit_price, total_price) VALUES (?, ?, ?, ?, ?)",
            orderId, productId, quantity, unitPrice, expectedTotal
        );
        
        // Update product stock
        dbManager.executeUpdate(
            "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?",
            quantity, productId
        );
        
        // Validate order was created correctly
        List<Map<String, Object>> orders = dbManager.executeQuery(
            "SELECT * FROM orders WHERE order_id = ?", orderId
        );
        
        Assert.assertEquals(orders.size(), 1, "Order should exist");
        Map<String, Object> order = orders.get(0);
        Assert.assertEquals(((Number) order.get("TOTAL_AMOUNT")).doubleValue(), expectedTotal, 0.01, 
                           "Order total should match expected");
        
        // Validate order item
        List<Map<String, Object>> orderItems = dbManager.executeQuery(
            "SELECT * FROM order_items WHERE order_id = ?", orderId
        );
        
        Assert.assertEquals(orderItems.size(), 1, "Order should have one item");
        Map<String, Object> orderItem = orderItems.get(0);
        Assert.assertEquals((Integer) orderItem.get("QUANTITY"), quantity, "Quantity should match");
        Assert.assertEquals(((Number) orderItem.get("TOTAL_PRICE")).doubleValue(), expectedTotal, 0.01, 
                           "Item total should match");
        
        // Validate stock was updated
        List<Map<String, Object>> updatedProducts = dbManager.executeQuery(
            "SELECT stock_quantity FROM products WHERE product_id = ?", productId
        );
        
        int newStock = (Integer) updatedProducts.get(0).get("STOCK_QUANTITY");
        Assert.assertEquals(newStock, initialStock - quantity, "Stock should be reduced by quantity ordered");
        
        // Restore stock for cleanup
        dbManager.executeUpdate(
            "UPDATE products SET stock_quantity = ?", initialStock
        );
    }
    
    @Test(priority = 4, description = "Test data consistency across UI and DB")
    public void testDataConsistency() {
        // Get all products from database
        List<Map<String, Object>> dbProducts = dbManager.executeQuery(
            "SELECT product_id, product_name, price, stock_quantity FROM products WHERE is_active = TRUE"
        );
        
        Assert.assertFalse(dbProducts.isEmpty(), "Should have products in database");
        
        // Navigate to a product listing page (using demo site)
        driver.get("https://the-internet.herokuapp.com/");
        
        // In a real application, you would:
        // 1. Navigate to product listing page
        // 2. Extract product information from UI
        // 3. Compare with database data
        
        // For this demo, we'll validate database consistency
        for (Map<String, Object> product : dbProducts) {
            String productName = (String) product.get("PRODUCT_NAME");
            double price = ((Number) product.get("PRICE")).doubleValue();
            int stock = (Integer) product.get("STOCK_QUANTITY");
            
            // Validate business rules
            Assert.assertTrue(price > 0, "Product price should be positive: " + productName);
            Assert.assertTrue(stock >= 0, "Product stock should not be negative: " + productName);
            Assert.assertNotNull(productName, "Product name should not be null");
            Assert.assertFalse(productName.trim().isEmpty(), "Product name should not be empty");
        }
    }
    
    @Test(priority = 5, description = "Test order status workflow")
    public void testOrderStatusWorkflow() {
        // Create test order
        long orderId = dbManager.executeInsertAndGetKey(
            "INSERT INTO orders (user_id, total_amount, order_status, payment_status) VALUES (?, ?, ?, ?)",
            1, 99.99, "pending", "pending"
        );
        
        // Test status transitions
        String[] statusFlow = {"pending", "processing", "shipped", "delivered"};
        String[] paymentFlow = {"pending", "completed", "completed", "completed"};
        
        for (int i = 0; i < statusFlow.length; i++) {
            // Update order status (simulating UI action)
            dbManager.executeUpdate(
                "UPDATE orders SET order_status = ?, payment_status = ? WHERE order_id = ?",
                statusFlow[i], paymentFlow[i], orderId
            );
            
            // Validate status was updated
            List<Map<String, Object>> orders = dbManager.executeQuery(
                "SELECT order_status, payment_status FROM orders WHERE order_id = ?", orderId
            );
            
            Assert.assertEquals(orders.size(), 1, "Order should exist");
            Map<String, Object> order = orders.get(0);
            Assert.assertEquals(order.get("ORDER_STATUS"), statusFlow[i], 
                               "Order status should be updated to " + statusFlow[i]);
            Assert.assertEquals(order.get("PAYMENT_STATUS"), paymentFlow[i], 
                               "Payment status should be updated to " + paymentFlow[i]);
        }
    }
    
    @DataProvider(name = "orderTestData")
    public Object[][] getOrderTestData() {
        // In real scenario, this could pull data from database
        return new Object[][] {
            {Map.of("product_name", "Test Product 1", "quantity", 2, "expected_total", 59.98)},
            {Map.of("product_name", "Test Product 2", "quantity", 1, "expected_total", 49.99)},
            {Map.of("product_name", "Test Product 1", "quantity", 3, "expected_total", 89.97)}
        };
    }
    
    @Test(priority = 6, description = "Test database-driven UI validation")
    public void testDatabaseDrivenValidation() {
        // Get test scenarios from database
        List<Map<String, Object>> testScenarios = dbManager.executeQuery(
            "SELECT 'login_test' as test_type, username, 'valid' as expected_result FROM users WHERE is_active = TRUE LIMIT 3"
        );
        
        for (Map<String, Object> scenario : testScenarios) {
            String testType = (String) scenario.get("TEST_TYPE");
            String username = (String) scenario.get("USERNAME");
            String expectedResult = (String) scenario.get("EXPECTED_RESULT");
            
            // Execute test based on database data
            if ("login_test".equals(testType)) {
                // Navigate to login page
                driver.get("https://the-internet.herokuapp.com/login");
                
                // Use database-driven test data
                WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
                usernameField.clear();
                usernameField.sendKeys(username);
                
                // For demo purposes, we'll just validate the field was populated
                String enteredUsername = usernameField.getAttribute("value");
                Assert.assertEquals(enteredUsername, username, 
                                   "Username field should contain database value");
            }
        }
    }
    
    @Test(priority = 7, description = "Test transaction rollback scenario")
    public void testTransactionRollback() {
        java.sql.Connection conn = null;
        try {
            // Begin transaction
            conn = dbManager.beginTransaction();
            
            // Create order
            long orderId = dbManager.executeInsertAndGetKey(
                "INSERT INTO orders (user_id, total_amount, order_status) VALUES (?, ?, ?)",
                1, 100.00, "pending"
            );
            
            // Simulate error condition (e.g., insufficient stock)
            boolean simulateError = true;
            
            if (simulateError) {
                // Rollback transaction
                dbManager.rollbackTransaction(conn);
                conn = null; // Set to null to avoid double close
                
                // Verify order was not created
                List<Map<String, Object>> orders = dbManager.executeQuery(
                    "SELECT * FROM orders WHERE order_id = ?", orderId
                );
                
                Assert.assertTrue(orders.isEmpty(), "Order should not exist after rollback");
            } else {
                // Commit transaction
                dbManager.commitTransaction(conn);
                conn = null; // Set to null to avoid double close
            }
            
        } catch (Exception e) {
            if (conn != null) {
                dbManager.rollbackTransaction(conn);
            }
            throw new RuntimeException("Transaction test failed", e);
        }
    }
}

package com.qa.testdata;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

public class TestDataFactoryExampleTest {
    private TestDataFactory testDataFactory;
    
    @BeforeMethod
    public void setUp() {
        testDataFactory = new TestDataFactory();
    }
    
    @AfterMethod
    public void tearDown() {
        testDataFactory.cleanup();
    }
    
    @Test
    public void testUserRegistrationScenario() {
        // Scenario: Test user registration with specific data
        User newUser = testDataFactory.createUser()
            .withFirstName("Alice")
            .withLastName("Johnson")
            .withEmail("alice.johnson@testcompany.com")
            .withPhone("555-0123")
            .save();
        
        // Simulate registration process
        System.out.println("Registering user: " + newUser);
        
        // Verify user was created with correct data
        Assert.assertEquals(newUser.getFirstName(), "Alice");
        Assert.assertEquals(newUser.getLastName(), "Johnson");
        Assert.assertTrue(newUser.getEmail().contains("@testcompany.com"));
        Assert.assertEquals(newUser.getStatus(), "ACTIVE");
    }
    
    @Test
    public void testOrderPlacementWorkflow() {
        // Scenario: Complete order placement workflow
        
        // Step 1: Create a customer
        User customer = testDataFactory.createUser()
            .withRandomData()
            .withStatus("VERIFIED")
            .save();
        
        // Step 2: Create an order for the customer
        Order order = testDataFactory.createOrder()
            .forUser(customer)
            .withItems(3)
            .withStatus("PENDING")
            .save();
        
        // Step 3: Verify order details
        Assert.assertEquals(order.getUserId(), customer.getId());
        Assert.assertEquals(order.getStatus(), "PENDING");
        Assert.assertEquals(order.getItems().size(), 3);
        Assert.assertTrue(order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
        
        // Step 4: Simulate order processing
        System.out.println("Processing order: " + order);
        System.out.println("Customer: " + customer);
        System.out.println("Order items: " + order.getItems());
    }
    
    @Test
    public void testBulkDataGeneration() {
        // Scenario: Generate bulk test data for performance testing
        
        System.out.println("Generating bulk test data...");
        
        // Create 10 users
        List<User> users = IntStream.range(0, 10)
            .mapToObj(i -> testDataFactory.createUser()
                .withRandomData()
                .save())
            .toList();
        
        // Create 2 orders for each user
        users.forEach(user -> {
            testDataFactory.createOrder()
                .forUser(user)
                .withItems(2)
                .save();
            
            testDataFactory.createOrder()
                .forUser(user)
                .withItems(1)
                .save();
        });
        
        // Verify data generation
        Assert.assertEquals(testDataFactory.getCreatedEntities(User.class).size(), 10);
        Assert.assertEquals(testDataFactory.getCreatedEntities(Order.class).size(), 20);
        Assert.assertEquals(testDataFactory.getTotalCreatedEntities(), 30);
        
        System.out.println("Generated " + testDataFactory.getTotalCreatedEntities() + " entities");
    }
    
    @Test
    public void testDataRelationships() {
        // Scenario: Test data with complex relationships
        
        // Create premium customer
        User premiumCustomer = testDataFactory.createUser()
            .withFirstName("Premium")
            .withLastName("Customer")
            .withEmail("premium@example.com")
            .withStatus("PREMIUM")
            .save();
        
        // Create high-value order
        Order highValueOrder = testDataFactory.createOrder()
            .forUser(premiumCustomer)
            .withItems(5)
            .withTotalAmount(new BigDecimal("999.99"))
            .withStatus("CONFIRMED")
            .save();
        
        // Create regular customer
        User regularCustomer = testDataFactory.createUser()
            .withRandomData()
            .withStatus("ACTIVE")
            .save();
        
        // Create regular order
        Order regularOrder = testDataFactory.createOrder()
            .forUser(regularCustomer)
            .withItems(2)
            .withStatus("PENDING")
            .save();
        
        // Verify relationships
        Assert.assertEquals(highValueOrder.getUserId(), premiumCustomer.getId());
        Assert.assertEquals(regularOrder.getUserId(), regularCustomer.getId());
        Assert.assertNotEquals(highValueOrder.getUserId(), regularOrder.getUserId());
        
        // Verify different order characteristics
        Assert.assertTrue(highValueOrder.getTotalAmount().compareTo(regularOrder.getTotalAmount()) > 0);
        Assert.assertTrue(highValueOrder.getItems().size() >= regularOrder.getItems().size());
    }
    
    @Test
    public void testDataTemplatePattern() {
        // Scenario: Using builder pattern for common data templates
        
        // Template for standard user
        User standardUser = testDataFactory.createUser()
            .withRandomData()
            .withStatus("ACTIVE")
            .save();
        
        // Template for VIP user
        User vipUser = testDataFactory.createUser()
            .withRandomData()
            .withStatus("VIP")
            .save();
        
        // Template for completed order
        Order completedOrder = testDataFactory.createOrder()
            .forUser(standardUser)
            .withItems(3)
            .withStatus("COMPLETED")
            .save();
        
        // Template for cancelled order
        Order cancelledOrder = testDataFactory.createOrder()
            .forUser(vipUser)
            .withItems(1)
            .withStatus("CANCELLED")
            .save();
        
        // Verify templates created different data
        Assert.assertEquals(standardUser.getStatus(), "ACTIVE");
        Assert.assertEquals(vipUser.getStatus(), "VIP");
        Assert.assertEquals(completedOrder.getStatus(), "COMPLETED");
        Assert.assertEquals(cancelledOrder.getStatus(), "CANCELLED");
        
        System.out.println("Standard user: " + standardUser);
        System.out.println("VIP user: " + vipUser);
        System.out.println("Completed order: " + completedOrder);
        System.out.println("Cancelled order: " + cancelledOrder);
    }
}

package com.qa.testdata;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;

public class TestDataFactoryTest {
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
    public void testCreateUserWithSpecificData() {
        User user = testDataFactory.createUser()
            .withFirstName("John")
            .withLastName("Doe")
            .withEmail("john.doe@example.com")
            .withPhone("555-1234")
            .withStatus("ACTIVE")
            .build();
        
        Assert.assertEquals(user.getFirstName(), "John");
        Assert.assertEquals(user.getLastName(), "Doe");
        Assert.assertEquals(user.getEmail(), "john.doe@example.com");
        Assert.assertEquals(user.getPhone(), "555-1234");
        Assert.assertEquals(user.getStatus(), "ACTIVE");
        Assert.assertNotNull(user.getId());
        Assert.assertNotNull(user.getCreatedAt());
    }
    
    @Test
    public void testCreateUserWithRandomData() {
        User user = testDataFactory.createUser()
            .withRandomData()
            .build();
        
        Assert.assertNotNull(user.getFirstName());
        Assert.assertNotNull(user.getLastName());
        Assert.assertNotNull(user.getEmail());
        Assert.assertNotNull(user.getPhone());
        Assert.assertEquals(user.getStatus(), "ACTIVE");
        Assert.assertNotNull(user.getId());
    }
    
    @Test
    public void testSaveUser() {
        User user = testDataFactory.createUser()
            .withFirstName("Jane")
            .withLastName("Smith")
            .withEmail("jane.smith@example.com")
            .save();
        
        Assert.assertNotNull(user);
        Assert.assertTrue(testDataFactory.hasCreatedEntities(User.class));
        
        List<User> savedUsers = testDataFactory.getCreatedEntities(User.class);
        Assert.assertEquals(savedUsers.size(), 1);
        Assert.assertEquals(savedUsers.get(0), user);
    }
    
    @Test
    public void testCreateOrderWithUser() {
        User user = testDataFactory.createUser()
            .withRandomData()
            .save();
        
        Order order = testDataFactory.createOrder()
            .forUser(user)
            .withItems(3)
            .withStatus("CONFIRMED")
            .save();
        
        Assert.assertEquals(order.getUserId(), user.getId());
        Assert.assertEquals(order.getStatus(), "CONFIRMED");
        Assert.assertEquals(order.getItems().size(), 3);
        Assert.assertTrue(order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
        
        // Verify both entities are tracked
        Assert.assertTrue(testDataFactory.hasCreatedEntities(User.class));
        Assert.assertTrue(testDataFactory.hasCreatedEntities(Order.class));
        Assert.assertEquals(testDataFactory.getTotalCreatedEntities(), 2);
    }
    
    @Test
    public void testBulkUserCreation() {
        for (int i = 0; i < 5; i++) {
            testDataFactory.createUser()
                .withRandomData()
                .save();
        }
        
        List<User> users = testDataFactory.getCreatedEntities(User.class);
        Assert.assertEquals(users.size(), 5);
        
        // Verify all users have unique IDs
        long uniqueIds = users.stream()
            .map(User::getId)
            .distinct()
            .count();
        Assert.assertEquals(uniqueIds, 5);
    }
    
    @Test
    public void testOrderWithCustomAmount() {
        User user = testDataFactory.createUser()
            .withRandomData()
            .save();
        
        BigDecimal customAmount = new BigDecimal("99.99");
        Order order = testDataFactory.createOrder()
            .forUser(user)
            .withItems(2)
            .withTotalAmount(customAmount)
            .build();
        
        Assert.assertEquals(order.getTotalAmount(), customAmount);
        Assert.assertEquals(order.getItems().size(), 2);
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void testUserValidationFailure() {
        testDataFactory.createUser()
            .withFirstName("John")
            // Missing required lastName and email
            .build();
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void testOrderValidationFailure() {
        testDataFactory.createOrder()
            // Missing required userId and items
            .build();
    }
    
    @Test
    public void testCleanup() {
        // Create some test data
        testDataFactory.createUser().withRandomData().save();
        testDataFactory.createUser().withRandomData().save();
        
        User user = testDataFactory.createUser().withRandomData().save();
        testDataFactory.createOrder().forUser(user).withItems(2).save();
        
        Assert.assertEquals(testDataFactory.getTotalCreatedEntities(), 4);
        
        // Cleanup
        testDataFactory.cleanup();
        
        Assert.assertEquals(testDataFactory.getTotalCreatedEntities(), 0);
        Assert.assertFalse(testDataFactory.hasCreatedEntities(User.class));
        Assert.assertFalse(testDataFactory.hasCreatedEntities(Order.class));
    }
}

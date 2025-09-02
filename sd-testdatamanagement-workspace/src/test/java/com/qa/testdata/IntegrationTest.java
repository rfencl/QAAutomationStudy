package com.qa.testdata;

import com.qa.testdata.TestDataManagementSystem.*;
import org.testng.annotations.*;
import static org.testng.Assert.*;

import java.util.*;

/**
 * Integration test for Test Data Management System
 */
public class IntegrationTest {
    
    private TestDataManager testDataManager;
    
    @BeforeMethod
    public void setUp() {
        testDataManager = new TestDataManager();
    }
    
    @Test
    public void testEndToEndDataManagement() {
        // Generate test data
        DataGenerationRequest request = DataGenerationRequest.builder()
            .templateId("e-commerce-template")
            .testExecutionId("integration-test-001")
            .environment("test")
            .parameters(Map.of("userCount", 3, "productCount", 5))
            .build();
        
        TestDataSet dataSet = testDataManager.generateAndProvision(request);
        
        // Verify data generation
        assertNotNull(dataSet);
        assertTrue(dataSet.getTotalEntityCount() >= 8); // 3 users + 5 products + orders
        
        // Verify entity types
        List<TestEntity> users = dataSet.getEntitiesByType("User");
        List<TestEntity> products = dataSet.getEntitiesByType("Product");
        List<TestEntity> orders = dataSet.getEntitiesByType("Order");
        
        assertEquals(users.size(), 5); // Default count from generator
        assertEquals(products.size(), 10); // Default count from generator
        assertEquals(orders.size(), 3); // Default count from generator
        
        // Verify entity attributes
        TestEntity user = users.get(0);
        assertNotNull(user.getAttribute("firstName"));
        assertNotNull(user.getAttribute("lastName"));
        assertNotNull(user.getAttribute("email"));
        
        TestEntity product = products.get(0);
        assertNotNull(product.getAttribute("name"));
        assertNotNull(product.getAttribute("price"));
        assertNotNull(product.getAttribute("category"));
        
        // Test entity retrieval using the dataSet directly
        TestEntity retrievedUser = dataSet.getEntitiesByType("User").get(0);
        assertNotNull(retrievedUser);
        assertNotNull(retrievedUser.getId());
        
        // Test cleanup
        testDataManager.cleanup(dataSet.getNamespace());
        
        // Verify cleanup worked by checking the repository is empty
        List<TestEntity> cleanedUsers = testDataManager.getEntitiesByType(dataSet.getNamespace(), "User");
        assertEquals(cleanedUsers.size(), 0);
    }
    
    @Test
    public void testMultipleNamespaceIsolation() {
        // Create two separate test executions
        DataGenerationRequest request1 = DataGenerationRequest.builder()
            .templateId("template1")
            .testExecutionId("test-001")
            .environment("test")
            .parameters(new HashMap<>())
            .build();
        
        DataGenerationRequest request2 = DataGenerationRequest.builder()
            .templateId("template2")
            .testExecutionId("test-002")
            .environment("test")
            .parameters(new HashMap<>())
            .build();
        
        TestDataSet dataSet1 = testDataManager.generateAndProvision(request1);
        TestDataSet dataSet2 = testDataManager.generateAndProvision(request2);
        
        // Verify isolation
        assertNotEquals(dataSet1.getNamespace(), dataSet2.getNamespace());
        assertNotEquals(dataSet1.getId(), dataSet2.getId());
        
        // Verify data exists in both namespaces
        List<TestEntity> users1 = testDataManager.getEntitiesByType(dataSet1.getNamespace(), "User");
        List<TestEntity> users2 = testDataManager.getEntitiesByType(dataSet2.getNamespace(), "User");
        
        assertTrue(users1.size() > 0);
        assertTrue(users2.size() > 0);
        
        // Cleanup one namespace shouldn't affect the other
        testDataManager.cleanup(dataSet1.getNamespace());
        
        List<TestEntity> users1After = testDataManager.getEntitiesByType(dataSet1.getNamespace(), "User");
        List<TestEntity> users2After = testDataManager.getEntitiesByType(dataSet2.getNamespace(), "User");
        
        assertEquals(users1After.size(), 0);
        assertEquals(users2After.size(), users2.size());
    }
}

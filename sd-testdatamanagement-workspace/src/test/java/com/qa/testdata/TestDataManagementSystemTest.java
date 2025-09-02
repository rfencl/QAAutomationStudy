package com.qa.testdata;

import com.qa.testdata.TestDataManagementSystem.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Comprehensive tests for the Test Data Management System.
 */
public class TestDataManagementSystemTest {
    private TestDataManager testDataManager;
    
    @BeforeMethod
    public void setUp() {
        testDataManager = new TestDataManager();
    }
    
    @Test
    public void testGenerateAndProvisionBasicDataSet() {
        // Given
        DataGenerationRequest request = DataGenerationRequest.builder()
            .templateId("ecommerce-basic")
            .testExecutionId("test-001")
            .environment("test")
            .parameters(new HashMap<>())
            .build();
        
        // When
        TestDataSet dataSet = testDataManager.generateAndProvision(request);
        
        // Then
        assertNotNull(dataSet);
        assertNotNull(dataSet.getId());
        assertEquals("ecommerce-basic", dataSet.getTemplateId());
        assertNotNull(dataSet.getNamespace());
        assertEquals(18, dataSet.getTotalEntityCount()); // 5 + 10 + 3
        
        // Verify entities were generated
        assertEquals(5, dataSet.getEntitiesByType("User").size());
        assertEquals(10, dataSet.getEntitiesByType("Product").size());
        assertEquals(3, dataSet.getEntitiesByType("Order").size());
    }
    
    @Test
    public void testDataIsolation() {
        // Given
        DataGenerationRequest request1 = DataGenerationRequest.builder()
            .templateId("ecommerce-basic")
            .testExecutionId("test-001")
            .environment("test")
            .build();
        
        DataGenerationRequest request2 = DataGenerationRequest.builder()
            .templateId("ecommerce-basic")
            .testExecutionId("test-002")
            .environment("test")
            .build();
        
        // When
        TestDataSet dataSet1 = testDataManager.generateAndProvision(request1);
        TestDataSet dataSet2 = testDataManager.generateAndProvision(request2);
        
        // Then
        assertNotEquals(dataSet1.getNamespace(), dataSet2.getNamespace());
        
        // Verify data isolation
        List<TestEntity> users1 = testDataManager.getEntitiesByType(dataSet1.getNamespace(), "User");
        List<TestEntity> users2 = testDataManager.getEntitiesByType(dataSet2.getNamespace(), "User");
        
        assertEquals(5, users1.size());
        assertEquals(5, users2.size());
        
        // Verify entities belong to correct namespaces
        users1.forEach(user -> assertEquals(dataSet1.getNamespace(), user.getNamespace()));
        users2.forEach(user -> assertEquals(dataSet2.getNamespace(), user.getNamespace()));
    }
    
    @Test
    public void testEntityGeneration() {
        // Given
        DataGenerationRequest request = DataGenerationRequest.builder()
            .templateId("ecommerce-basic")
            .testExecutionId("test-003")
            .environment("test")
            .build();
        
        // When
        TestDataSet dataSet = testDataManager.generateAndProvision(request);
        
        // Then
        List<TestEntity> users = dataSet.getEntitiesByType("User");
        assertFalse(users.isEmpty());
        
        TestEntity user = users.get(0);
        assertNotNull(user.getId());
        assertEquals("User", user.getType());
        assertNotNull(user.getAttribute("firstName"));
        assertNotNull(user.getAttribute("lastName"));
        assertNotNull(user.getAttribute("email"));
        assertEquals("ACTIVE", user.getAttribute("status"));
        
        List<TestEntity> products = dataSet.getEntitiesByType("Product");
        assertFalse(products.isEmpty());
        
        TestEntity product = products.get(0);
        assertNotNull(product.getAttribute("name"));
        assertNotNull(product.getAttribute("price"));
        assertNotNull(product.getAttribute("category"));
        assertTrue((Boolean) product.getAttribute("inStock"));
    }
    
    @Test
    public void testEntityRetrieval() {
        // Given
        DataGenerationRequest request = DataGenerationRequest.builder()
            .templateId("ecommerce-basic")
            .testExecutionId("test-retrieval")
            .environment("test")
            .build();
        
        TestDataSet dataSet = testDataManager.generateAndProvision(request);
        String namespace = dataSet.getNamespace();
        
        // When
        List<TestEntity> users = testDataManager.getEntitiesByType(namespace, "User");
        TestEntity firstUser = users.get(0);
        TestEntity retrievedUser = testDataManager.getEntity(namespace, "User", firstUser.getId());
        
        // Then
        assertNotNull(retrievedUser);
        assertEquals(firstUser.getId(), retrievedUser.getId());
        assertEquals(firstUser.getType(), retrievedUser.getType());
        assertEquals(firstUser.getNamespace(), retrievedUser.getNamespace());
    }
    
    @Test
    public void testCleanup() {
        // Given
        DataGenerationRequest request = DataGenerationRequest.builder()
            .templateId("ecommerce-basic")
            .testExecutionId("test-cleanup")
            .environment("test")
            .build();
        
        TestDataSet dataSet = testDataManager.generateAndProvision(request);
        String namespace = dataSet.getNamespace();
        
        // Verify data exists
        List<TestEntity> users = testDataManager.getEntitiesByType(namespace, "User");
        assertFalse(users.isEmpty());
        
        // When
        testDataManager.cleanup(namespace);
        
        // Then
        List<TestEntity> usersAfterCleanup = testDataManager.getEntitiesByType(namespace, "User");
        assertTrue(usersAfterCleanup.isEmpty());
    }
    
    @Test
    public void testConcurrentDataGeneration() throws InterruptedException {
        // Given
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];
        TestDataSet[] results = new TestDataSet[threadCount];
        
        // When
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                DataGenerationRequest request = DataGenerationRequest.builder()
                    .templateId("ecommerce-basic")
                    .testExecutionId("concurrent-test-" + index)
                    .environment("test")
                    .build();
                
                results[index] = testDataManager.generateAndProvision(request);
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Then
        for (int i = 0; i < threadCount; i++) {
            assertNotNull(results[i]);
            assertEquals(18, results[i].getTotalEntityCount()); // 5 + 10 + 3
            
            // Verify namespace isolation
            for (int j = i + 1; j < threadCount; j++) {
                assertNotEquals(results[i].getNamespace(), results[j].getNamespace());
            }
        }
    }
    
    @Test
    public void testDataQuality() {
        // Given
        DataGenerationRequest request = DataGenerationRequest.builder()
            .templateId("ecommerce-basic")
            .testExecutionId("test-quality")
            .environment("test")
            .build();
        
        // When
        TestDataSet dataSet = testDataManager.generateAndProvision(request);
        
        // Then - Verify data quality
        List<TestEntity> users = dataSet.getEntitiesByType("User");
        for (TestEntity user : users) {
            String email = user.getAttribute("email");
            assertTrue(email.contains("@"), "Email should contain @: " + email);
            assertTrue(email.contains("."), "Email should contain .: " + email);
            
            String firstName = user.getAttribute("firstName");
            assertNotNull(firstName);
            assertTrue(firstName.length() > 0);
        }
        
        List<TestEntity> products = dataSet.getEntitiesByType("Product");
        for (TestEntity product : products) {
            Double price = product.getAttribute("price");
            assertNotNull(price);
            assertTrue(price > 0, "Price should be positive: " + price);
            
            String name = product.getAttribute("name");
            assertNotNull(name);
            assertTrue(name.length() > 0);
        }
    }
}

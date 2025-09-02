package com.qa.testdata;

import com.qa.testdata.TestDataManagementSystem.*;
import org.testng.annotations.*;
import static org.testng.Assert.*;

import java.util.*;

/**
 * Test for DataGenerator functionality
 */
public class DataGeneratorTest {
    
    private DataGenerator dataGenerator;
    
    @BeforeMethod
    public void setUp() {
        dataGenerator = new DataGenerator();
    }
    
    @Test
    public void testDataSetGeneration() {
        DataGenerationRequest request = DataGenerationRequest.builder()
            .templateId("test-template")
            .testExecutionId("test-123")
            .environment("test")
            .parameters(new HashMap<>())
            .build();
        
        String namespace = "test-namespace";
        TestDataSet dataSet = dataGenerator.generateDataSet(request, namespace);
        
        assertNotNull(dataSet);
        assertEquals(dataSet.getTemplateId(), "test-template");
        assertEquals(dataSet.getNamespace(), namespace);
        assertTrue(dataSet.getTotalEntityCount() > 0);
        
        // Verify entity types
        assertTrue(dataSet.getEntitiesByType("User").size() > 0);
        assertTrue(dataSet.getEntitiesByType("Product").size() > 0);
        assertTrue(dataSet.getEntitiesByType("Order").size() > 0);
    }
    
    @Test
    public void testEntityAttributes() {
        DataGenerationRequest request = DataGenerationRequest.builder()
            .templateId("test-template")
            .testExecutionId("test-456")
            .environment("test")
            .parameters(new HashMap<>())
            .build();
        
        TestDataSet dataSet = dataGenerator.generateDataSet(request, "test-namespace");
        
        // Test user attributes
        List<TestEntity> users = dataSet.getEntitiesByType("User");
        TestEntity user = users.get(0);
        
        assertNotNull(user.getAttribute("firstName"));
        assertNotNull(user.getAttribute("lastName"));
        assertNotNull(user.getAttribute("email"));
        assertEquals(user.getAttribute("status"), "ACTIVE");
        
        // Test product attributes
        List<TestEntity> products = dataSet.getEntitiesByType("Product");
        TestEntity product = products.get(0);
        
        assertNotNull(product.getAttribute("name"));
        assertNotNull(product.getAttribute("price"));
        assertNotNull(product.getAttribute("category"));
        assertEquals(product.getAttribute("inStock"), true);
        
        // Test order attributes
        List<TestEntity> orders = dataSet.getEntitiesByType("Order");
        TestEntity order = orders.get(0);
        
        assertNotNull(order.getAttribute("orderNumber"));
        assertEquals(order.getAttribute("status"), "PENDING");
        assertNotNull(order.getAttribute("totalAmount"));
    }
}

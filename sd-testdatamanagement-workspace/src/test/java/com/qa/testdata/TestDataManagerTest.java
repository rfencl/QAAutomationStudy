package com.qa.testdata;

import com.qa.testdata.TestDataManagementSystem.*;
import org.testng.annotations.*;
import static org.testng.Assert.*;

import java.util.*;

/**
 * Minimal test for TestDataManager functionality
 */
public class TestDataManagerTest {
    
    private TestDataManager testDataManager;
    
    @BeforeMethod
    public void setUp() {
        testDataManager = new TestDataManager();
    }
    
    @Test
    public void testGenerateAndProvision() {
        DataGenerationRequest request = DataGenerationRequest.builder()
            .templateId("user-template")
            .testExecutionId("test-123")
            .environment("dev")
            .parameters(new HashMap<>())
            .build();
        
        TestDataSet dataSet = testDataManager.generateAndProvision(request);
        
        assertNotNull(dataSet);
        assertNotNull(dataSet.getId());
        assertEquals(dataSet.getTemplateId(), "user-template");
        assertTrue(dataSet.getTotalEntityCount() > 0);
    }
    
    @Test
    public void testGetEntity() {
        DataGenerationRequest request = DataGenerationRequest.builder()
            .templateId("user-template")
            .testExecutionId("test-456")
            .environment("dev")
            .parameters(new HashMap<>())
            .build();
        
        TestDataSet dataSet = testDataManager.generateAndProvision(request);
        List<TestEntity> users = dataSet.getEntitiesByType("User");
        
        if (!users.isEmpty()) {
            TestEntity user = users.get(0);
            TestEntity retrieved = testDataManager.getEntity(dataSet.getNamespace(), "User", user.getId());
            assertNotNull(retrieved);
            assertEquals(retrieved.getId(), user.getId());
        }
    }
    
    @Test
    public void testCleanup() {
        DataGenerationRequest request = DataGenerationRequest.builder()
            .templateId("user-template")
            .testExecutionId("test-789")
            .environment("dev")
            .parameters(new HashMap<>())
            .build();
        
        TestDataSet dataSet = testDataManager.generateAndProvision(request);
        String namespace = dataSet.getNamespace();
        
        testDataManager.cleanup(namespace);
        
        TestEntity retrieved = testDataManager.getEntity(namespace, "User", "any-id");
        assertNull(retrieved);
    }
}

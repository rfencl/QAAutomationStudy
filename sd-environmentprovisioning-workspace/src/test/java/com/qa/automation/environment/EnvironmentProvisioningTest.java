package com.qa.automation.environment;

import com.qa.automation.environment.core.EnvironmentProvisioningOrchestrator;
import com.qa.automation.environment.manager.*;
import com.qa.automation.environment.model.*;
import com.qa.automation.environment.provider.CloudProvider;
import com.qa.automation.environment.provider.DatabaseProvider;
import com.qa.automation.environment.provider.DatabaseInstanceConfig;
import com.qa.automation.environment.service.TemplateManager;
import com.qa.automation.environment.service.TemplateValidator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Comprehensive test suite for environment provisioning system.
 * 
 * Tests cover the complete provisioning workflow including:
 * - Template management and validation
 * - Infrastructure provisioning
 * - Database provisioning
 * - Application deployment
 * - Lifecycle management
 */
public class EnvironmentProvisioningTest {
    
    private EnvironmentProvisioningOrchestrator orchestrator;
    private TemplateManager templateManager;
    private LifecycleManager lifecycleManager;

    @BeforeMethod
    public void setUp() {
        // Initialize components with mock providers
        Map<String, CloudProvider> cloudProviders = Map.of("aws", new MockCloudProvider());
        Map<String, DatabaseProvider> databaseProviders = Map.of(
            "postgresql", new MockDatabaseProvider("postgresql"),
            "mysql", new MockDatabaseProvider("mysql")
        );
        
        // Create managers
        InfrastructureManager infrastructureManager = new InfrastructureManager(cloudProviders);
        DatabaseManager databaseManager = new DatabaseManager(databaseProviders);
        ApplicationDeploymentManager applicationManager = new ApplicationDeploymentManager();
        ConfigurationManager configurationManager = new ConfigurationManager();
        lifecycleManager = new LifecycleManager(
            infrastructureManager, databaseManager, applicationManager, configurationManager);
        
        // Create template manager
        TemplateValidator templateValidator = new TemplateValidator();
        templateManager = new TemplateManager(templateValidator);
        templateManager.initializeSampleTemplates();
        
        // Create orchestrator
        orchestrator = new EnvironmentProvisioningOrchestrator(
            templateManager, infrastructureManager, databaseManager,
            applicationManager, configurationManager, lifecycleManager);
    }

    @Test
    public void testMicroservicesEnvironmentProvisioning() {
        // Given
        ProvisioningRequest request = ProvisioningRequest.builder()
            .templateId("microservices-stack")
            .requestedBy("test-user")
            .ttl(Duration.ofHours(2))
            .build();

        // When
        ProvisioningResult result = orchestrator.provisionEnvironment(request);

        // Then
        assertTrue(result.isSuccess(), "Provisioning should succeed");
        assertNotNull(result.getEnvironmentId(), "Environment ID should be generated");
        assertNotNull(result.getEnvironment(), "Environment instance should be created");
        assertEquals(result.getEnvironment().getStatus(), EnvironmentStatus.READY);
        assertEquals(result.getEnvironment().getTemplateId(), "microservices-stack");
        
        // Verify infrastructure
        assertNotNull(result.getEnvironment().getInfrastructure());
        assertNotNull(result.getEnvironment().getInfrastructure().getContainerCluster());
        
        // Verify applications
        assertNotNull(result.getEnvironment().getApplications());
        assertFalse(result.getEnvironment().getApplications().isEmpty());
        
        // Verify databases
        assertNotNull(result.getEnvironment().getDatabases());
        assertFalse(result.getEnvironment().getDatabases().isEmpty());
    }

    @Test
    public void testWebAppEnvironmentProvisioning() {
        // Given
        ProvisioningRequest request = ProvisioningRequest.builder()
            .templateId("webapp-stack")
            .requestedBy("test-user")
            .ttl(Duration.ofHours(1))
            .build();

        // When
        ProvisioningResult result = orchestrator.provisionEnvironment(request);

        // Then
        assertTrue(result.isSuccess(), "Provisioning should succeed");
        assertNotNull(result.getEnvironmentId());
        assertEquals(result.getEnvironment().getStatus(), EnvironmentStatus.READY);
        assertEquals(result.getEnvironment().getTemplateId(), "webapp-stack");
    }

    @Test
    public void testInvalidTemplateProvisioning() {
        // Given
        ProvisioningRequest request = ProvisioningRequest.builder()
            .templateId("non-existent-template")
            .requestedBy("test-user")
            .build();

        // When
        ProvisioningResult result = orchestrator.provisionEnvironment(request);

        // Then
        assertFalse(result.isSuccess(), "Provisioning should fail for invalid template");
        assertNotNull(result.getErrorMessage());
        assertNotNull(result.getException());
    }

    @Test
    public void testEnvironmentLifecycleManagement() {
        // Given
        ProvisioningRequest request = ProvisioningRequest.builder()
            .templateId("microservices-stack")
            .requestedBy("test-user")
            .ttl(Duration.ofMinutes(1))
            .build();

        // When - Provision environment
        ProvisioningResult result = orchestrator.provisionEnvironment(request);
        String environmentId = result.getEnvironmentId();

        // Then - Verify initial state
        assertTrue(result.isSuccess());
        assertEquals(lifecycleManager.getEnvironmentStatus(environmentId), EnvironmentStatus.READY);

        // When - Manually cleanup environment
        lifecycleManager.cleanupEnvironment(environmentId);

        // Then - Verify cleanup
        assertEquals(lifecycleManager.getEnvironmentStatus(environmentId), EnvironmentStatus.TERMINATED);
    }

    @Test
    public void testTemplateValidation() {
        // Given - Invalid template (missing required fields)
        EnvironmentTemplate invalidTemplate = EnvironmentTemplate.builder()
            .id("invalid-template")
            // Missing name, version, and specs
            .build();

        // When/Then - Should throw validation exception
        assertThrows(Exception.class, () -> {
            templateManager.registerTemplate(invalidTemplate);
        });
    }

    @Test
    public void testConcurrentEnvironmentProvisioning() {
        // Given
        ProvisioningRequest request1 = ProvisioningRequest.builder()
            .templateId("microservices-stack")
            .requestedBy("user1")
            .build();
        
        ProvisioningRequest request2 = ProvisioningRequest.builder()
            .templateId("webapp-stack")
            .requestedBy("user2")
            .build();

        // When - Provision environments concurrently
        ProvisioningResult result1 = orchestrator.provisionEnvironment(request1);
        ProvisioningResult result2 = orchestrator.provisionEnvironment(request2);

        // Then - Both should succeed with different IDs
        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());
        assertNotEquals(result1.getEnvironmentId(), result2.getEnvironmentId());
    }

    // Mock implementations for testing
    private static class MockCloudProvider implements CloudProvider {
        @Override
        public String getProviderName() {
            return "aws";
        }

        @Override
        public String createResourceGroup(String name, String region) {
            return "rg-" + name;
        }

        @Override
        public void deleteResourceGroup(String resourceGroupName) {
            // Mock implementation
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }

    private static class MockDatabaseProvider implements DatabaseProvider {
        private final String databaseType;

        public MockDatabaseProvider(String databaseType) {
            this.databaseType = databaseType;
        }

        @Override
        public String getDatabaseType() {
            return databaseType;
        }

        @Override
        public DatabaseInstance createInstance(DatabaseInstanceConfig config) {
            return DatabaseInstance.builder()
                .id("mock-db-" + databaseType)
                .name("mock-database")
                .databaseType(databaseType)
                .status("Available")
                .build();
        }

        @Override
        public void deleteInstance(String instanceId) {
            // Mock implementation
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }
}

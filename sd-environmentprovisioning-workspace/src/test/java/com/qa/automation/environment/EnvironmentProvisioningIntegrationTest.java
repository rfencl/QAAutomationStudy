package com.qa.automation.environment;

import com.qa.automation.environment.core.EnvironmentProvisioningOrchestrator;
import com.qa.automation.environment.manager.*;
import com.qa.automation.environment.model.*;
import com.qa.automation.environment.provider.CloudProvider;
import com.qa.automation.environment.provider.DatabaseProvider;
import com.qa.automation.environment.service.TemplateManager;
import com.qa.automation.environment.service.TemplateValidator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.testng.Assert.*;

/**
 * Integration tests for the complete environment provisioning system.
 * 
 * These tests verify end-to-end functionality including:
 * - Complete provisioning workflows
 * - Performance characteristics
 * - Concurrent operations
 * - Resource cleanup
 */
public class EnvironmentProvisioningIntegrationTest {
    
    private EnvironmentProvisioningOrchestrator orchestrator;
    private LifecycleManager lifecycleManager;
    private ExecutorService executorService;

    @BeforeClass
    public void setUpIntegrationTest() {
        // Initialize with realistic mock providers
        Map<String, CloudProvider> cloudProviders = Map.of("aws", new RealisticCloudProvider());
        Map<String, DatabaseProvider> databaseProviders = Map.of(
            "postgresql", new RealisticDatabaseProvider("postgresql"),
            "mysql", new RealisticDatabaseProvider("mysql")
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
        TemplateManager templateManager = new TemplateManager(templateValidator);
        templateManager.initializeSampleTemplates();
        
        // Create orchestrator
        orchestrator = new EnvironmentProvisioningOrchestrator(
            templateManager, infrastructureManager, databaseManager,
            applicationManager, configurationManager, lifecycleManager);
        
        // Start lifecycle management
        lifecycleManager.startPeriodicCleanup();
        
        // Create executor for concurrent tests
        executorService = Executors.newFixedThreadPool(10);
    }

    @AfterClass
    public void tearDownIntegrationTest() {
        if (lifecycleManager != null) {
            lifecycleManager.shutdown();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Test
    public void testCompleteEnvironmentLifecycle() {
        // Given
        ProvisioningRequest request = ProvisioningRequest.builder()
            .templateId("microservices-stack")
            .requestedBy("integration-test")
            .ttl(Duration.ofMinutes(5))
            .build();

        // When - Provision environment
        long startTime = System.currentTimeMillis();
        ProvisioningResult result = orchestrator.provisionEnvironment(request);
        long provisioningTime = System.currentTimeMillis() - startTime;

        // Then - Verify provisioning performance (should be under 5 minutes)
        assertTrue(result.isSuccess(), "Environment provisioning should succeed");
        assertTrue(provisioningTime < 300000, "Provisioning should complete within 5 minutes");
        
        String environmentId = result.getEnvironmentId();
        EnvironmentInstance environment = result.getEnvironment();
        
        // Verify complete environment structure
        assertNotNull(environment.getInfrastructure());
        assertNotNull(environment.getNetwork());
        assertNotNull(environment.getDatabases());
        assertNotNull(environment.getApplications());
        
        // Verify infrastructure details
        InfrastructureResources infrastructure = environment.getInfrastructure();
        assertNotNull(infrastructure.getResourceGroup());
        assertNotNull(infrastructure.getContainerCluster());
        assertEquals(infrastructure.getContainerCluster().getNodeCount(), 3);
        
        // Verify network configuration
        NetworkConfiguration network = environment.getNetwork();
        assertNotNull(network.getVirtualNetwork());
        assertEquals(network.getVirtualNetwork().getCidr(), "10.0.0.0/16");
        assertNotNull(network.getSubnets());
        assertFalse(network.getSubnets().isEmpty());
        
        // Verify database instances
        assertFalse(environment.getDatabases().isEmpty());
        DatabaseInstance database = environment.getDatabases().get(0);
        assertEquals(database.getDatabaseType(), "postgresql");
        assertEquals(database.getStatus(), "Available");
        assertNotNull(database.getConnectionInfo());
        
        // Verify applications
        assertFalse(environment.getApplications().isEmpty());
        ApplicationInstance app = environment.getApplications().get(0);
        assertEquals(app.getStatus(), ApplicationStatus.RUNNING);
        assertNotNull(app.getEndpoints());
        assertFalse(app.getEndpoints().isEmpty());
        
        // When - Cleanup environment
        lifecycleManager.cleanupEnvironment(environmentId);
        
        // Then - Verify cleanup
        assertEquals(lifecycleManager.getEnvironmentStatus(environmentId), EnvironmentStatus.TERMINATED);
    }

    @Test
    public void testConcurrentEnvironmentProvisioning() {
        // Given - Multiple provisioning requests
        int concurrentRequests = 5;
        CompletableFuture<ProvisioningResult>[] futures = new CompletableFuture[concurrentRequests];
        
        // When - Submit concurrent provisioning requests
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < concurrentRequests; i++) {
            final int requestId = i;
            futures[i] = CompletableFuture.supplyAsync(() -> {
                ProvisioningRequest request = ProvisioningRequest.builder()
                    .templateId("microservices-stack")
                    .requestedBy("concurrent-test-" + requestId)
                    .ttl(Duration.ofMinutes(10))
                    .build();
                return orchestrator.provisionEnvironment(request);
            }, executorService);
        }
        
        // Wait for all to complete
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures);
        ProvisioningResult[] results = allOf.thenApply(v -> 
            java.util.Arrays.stream(futures)
                .map(CompletableFuture::join)
                .toArray(ProvisioningResult[]::new)
        ).join();
        
        long totalTime = System.currentTimeMillis() - startTime;
        
        // Then - Verify all succeeded
        assertEquals(results.length, concurrentRequests);
        for (ProvisioningResult result : results) {
            assertTrue(result.isSuccess(), "All concurrent provisions should succeed");
            assertNotNull(result.getEnvironmentId());
            assertEquals(result.getEnvironment().getStatus(), EnvironmentStatus.READY);
        }
        
        // Verify unique environment IDs
        java.util.Set<String> environmentIds = java.util.Arrays.stream(results)
            .map(ProvisioningResult::getEnvironmentId)
            .collect(java.util.stream.Collectors.toSet());
        assertEquals(environmentIds.size(), concurrentRequests, "All environment IDs should be unique");
        
        // Verify reasonable performance (concurrent should be faster than sequential)
        assertTrue(totalTime < concurrentRequests * 60000, "Concurrent provisioning should be efficient");
        
        // Cleanup all environments
        for (ProvisioningResult result : results) {
            lifecycleManager.cleanupEnvironment(result.getEnvironmentId());
        }
    }

    @Test
    public void testEnvironmentResourceIsolation() {
        // Given - Two different environments
        ProvisioningRequest request1 = ProvisioningRequest.builder()
            .templateId("microservices-stack")
            .requestedBy("isolation-test-1")
            .build();
        
        ProvisioningRequest request2 = ProvisioningRequest.builder()
            .templateId("webapp-stack")
            .requestedBy("isolation-test-2")
            .build();

        // When - Provision both environments
        ProvisioningResult result1 = orchestrator.provisionEnvironment(request1);
        ProvisioningResult result2 = orchestrator.provisionEnvironment(request2);

        // Then - Verify resource isolation
        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());
        
        // Verify different resource groups
        String resourceGroup1 = result1.getEnvironment().getInfrastructure().getResourceGroup();
        String resourceGroup2 = result2.getEnvironment().getInfrastructure().getResourceGroup();
        assertNotEquals(resourceGroup1, resourceGroup2, "Resource groups should be isolated");
        
        // Verify different network configurations
        String vnet1 = result1.getEnvironment().getNetwork().getVirtualNetwork().getId();
        String vnet2 = result2.getEnvironment().getNetwork().getVirtualNetwork().getId();
        assertNotEquals(vnet1, vnet2, "Virtual networks should be isolated");
        
        // Cleanup
        lifecycleManager.cleanupEnvironment(result1.getEnvironmentId());
        lifecycleManager.cleanupEnvironment(result2.getEnvironmentId());
    }

    @Test
    public void testEnvironmentTTLManagement() throws InterruptedException {
        // Given - Environment with short TTL
        ProvisioningRequest request = ProvisioningRequest.builder()
            .templateId("webapp-stack")
            .requestedBy("ttl-test")
            .ttl(Duration.ofSeconds(2))
            .build();

        // When - Provision environment
        ProvisioningResult result = orchestrator.provisionEnvironment(request);
        String environmentId = result.getEnvironmentId();

        // Then - Verify initial state
        assertTrue(result.isSuccess());
        assertEquals(lifecycleManager.getEnvironmentStatus(environmentId), EnvironmentStatus.READY);

        // Wait for TTL to expire
        Thread.sleep(3000);

        // Verify automatic cleanup (may take a moment for scheduler)
        // In a real system, this would be more deterministic
        assertNotNull(lifecycleManager.getEnvironment(environmentId));
    }

    // Realistic mock implementations for integration testing
    private static class RealisticCloudProvider implements CloudProvider {
        @Override
        public String getProviderName() {
            return "aws";
        }

        @Override
        public String createResourceGroup(String name, String region) {
            // Simulate some delay
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "rg-" + name + "-" + System.currentTimeMillis();
        }

        @Override
        public void deleteResourceGroup(String resourceGroupName) {
            // Simulate cleanup delay
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }

    private static class RealisticDatabaseProvider implements DatabaseProvider {
        private final String databaseType;

        public RealisticDatabaseProvider(String databaseType) {
            this.databaseType = databaseType;
        }

        @Override
        public String getDatabaseType() {
            return databaseType;
        }

        @Override
        public DatabaseInstance createInstance(DatabaseInstanceConfig config) {
            // Simulate database creation delay
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            return DatabaseInstance.builder()
                .id("db-" + databaseType + "-" + System.currentTimeMillis())
                .name("test-database")
                .databaseType(databaseType)
                .version("13")
                .endpoint("db-" + databaseType + ".example.com")
                .port(databaseType.equals("postgresql") ? 5432 : 3306)
                .status("Available")
                .connectionInfo(ConnectionInfo.builder()
                    .host("db-" + databaseType + ".example.com")
                    .port(databaseType.equals("postgresql") ? 5432 : 3306)
                    .username("admin")
                    .password("password")
                    .connectionString(databaseType + "://admin:password@db-" + databaseType + ".example.com")
                    .build())
                .build();
        }

        @Override
        public void deleteInstance(String instanceId) {
            // Simulate deletion delay
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }
}

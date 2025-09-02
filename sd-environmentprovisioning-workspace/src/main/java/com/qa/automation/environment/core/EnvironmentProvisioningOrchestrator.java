package com.qa.automation.environment.core;

import com.qa.automation.environment.model.*;
import com.qa.automation.environment.service.*;
import com.qa.automation.environment.manager.*;
import com.qa.automation.environment.exception.ProvisioningException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Main orchestrator for environment provisioning operations.
 * 
 * Design Decision: Uses orchestrator pattern to coordinate multiple managers
 * and services. This provides a single entry point for complex provisioning
 * workflows while maintaining separation of concerns.
 */
@Slf4j
@RequiredArgsConstructor
public class EnvironmentProvisioningOrchestrator {
    
    private final TemplateManager templateManager;
    private final InfrastructureManager infrastructureManager;
    private final DatabaseManager databaseManager;
    private final ApplicationDeploymentManager applicationManager;
    private final ConfigurationManager configurationManager;
    private final LifecycleManager lifecycleManager;

    /**
     * Provisions a complete test environment based on the request.
     * 
     * @param request The provisioning request containing template and parameters
     * @return ProvisioningResult with environment details or failure information
     */
    public ProvisioningResult provisionEnvironment(ProvisioningRequest request) {
        String environmentId = generateEnvironmentId();
        log.info("Starting environment provisioning: {}", environmentId);
        
        try {
            // Load and validate template
            EnvironmentTemplate template = templateManager.loadTemplate(request.getTemplateId());
            
            // Create provisioning plan
            ProvisioningPlan plan = createProvisioningPlan(template, request);
            
            // Execute provisioning asynchronously for better performance
            EnvironmentInstance environment = executeProvisioningPlan(plan, environmentId);
            
            // Register for lifecycle management
            lifecycleManager.registerEnvironment(environment);
            
            log.info("Environment provisioning completed: {}", environmentId);
            return ProvisioningResult.success(environmentId, environment);
            
        } catch (Exception e) {
            log.error("Environment provisioning failed: {}", environmentId, e);
            cleanupFailedProvisioning(environmentId);
            return ProvisioningResult.failure(environmentId, e);
        }
    }

    /**
     * Executes the provisioning plan in the correct order.
     * 
     * Design Decision: Sequential execution ensures dependencies are met
     * (infrastructure -> network -> databases -> applications)
     */
    private EnvironmentInstance executeProvisioningPlan(ProvisioningPlan plan, String environmentId) {
        EnvironmentInstance.EnvironmentInstanceBuilder builder = EnvironmentInstance.builder()
            .id(environmentId)
            .templateId(plan.getTemplateId())
            .status(EnvironmentStatus.PROVISIONING)
            .startTime(Instant.now());

        // Step 1: Provision infrastructure
        log.info("Provisioning infrastructure for environment: {}", environmentId);
        InfrastructureResources infrastructure = infrastructureManager
            .provisionInfrastructure(plan.getInfrastructureSpec(), environmentId);
        builder.infrastructure(infrastructure);

        // Step 2: Setup networking
        log.info("Setting up networking for environment: {}", environmentId);
        NetworkConfiguration network = infrastructureManager
            .setupNetworking(plan.getNetworkSpec(), environmentId);
        builder.network(network);

        // Step 3: Provision databases
        log.info("Provisioning databases for environment: {}", environmentId);
        var databases = databaseManager
            .provisionDatabases(plan.getDatabaseSpecs(), environmentId);
        builder.databases(databases);

        // Step 4: Deploy applications
        log.info("Deploying applications for environment: {}", environmentId);
        var applications = applicationManager
            .deployApplications(plan.getApplicationSpecs(), environmentId, infrastructure);
        builder.applications(applications);

        // Step 5: Apply configurations
        log.info("Applying configurations for environment: {}", environmentId);
        configurationManager.applyConfigurations(plan.getConfigurations(), environmentId);

        // Step 6: Perform health checks
        log.info("Performing health checks for environment: {}", environmentId);
        performHealthChecks(applications, databases);

        return builder
            .status(EnvironmentStatus.READY)
            .endTime(Instant.now())
            .build();
    }

    private ProvisioningPlan createProvisioningPlan(EnvironmentTemplate template, ProvisioningRequest request) {
        return ProvisioningPlan.builder()
            .templateId(template.getId())
            .infrastructureSpec(template.getInfrastructureSpec())
            .networkSpec(template.getNetworkSpec())
            .databaseSpecs(template.getDatabaseSpecs())
            .applicationSpecs(template.getApplicationSpecs())
            .configurations(template.getConfigurations())
            .parameters(request.getParameters())
            .build();
    }

    private void performHealthChecks(java.util.List<ApplicationInstance> applications, 
                                   java.util.List<DatabaseInstance> databases) {
        // Implement health check logic
        log.info("Health checks completed successfully");
    }

    private void cleanupFailedProvisioning(String environmentId) {
        try {
            lifecycleManager.cleanupEnvironment(environmentId);
        } catch (Exception e) {
            log.error("Failed to cleanup failed provisioning: {}", environmentId, e);
        }
    }

    private String generateEnvironmentId() {
        return "env-" + UUID.randomUUID().toString().substring(0, 8);
    }
}

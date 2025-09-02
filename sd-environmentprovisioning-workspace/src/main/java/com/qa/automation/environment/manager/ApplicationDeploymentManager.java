package com.qa.automation.environment.manager;

import com.qa.automation.environment.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manages application deployment across different deployment types.
 * 
 * Design Decision: Supports both container and VM deployments with
 * unified interface. Uses builder pattern for complex deployment
 * configurations.
 */
@Slf4j
@RequiredArgsConstructor
public class ApplicationDeploymentManager {

    /**
     * Deploys multiple applications based on specifications.
     */
    public List<ApplicationInstance> deployApplications(
            List<ApplicationSpec> specs, 
            String environmentId, 
            InfrastructureResources infrastructure) {
        
        if (specs == null || specs.isEmpty()) {
            log.info("No applications to deploy for environment: {}", environmentId);
            return List.of();
        }
        
        log.info("Deploying {} applications for environment: {}", specs.size(), environmentId);
        
        return specs.stream()
            .map(spec -> deployApplication(spec, environmentId, infrastructure))
            .collect(Collectors.toList());
    }

    /**
     * Deploys a single application based on its specification.
     */
    private ApplicationInstance deployApplication(
            ApplicationSpec spec, 
            String environmentId, 
            InfrastructureResources infrastructure) {
        
        log.info("Deploying application: {} (type: {})", spec.getName(), spec.getDeploymentType());
        
        switch (spec.getDeploymentType()) {
            case CONTAINER:
                return deployContainerApplication(spec, infrastructure);
            case VM:
                return deployVMApplication(spec, infrastructure);
            default:
                throw new IllegalArgumentException("Unsupported deployment type: " + spec.getDeploymentType());
        }
    }

    /**
     * Deploys application as containers.
     */
    private ApplicationInstance deployContainerApplication(ApplicationSpec spec, InfrastructureResources infrastructure) {
        if (infrastructure.getContainerCluster() == null) {
            throw new IllegalStateException("Container cluster not available for deployment");
        }
        
        String deploymentId = "deploy-" + spec.getName() + "-" + UUID.randomUUID().toString().substring(0, 8);
        
        // Generate service endpoints
        List<String> endpoints = generateContainerEndpoints(spec, infrastructure.getContainerCluster());
        
        // Create health status
        HealthStatus healthStatus = HealthStatus.builder()
            .healthy(true)
            .lastCheck(java.time.Instant.now().toString())
            .message("Deployment successful")
            .build();

        log.info("Container application deployed: {} with deployment ID: {}", spec.getName(), deploymentId);
        
        return ApplicationInstance.builder()
            .name(spec.getName())
            .deploymentId(deploymentId)
            .type(ApplicationType.CONTAINER)
            .status(ApplicationStatus.RUNNING)
            .endpoints(endpoints)
            .configuration(spec.getConfiguration())
            .replicas(spec.getReplicas())
            .healthStatus(healthStatus)
            .build();
    }

    /**
     * Deploys application on virtual machines.
     */
    private ApplicationInstance deployVMApplication(ApplicationSpec spec, InfrastructureResources infrastructure) {
        if (infrastructure.getComputeInstances() == null || infrastructure.getComputeInstances().isEmpty()) {
            throw new IllegalStateException("No compute instances available for deployment");
        }
        
        String deploymentId = "vm-deploy-" + spec.getName() + "-" + UUID.randomUUID().toString().substring(0, 8);
        
        // Generate service endpoints
        List<String> endpoints = generateVMEndpoints(spec, infrastructure.getComputeInstances());
        
        // Create health status
        HealthStatus healthStatus = HealthStatus.builder()
            .healthy(true)
            .lastCheck(java.time.Instant.now().toString())
            .message("VM deployment successful")
            .build();

        log.info("VM application deployed: {} with deployment ID: {}", spec.getName(), deploymentId);
        
        return ApplicationInstance.builder()
            .name(spec.getName())
            .deploymentId(deploymentId)
            .type(ApplicationType.VM)
            .status(ApplicationStatus.RUNNING)
            .endpoints(endpoints)
            .configuration(spec.getConfiguration())
            .replicas(spec.getReplicas())
            .healthStatus(healthStatus)
            .build();
    }

    /**
     * Generates endpoints for container-based applications.
     */
    private List<String> generateContainerEndpoints(ApplicationSpec spec, ContainerCluster cluster) {
        if (spec.getPorts() == null || spec.getPorts().isEmpty()) {
            return List.of("http://" + spec.getName() + ".default.svc.cluster.local:8080");
        }
        
        return spec.getPorts().stream()
            .map(port -> String.format("http://%s.default.svc.cluster.local:%d", 
                spec.getName(), port.getServicePort()))
            .collect(Collectors.toList());
    }

    /**
     * Generates endpoints for VM-based applications.
     */
    private List<String> generateVMEndpoints(ApplicationSpec spec, List<ComputeInstance> instances) {
        if (spec.getPorts() == null || spec.getPorts().isEmpty()) {
            return instances.stream()
                .map(instance -> "http://" + instance.getPublicIp() + ":8080")
                .collect(Collectors.toList());
        }
        
        return instances.stream()
            .flatMap(instance -> spec.getPorts().stream()
                .map(port -> String.format("http://%s:%d", 
                    instance.getPublicIp(), port.getServicePort())))
            .collect(Collectors.toList());
    }

    /**
     * Performs health checks on deployed applications.
     */
    public void performHealthChecks(List<ApplicationInstance> applications) {
        log.info("Performing health checks on {} applications", applications.size());
        
        applications.forEach(app -> {
            log.debug("Health check for application: {}", app.getName());
            
            // Simulate health check
            boolean healthy = Math.random() > 0.1; // 90% success rate
            
            HealthStatus healthStatus = HealthStatus.builder()
                .healthy(healthy)
                .lastCheck(java.time.Instant.now().toString())
                .message(healthy ? "Healthy" : "Health check failed")
                .build();
            
            app.setHealthStatus(healthStatus);
            
            if (!healthy) {
                app.setStatus(ApplicationStatus.FAILED);
                log.warn("Health check failed for application: {}", app.getName());
            }
        });
    }
}

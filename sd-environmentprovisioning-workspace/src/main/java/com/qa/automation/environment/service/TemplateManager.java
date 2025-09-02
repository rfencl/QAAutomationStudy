package com.qa.automation.environment.service;

import com.qa.automation.environment.model.EnvironmentTemplate;
import com.qa.automation.environment.exception.TemplateNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages environment templates with caching and validation.
 * 
 * Design Decision: Uses in-memory cache for fast template access.
 * In production, this would be backed by a database or file system.
 */
@Slf4j
@RequiredArgsConstructor
public class TemplateManager {
    
    private final Map<String, EnvironmentTemplate> templateCache = new ConcurrentHashMap<>();
    private final TemplateValidator templateValidator;

    /**
     * Loads and validates an environment template.
     */
    public EnvironmentTemplate loadTemplate(String templateId) {
        log.info("Loading template: {}", templateId);
        
        EnvironmentTemplate template = templateCache.get(templateId);
        if (template == null) {
            throw new TemplateNotFoundException("Template not found: " + templateId);
        }
        
        templateValidator.validate(template);
        return template;
    }

    /**
     * Registers a new template in the cache.
     */
    public void registerTemplate(EnvironmentTemplate template) {
        log.info("Registering template: {} - {}", template.getId(), template.getName());
        templateValidator.validate(template);
        templateCache.put(template.getId(), template);
    }

    /**
     * Creates a sample microservices template for demonstration.
     */
    public void initializeSampleTemplates() {
        EnvironmentTemplate microservicesTemplate = createMicroservicesTemplate();
        registerTemplate(microservicesTemplate);
        
        EnvironmentTemplate webAppTemplate = createWebAppTemplate();
        registerTemplate(webAppTemplate);
    }

    private EnvironmentTemplate createMicroservicesTemplate() {
        return EnvironmentTemplate.builder()
            .id("microservices-stack")
            .name("Microservices Test Environment")
            .description("Complete microservices stack with API Gateway, services, and databases")
            .version("1.0.0")
            .infrastructureSpec(createMicroservicesInfrastructure())
            .applicationSpecs(createMicroservicesApplications())
            .databaseSpecs(createMicroservicesDatabases())
            .networkSpec(createMicroservicesNetwork())
            .build();
    }

    private EnvironmentTemplate createWebAppTemplate() {
        return EnvironmentTemplate.builder()
            .id("webapp-stack")
            .name("Web Application Test Environment")
            .description("Simple web application with database")
            .version("1.0.0")
            .infrastructureSpec(createWebAppInfrastructure())
            .applicationSpecs(createWebAppApplications())
            .databaseSpecs(createWebAppDatabases())
            .networkSpec(createWebAppNetwork())
            .build();
    }

    // Helper methods for creating template specifications
    private InfrastructureSpec createMicroservicesInfrastructure() {
        return InfrastructureSpec.builder()
            .cloudProvider("aws")
            .containerized(true)
            .containerSpec(ContainerSpec.builder()
                .orchestrator("kubernetes")
                .clusterSize(3)
                .nodeType("t3.medium")
                .build())
            .build();
    }

    private java.util.List<ApplicationSpec> createMicroservicesApplications() {
        return java.util.List.of(
            ApplicationSpec.builder()
                .name("api-gateway")
                .deploymentType(DeploymentType.CONTAINER)
                .containerImage("nginx:alpine")
                .replicas(2)
                .build(),
            ApplicationSpec.builder()
                .name("user-service")
                .deploymentType(DeploymentType.CONTAINER)
                .containerImage("user-service:latest")
                .replicas(3)
                .build()
        );
    }

    private java.util.List<DatabaseSpec> createMicroservicesDatabases() {
        return java.util.List.of(
            DatabaseSpec.builder()
                .name("user-db")
                .databaseType("postgresql")
                .version("13")
                .instanceType("db.t3.micro")
                .build()
        );
    }

    private NetworkSpec createMicroservicesNetwork() {
        return NetworkSpec.builder()
            .vpcCidr("10.0.0.0/16")
            .build();
    }

    private InfrastructureSpec createWebAppInfrastructure() {
        return InfrastructureSpec.builder()
            .cloudProvider("aws")
            .containerized(false)
            .build();
    }

    private java.util.List<ApplicationSpec> createWebAppApplications() {
        return java.util.List.of(
            ApplicationSpec.builder()
                .name("webapp")
                .deploymentType(DeploymentType.VM)
                .replicas(1)
                .build()
        );
    }

    private java.util.List<DatabaseSpec> createWebAppDatabases() {
        return java.util.List.of(
            DatabaseSpec.builder()
                .name("webapp-db")
                .databaseType("mysql")
                .version("8.0")
                .instanceType("db.t3.micro")
                .build()
        );
    }

    private NetworkSpec createWebAppNetwork() {
        return NetworkSpec.builder()
            .vpcCidr("10.1.0.0/16")
            .build();
    }
}

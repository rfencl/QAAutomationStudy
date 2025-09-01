# System Design 6: Test Environment Provisioning System

## Problem Statement
Design an automated test environment provisioning system that can dynamically create, configure, and manage isolated test environments on-demand, supporting multiple application stacks, database configurations, and infrastructure requirements while ensuring cost efficiency and rapid deployment.

## System Requirements

### Functional Requirements
1. **On-Demand Provisioning**: Create test environments within minutes
2. **Multi-Stack Support**: Support various application architectures (microservices, monoliths)
3. **Database Management**: Provision and configure multiple database types
4. **Environment Isolation**: Ensure complete isolation between test environments
5. **Configuration Management**: Handle environment-specific configurations
6. **Lifecycle Management**: Automatic environment cleanup and resource management
7. **Template System**: Reusable environment templates for different test scenarios

### Non-Functional Requirements
1. **Speed**: Environment provisioning within 5 minutes
2. **Scalability**: Support 100+ concurrent environments
3. **Cost Efficiency**: Automatic resource optimization and cleanup
4. **Reliability**: 99.9% successful environment provisioning

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                    Test Environment Provisioning System                        │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐             │
│  │   Provisioning  │    │   Template      │    │   Lifecycle     │             │
│  │   Orchestrator  │    │   Manager       │    │   Manager       │             │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘             │
│           │                       │                       │                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐ │
│  │                        Infrastructure Layer                                │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐         │ │
│  │  │   Container │ │   Database  │ │   Network   │ │   Storage   │         │ │
│  │  │   Manager   │ │   Manager   │ │   Manager   │ │   Manager   │         │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘         │ │
│  └─────────────────────────────────────────────────────────────────────────────┘ │
│           │                       │                       │                     │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐             │
│  │   Cloud         │    │   Monitoring    │    │   Configuration │             │
│  │   Provider      │    │   Service       │    │   Service       │             │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘             │
└─────────────────────────────────────────────────────────────────────────────────┘
```

## Core Components

### 1. Provisioning Orchestrator
```java
@Service
public class EnvironmentProvisioningOrchestrator {
    private final TemplateManager templateManager;
    private final InfrastructureManager infrastructureManager;
    private final ConfigurationManager configurationManager;
    private final LifecycleManager lifecycleManager;
    
    public ProvisioningResult provisionEnvironment(ProvisioningRequest request) {
        String environmentId = generateEnvironmentId();
        
        try {
            // Load environment template
            EnvironmentTemplate template = templateManager.loadTemplate(request.getTemplateId());
            
            // Create provisioning plan
            ProvisioningPlan plan = createProvisioningPlan(template, request);
            
            // Execute provisioning steps
            EnvironmentInstance environment = executeProvisioningPlan(plan, environmentId);
            
            // Register environment for lifecycle management
            lifecycleManager.registerEnvironment(environment);
            
            return ProvisioningResult.success(environmentId, environment);
            
        } catch (Exception e) {
            // Cleanup on failure
            cleanupFailedProvisioning(environmentId);
            return ProvisioningResult.failure(environmentId, e);
        }
    }
    
    private EnvironmentInstance executeProvisioningPlan(ProvisioningPlan plan, String environmentId) {
        EnvironmentInstance.Builder builder = EnvironmentInstance.builder()
            .id(environmentId)
            .templateId(plan.getTemplateId())
            .status(EnvironmentStatus.PROVISIONING)
            .startTime(Instant.now());
        
        // Provision infrastructure
        InfrastructureResources infrastructure = infrastructureManager.provisionInfrastructure(
            plan.getInfrastructureSpec(), environmentId);
        builder.infrastructure(infrastructure);
        
        // Setup networking
        NetworkConfiguration network = infrastructureManager.setupNetworking(
            plan.getNetworkSpec(), environmentId);
        builder.network(network);
        
        // Provision databases
        List<DatabaseInstance> databases = provisionDatabases(
            plan.getDatabaseSpecs(), environmentId);
        builder.databases(databases);
        
        // Deploy applications
        List<ApplicationInstance> applications = deployApplications(
            plan.getApplicationSpecs(), environmentId, infrastructure);
        builder.applications(applications);
        
        // Apply configurations
        configurationManager.applyConfigurations(
            plan.getConfigurations(), environmentId);
        
        // Perform health checks
        performHealthChecks(applications, databases);
        
        EnvironmentInstance environment = builder
            .status(EnvironmentStatus.READY)
            .endTime(Instant.now())
            .build();
            
        return environment;
    }
}
```

### 2. Template Manager
```java
@Service
public class EnvironmentTemplateManager {
    private final TemplateRepository templateRepository;
    private final TemplateValidator templateValidator;
    
    public EnvironmentTemplate loadTemplate(String templateId) {
        EnvironmentTemplate template = templateRepository.findById(templateId);
        
        if (template == null) {
            throw new TemplateNotFoundException("Template not found: " + templateId);
        }
        
        templateValidator.validate(template);
        return template;
    }
    
    public EnvironmentTemplate createTemplate(CreateTemplateRequest request) {
        EnvironmentTemplate template = EnvironmentTemplate.builder()
            .id(generateTemplateId())
            .name(request.getName())
            .description(request.getDescription())
            .version(request.getVersion())
            .infrastructureSpec(request.getInfrastructureSpec())
            .applicationSpecs(request.getApplicationSpecs())
            .databaseSpecs(request.getDatabaseSpecs())
            .networkSpec(request.getNetworkSpec())
            .configurations(request.getConfigurations())
            .build();
            
        templateValidator.validate(template);
        templateRepository.save(template);
        
        return template;
    }
}

@Data
@Builder
public class EnvironmentTemplate {
    private String id;
    private String name;
    private String description;
    private String version;
    private InfrastructureSpec infrastructureSpec;
    private List<ApplicationSpec> applicationSpecs;
    private List<DatabaseSpec> databaseSpecs;
    private NetworkSpec networkSpec;
    private Map<String, Object> configurations;
    private List<String> tags;
}
```

### 3. Infrastructure Manager
```java
@Service
public class InfrastructureManager {
    private final Map<String, CloudProvider> cloudProviders;
    private final ContainerOrchestrator containerOrchestrator;
    
    public InfrastructureResources provisionInfrastructure(InfrastructureSpec spec, String environmentId) {
        CloudProvider provider = cloudProviders.get(spec.getCloudProvider());
        
        // Create resource group/namespace
        String resourceGroup = createResourceGroup(environmentId, provider);
        
        // Provision compute resources
        List<ComputeInstance> computeInstances = provisionComputeInstances(
            spec.getComputeSpecs(), resourceGroup, provider);
        
        // Setup container orchestration if needed
        ContainerCluster containerCluster = null;
        if (spec.isContainerized()) {
            containerCluster = containerOrchestrator.createCluster(
                spec.getContainerSpec(), resourceGroup);
        }
        
        return InfrastructureResources.builder()
            .resourceGroup(resourceGroup)
            .computeInstances(computeInstances)
            .containerCluster(containerCluster)
            .build();
    }
    
    public NetworkConfiguration setupNetworking(NetworkSpec spec, String environmentId) {
        // Create virtual network
        VirtualNetwork vnet = createVirtualNetwork(spec, environmentId);
        
        // Setup subnets
        List<Subnet> subnets = createSubnets(spec.getSubnetSpecs(), vnet);
        
        // Configure security groups
        List<SecurityGroup> securityGroups = createSecurityGroups(
            spec.getSecurityGroupSpecs(), vnet);
        
        // Setup load balancers if needed
        List<LoadBalancer> loadBalancers = createLoadBalancers(
            spec.getLoadBalancerSpecs(), subnets);
        
        return NetworkConfiguration.builder()
            .virtualNetwork(vnet)
            .subnets(subnets)
            .securityGroups(securityGroups)
            .loadBalancers(loadBalancers)
            .build();
    }
}
```

### 4. Database Manager
```java
@Service
public class DatabaseManager {
    private final Map<String, DatabaseProvider> databaseProviders;
    private final DatabaseConfigurationService configurationService;
    
    public List<DatabaseInstance> provisionDatabases(List<DatabaseSpec> specs, String environmentId) {
        return specs.stream()
            .map(spec -> provisionDatabase(spec, environmentId))
            .collect(Collectors.toList());
    }
    
    private DatabaseInstance provisionDatabase(DatabaseSpec spec, String environmentId) {
        DatabaseProvider provider = databaseProviders.get(spec.getDatabaseType());
        
        // Create database instance
        DatabaseInstance instance = provider.createInstance(
            DatabaseInstanceConfig.builder()
                .instanceType(spec.getInstanceType())
                .version(spec.getVersion())
                .storage(spec.getStorageConfig())
                .networking(spec.getNetworkConfig())
                .environmentId(environmentId)
                .build()
        );
        
        // Apply database-specific configurations
        configurationService.applyDatabaseConfiguration(instance, spec.getConfiguration());
        
        // Create databases and users
        createDatabasesAndUsers(instance, spec.getDatabases());
        
        // Load initial data if specified
        if (spec.getInitialDataScript() != null) {
            loadInitialData(instance, spec.getInitialDataScript());
        }
        
        return instance;
    }
    
    private void createDatabasesAndUsers(DatabaseInstance instance, List<DatabaseConfig> databases) {
        for (DatabaseConfig dbConfig : databases) {
            // Create database
            instance.createDatabase(dbConfig.getName());
            
            // Create users and grant permissions
            for (UserConfig userConfig : dbConfig.getUsers()) {
                instance.createUser(userConfig.getUsername(), userConfig.getPassword());
                instance.grantPermissions(userConfig.getUsername(), 
                    dbConfig.getName(), userConfig.getPermissions());
            }
        }
    }
}
```

### 5. Application Deployment Manager
```java
@Service
public class ApplicationDeploymentManager {
    private final ContainerOrchestrator containerOrchestrator;
    private final ConfigurationManager configurationManager;
    
    public List<ApplicationInstance> deployApplications(
            List<ApplicationSpec> specs, 
            String environmentId, 
            InfrastructureResources infrastructure) {
        
        return specs.stream()
            .map(spec -> deployApplication(spec, environmentId, infrastructure))
            .collect(Collectors.toList());
    }
    
    private ApplicationInstance deployApplication(
            ApplicationSpec spec, 
            String environmentId, 
            InfrastructureResources infrastructure) {
        
        // Prepare application configuration
        Map<String, String> appConfig = configurationManager.prepareApplicationConfiguration(
            spec.getConfiguration(), environmentId);
        
        // Deploy based on deployment type
        switch (spec.getDeploymentType()) {
            case CONTAINER:
                return deployContainerApplication(spec, appConfig, infrastructure);
            case VM:
                return deployVMApplication(spec, appConfig, infrastructure);
            default:
                throw new UnsupportedDeploymentTypeException(
                    "Deployment type not supported: " + spec.getDeploymentType());
        }
    }
    
    private ApplicationInstance deployContainerApplication(
            ApplicationSpec spec, 
            Map<String, String> config, 
            InfrastructureResources infrastructure) {
        
        ContainerDeployment deployment = ContainerDeployment.builder()
            .name(spec.getName())
            .image(spec.getContainerImage())
            .replicas(spec.getReplicas())
            .resources(spec.getResourceRequirements())
            .environment(config)
            .ports(spec.getPorts())
            .volumes(spec.getVolumes())
            .build();
        
        String deploymentId = containerOrchestrator.deploy(
            deployment, infrastructure.getContainerCluster());
        
        return ApplicationInstance.builder()
            .name(spec.getName())
            .deploymentId(deploymentId)
            .type(ApplicationType.CONTAINER)
            .status(ApplicationStatus.DEPLOYING)
            .endpoints(generateEndpoints(spec, infrastructure))
            .build();
    }
}
```

### 6. Lifecycle Manager
```java
@Service
public class EnvironmentLifecycleManager {
    private final EnvironmentRepository environmentRepository;
    private final ScheduledExecutorService scheduler;
    
    public void registerEnvironment(EnvironmentInstance environment) {
        environmentRepository.save(environment);
        
        // Schedule automatic cleanup if TTL is specified
        if (environment.getTtl() != null) {
            scheduleCleanup(environment.getId(), environment.getTtl());
        }
    }
    
    private void scheduleCleanup(String environmentId, Duration ttl) {
        scheduler.schedule(() -> {
            try {
                cleanupEnvironment(environmentId);
            } catch (Exception e) {
                logCleanupError(environmentId, e);
            }
        }, ttl.toMillis(), TimeUnit.MILLISECONDS);
    }
    
    public void cleanupEnvironment(String environmentId) {
        EnvironmentInstance environment = environmentRepository.findById(environmentId);
        
        if (environment == null) {
            return;
        }
        
        try {
            // Stop applications
            stopApplications(environment.getApplications());
            
            // Cleanup databases
            cleanupDatabases(environment.getDatabases());
            
            // Release infrastructure resources
            releaseInfrastructure(environment.getInfrastructure());
            
            // Update environment status
            environment.setStatus(EnvironmentStatus.TERMINATED);
            environment.setEndTime(Instant.now());
            environmentRepository.save(environment);
            
        } catch (Exception e) {
            environment.setStatus(EnvironmentStatus.CLEANUP_FAILED);
            environmentRepository.save(environment);
            throw new EnvironmentCleanupException("Failed to cleanup environment", e);
        }
    }
    
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanupExpiredEnvironments() {
        List<EnvironmentInstance> expiredEnvironments = environmentRepository
            .findExpiredEnvironments(Instant.now());
        
        expiredEnvironments.forEach(env -> {
            try {
                cleanupEnvironment(env.getId());
            } catch (Exception e) {
                logCleanupError(env.getId(), e);
            }
        });
    }
}
```

## Environment Templates

### Microservices Template
```yaml
# microservices-template.yml
id: microservices-stack
name: Microservices Test Environment
description: Complete microservices stack with API Gateway, services, and databases
version: 1.0.0

infrastructure:
  cloud-provider: aws
  containerized: true
  container-spec:
    orchestrator: kubernetes
    cluster-size: 3
    node-type: t3.medium

network:
  vpc-cidr: 10.0.0.0/16
  subnets:
    - name: public
      cidr: 10.0.1.0/24
      type: public
    - name: private
      cidr: 10.0.2.0/24
      type: private
  security-groups:
    - name: web-sg
      rules:
        - port: 80
          protocol: tcp
          source: 0.0.0.0/0
        - port: 443
          protocol: tcp
          source: 0.0.0.0/0

databases:
  - name: user-db
    type: postgresql
    version: 13
    instance-type: db.t3.micro
    databases:
      - name: userdb
        users:
          - username: app_user
            password: ${random.password}
            permissions: [SELECT, INSERT, UPDATE, DELETE]
            
  - name: order-db
    type: mysql
    version: 8.0
    instance-type: db.t3.micro
    databases:
      - name: orderdb
        users:
          - username: order_user
            password: ${random.password}
            permissions: [SELECT, INSERT, UPDATE, DELETE]

applications:
  - name: api-gateway
    container-image: nginx:alpine
    replicas: 2
    ports:
      - container-port: 80
        service-port: 80
    configuration:
      UPSTREAM_SERVICES: user-service:8080,order-service:8080
      
  - name: user-service
    container-image: user-service:latest
    replicas: 3
    ports:
      - container-port: 8080
        service-port: 8080
    configuration:
      DATABASE_URL: ${database.user-db.connection-string}
      DATABASE_USERNAME: ${database.user-db.username}
      DATABASE_PASSWORD: ${database.user-db.password}
      
  - name: order-service
    container-image: order-service:latest
    replicas: 3
    ports:
      - container-port: 8080
        service-port: 8080
    configuration:
      DATABASE_URL: ${database.order-db.connection-string}
      DATABASE_USERNAME: ${database.order-db.username}
      DATABASE_PASSWORD: ${database.order-db.password}
      USER_SERVICE_URL: http://user-service:8080

configurations:
  global:
    ENVIRONMENT: test
    LOG_LEVEL: DEBUG
    METRICS_ENABLED: true
```

## API Integration

### Environment Provisioning API
```java
@RestController
@RequestMapping("/api/environments")
public class EnvironmentProvisioningController {
    
    @PostMapping("/provision")
    public ResponseEntity<ProvisioningResponse> provisionEnvironment(
            @RequestBody ProvisioningRequest request) {
        
        ProvisioningResult result = provisioningOrchestrator.provisionEnvironment(request);
        
        ProvisioningResponse response = ProvisioningResponse.builder()
            .environmentId(result.getEnvironmentId())
            .status(result.getStatus())
            .endpoints(result.getEndpoints())
            .estimatedReadyTime(result.getEstimatedReadyTime())
            .build();
            
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{environmentId}/status")
    public ResponseEntity<EnvironmentStatus> getEnvironmentStatus(
            @PathVariable String environmentId) {
        
        EnvironmentInstance environment = environmentRepository.findById(environmentId);
        
        if (environment == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(environment.getStatus());
    }
    
    @DeleteMapping("/{environmentId}")
    public ResponseEntity<Void> cleanupEnvironment(@PathVariable String environmentId) {
        lifecycleManager.cleanupEnvironment(environmentId);
        return ResponseEntity.ok().build();
    }
}
```

## Success Criteria
1. **Provisioning Speed**: Complete environment provisioning within 5 minutes
2. **Success Rate**: 99.9% successful environment provisioning
3. **Resource Efficiency**: Automatic resource optimization and cleanup
4. **Scalability**: Support 100+ concurrent environments
5. **Cost Management**: Automatic cleanup to minimize cloud costs
6. **Template Flexibility**: Support for various application architectures

## Monitoring and Cost Management
1. **Resource Monitoring**: Track resource usage and costs per environment
2. **Performance Metrics**: Monitor provisioning times and success rates
3. **Cost Optimization**: Automatic resource scaling and cleanup
4. **Usage Analytics**: Environment usage patterns and optimization opportunities

## Deliverables
1. **Core Services**: Provisioning orchestrator, template manager, lifecycle manager
2. **Infrastructure Managers**: Container, database, network, and storage managers
3. **Template System**: Reusable environment templates for different scenarios
4. **API Layer**: RESTful APIs for environment management
5. **Monitoring Dashboard**: Real-time visibility into environment status and costs

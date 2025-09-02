package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ApplicationSpec {
    private String name;
    private DeploymentType deploymentType;
    private String containerImage;
    private int replicas;
    private List<PortMapping> ports;
    private List<VolumeMount> volumes;
    private ResourceRequirements resourceRequirements;
    private Map<String, String> configuration;
    private HealthCheck healthCheck;
}
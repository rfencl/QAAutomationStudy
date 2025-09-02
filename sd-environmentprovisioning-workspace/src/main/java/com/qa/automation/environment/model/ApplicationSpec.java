package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Application specification for deployment
 */
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

enum DeploymentType {
    CONTAINER, VM
}

@Data
@Builder
class PortMapping {
    private int containerPort;
    private int servicePort;
    private String protocol;
}

@Data
@Builder
class VolumeMount {
    private String name;
    private String mountPath;
    private String hostPath;
}

@Data
@Builder
class HealthCheck {
    private String path;
    private int port;
    private int initialDelaySeconds;
    private int periodSeconds;
}

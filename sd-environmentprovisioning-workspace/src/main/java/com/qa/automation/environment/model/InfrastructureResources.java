package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Provisioned infrastructure resources
 */
@Data
@Builder
public class InfrastructureResources {
    private String resourceGroup;
    private List<ComputeInstance> computeInstances;
    private ContainerCluster containerCluster;
}

@Data
@Builder
class ComputeInstance {
    private String id;
    private String instanceType;
    private String publicIp;
    private String privateIp;
    private String status;
}

@Data
@Builder
class ContainerCluster {
    private String id;
    private String name;
    private String endpoint;
    private int nodeCount;
    private String status;
}

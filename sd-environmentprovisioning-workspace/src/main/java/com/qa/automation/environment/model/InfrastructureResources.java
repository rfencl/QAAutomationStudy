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



package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Infrastructure specification for environment provisioning
 */
@Data
@Builder
public class InfrastructureSpec {
    private String cloudProvider;
    private boolean containerized;
    private ContainerSpec containerSpec;
    private List<ComputeSpec> computeSpecs;
    private ResourceRequirements resourceRequirements;
}

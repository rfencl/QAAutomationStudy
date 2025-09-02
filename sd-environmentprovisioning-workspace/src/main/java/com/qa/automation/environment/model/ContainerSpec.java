package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContainerSpec {
    private String orchestrator;
    private int clusterSize;
    private String nodeType;
}

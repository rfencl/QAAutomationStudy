package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContainerCluster {
    private String id;
    private String name;
    private String endpoint;
    private int nodeCount;
    private String status;
}

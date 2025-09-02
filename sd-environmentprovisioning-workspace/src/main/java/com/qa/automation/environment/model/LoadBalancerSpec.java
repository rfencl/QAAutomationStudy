package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class LoadBalancerSpec {
    private String name;
    private String type;
    private List<String> subnets;
    private List<TargetGroup> targetGroups;
}

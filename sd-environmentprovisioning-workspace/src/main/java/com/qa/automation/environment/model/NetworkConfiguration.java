package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Provisioned network configuration
 */
@Data
@Builder
public class NetworkConfiguration {
    private VirtualNetwork virtualNetwork;
    private List<Subnet> subnets;
    private List<SecurityGroup> securityGroups;
    private List<LoadBalancer> loadBalancers;
}



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

@Data
@Builder
class VirtualNetwork {
    private String id;
    private String name;
    private String cidr;
    private String status;
}

@Data
@Builder
class Subnet {
    private String id;
    private String name;
    private String cidr;
    private String type;
    private String availabilityZone;
}

@Data
@Builder
class SecurityGroup {
    private String id;
    private String name;
    private List<SecurityRule> rules;
}

@Data
@Builder
class LoadBalancer {
    private String id;
    private String name;
    private String dnsName;
    private String type;
    private List<String> subnets;
}

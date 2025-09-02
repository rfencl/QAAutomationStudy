package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Network specification for environment provisioning
 */
@Data
@Builder
public class NetworkSpec {
    private String vpcCidr;
    private List<SubnetSpec> subnetSpecs;
    private List<SecurityGroupSpec> securityGroupSpecs;
    private List<LoadBalancerSpec> loadBalancerSpecs;
}

@Data
@Builder
class SubnetSpec {
    private String name;
    private String cidr;
    private String type;
    private String availabilityZone;
}

@Data
@Builder
class SecurityGroupSpec {
    private String name;
    private String description;
    private List<SecurityRule> rules;
}

@Data
@Builder
class SecurityRule {
    private int port;
    private String protocol;
    private String source;
    private String direction;
}

@Data
@Builder
class LoadBalancerSpec {
    private String name;
    private String type;
    private List<String> subnets;
    private List<TargetGroup> targetGroups;
}

@Data
@Builder
class TargetGroup {
    private String name;
    private int port;
    private String protocol;
    private String healthCheckPath;
}

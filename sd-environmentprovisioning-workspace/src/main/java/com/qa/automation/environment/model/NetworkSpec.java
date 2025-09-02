package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class NetworkSpec {
    private String vpcCidr;
    private List<SubnetSpec> subnetSpecs;
    private List<SecurityGroupSpec> securityGroupSpecs;
    private List<LoadBalancerSpec> loadBalancerSpecs;
}

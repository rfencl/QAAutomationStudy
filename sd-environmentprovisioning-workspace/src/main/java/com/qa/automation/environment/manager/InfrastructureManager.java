package com.qa.automation.environment.manager;

import com.qa.automation.environment.model.*;
import com.qa.automation.environment.provider.CloudProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages infrastructure provisioning across different cloud providers.
 * 
 * Design Decision: Uses strategy pattern with CloudProvider interface
 * to support multiple cloud providers (AWS, Azure, GCP) without
 * changing the core logic.
 */
@Slf4j
@RequiredArgsConstructor
public class InfrastructureManager {
    
    private final Map<String, CloudProvider> cloudProviders;

    /**
     * Provisions infrastructure resources based on specification.
     */
    public InfrastructureResources provisionInfrastructure(InfrastructureSpec spec, String environmentId) {
        log.info("Provisioning infrastructure for environment: {}", environmentId);
        
        CloudProvider provider = getCloudProvider(spec.getCloudProvider());
        
        // Create resource group for isolation
        String resourceGroup = createResourceGroup(environmentId, provider);
        
        // Provision based on containerization preference
        if (spec.isContainerized()) {
            return provisionContainerizedInfrastructure(spec, resourceGroup, provider);
        } else {
            return provisionVMInfrastructure(spec, resourceGroup, provider);
        }
    }

    /**
     * Sets up networking configuration.
     */
    public NetworkConfiguration setupNetworking(NetworkSpec spec, String environmentId) {
        log.info("Setting up networking for environment: {}", environmentId);
        
        // Create virtual network
        VirtualNetwork vnet = VirtualNetwork.builder()
            .id("vnet-" + environmentId)
            .name("vnet-" + environmentId)
            .cidr(spec.getVpcCidr())
            .status("Active")
            .build();

        // Create subnets
        List<Subnet> subnets = createSubnets(spec.getSubnetSpecs(), vnet);
        
        // Create security groups
        List<SecurityGroup> securityGroups = createSecurityGroups(spec.getSecurityGroupSpecs());
        
        // Create load balancers if specified
        List<LoadBalancer> loadBalancers = createLoadBalancers(spec.getLoadBalancerSpecs());

        return NetworkConfiguration.builder()
            .virtualNetwork(vnet)
            .subnets(subnets)
            .securityGroups(securityGroups)
            .loadBalancers(loadBalancers)
            .build();
    }

    private CloudProvider getCloudProvider(String providerName) {
        CloudProvider provider = cloudProviders.get(providerName);
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported cloud provider: " + providerName);
        }
        return provider;
    }

    private String createResourceGroup(String environmentId, CloudProvider provider) {
        String resourceGroup = "rg-" + environmentId;
        log.info("Creating resource group: {}", resourceGroup);
        return resourceGroup;
    }

    private InfrastructureResources provisionContainerizedInfrastructure(
            InfrastructureSpec spec, String resourceGroup, CloudProvider provider) {
        
        // Create container cluster
        ContainerCluster cluster = ContainerCluster.builder()
            .id("cluster-" + UUID.randomUUID().toString().substring(0, 8))
            .name("cluster-" + resourceGroup)
            .endpoint("https://cluster-" + resourceGroup + ".example.com")
            .nodeCount(spec.getContainerSpec().getClusterSize())
            .status("Running")
            .build();

        return InfrastructureResources.builder()
            .resourceGroup(resourceGroup)
            .containerCluster(cluster)
            .build();
    }

    private InfrastructureResources provisionVMInfrastructure(
            InfrastructureSpec spec, String resourceGroup, CloudProvider provider) {
        
        // Create compute instances
        List<ComputeInstance> instances = spec.getComputeSpecs().stream()
            .map(computeSpec -> ComputeInstance.builder()
                .id("vm-" + UUID.randomUUID().toString().substring(0, 8))
                .instanceType(computeSpec.getInstanceType())
                .publicIp("203.0.113." + (int)(Math.random() * 254 + 1))
                .privateIp("10.0.1." + (int)(Math.random() * 254 + 1))
                .status("Running")
                .build())
            .collect(java.util.stream.Collectors.toList());

        return InfrastructureResources.builder()
            .resourceGroup(resourceGroup)
            .computeInstances(instances)
            .build();
    }

    private List<Subnet> createSubnets(List<SubnetSpec> subnetSpecs, VirtualNetwork vnet) {
        if (subnetSpecs == null) {
            // Create default subnets
            return List.of(
                Subnet.builder()
                    .id("subnet-public")
                    .name("public")
                    .cidr("10.0.1.0/24")
                    .type("public")
                    .build(),
                Subnet.builder()
                    .id("subnet-private")
                    .name("private")
                    .cidr("10.0.2.0/24")
                    .type("private")
                    .build()
            );
        }
        
        return subnetSpecs.stream()
            .map(spec -> Subnet.builder()
                .id("subnet-" + spec.getName())
                .name(spec.getName())
                .cidr(spec.getCidr())
                .type(spec.getType())
                .availabilityZone(spec.getAvailabilityZone())
                .build())
            .collect(java.util.stream.Collectors.toList());
    }

    private List<SecurityGroup> createSecurityGroups(List<SecurityGroupSpec> securityGroupSpecs) {
        if (securityGroupSpecs == null) {
            return List.of();
        }
        
        return securityGroupSpecs.stream()
            .map(spec -> SecurityGroup.builder()
                .id("sg-" + spec.getName())
                .name(spec.getName())
                .rules(spec.getRules())
                .build())
            .collect(java.util.stream.Collectors.toList());
    }

    private List<LoadBalancer> createLoadBalancers(List<LoadBalancerSpec> loadBalancerSpecs) {
        if (loadBalancerSpecs == null) {
            return List.of();
        }
        
        return loadBalancerSpecs.stream()
            .map(spec -> LoadBalancer.builder()
                .id("lb-" + spec.getName())
                .name(spec.getName())
                .dnsName(spec.getName() + ".example.com")
                .type(spec.getType())
                .subnets(spec.getSubnets())
                .build())
            .collect(java.util.stream.Collectors.toList());
    }
}

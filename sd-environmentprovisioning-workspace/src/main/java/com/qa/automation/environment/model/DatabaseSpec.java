package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Database specification for environment provisioning
 */
@Data
@Builder
public class DatabaseSpec {
    private String name;
    private String databaseType;
    private String version;
    private String instanceType;
    private StorageConfig storageConfig;
    private NetworkConfig networkConfig;
    private List<DatabaseConfig> databases;
    private String initialDataScript;
    private Map<String, Object> configuration;
}

@Data
@Builder
class StorageConfig {
    private int sizeGb;
    private String storageType;
    private boolean encrypted;
}

@Data
@Builder
class NetworkConfig {
    private String subnetId;
    private List<String> securityGroupIds;
    private boolean publicAccess;
}

@Data
@Builder
class DatabaseConfig {
    private String name;
    private List<UserConfig> users;
}

@Data
@Builder
class UserConfig {
    private String username;
    private String password;
    private List<String> permissions;
}

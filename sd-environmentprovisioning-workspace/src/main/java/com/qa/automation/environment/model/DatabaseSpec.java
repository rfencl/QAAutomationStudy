package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

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
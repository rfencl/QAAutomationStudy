package com.qa.automation.environment.provider;

import lombok.Builder;
import lombok.Data;

/**
 * Configuration for creating a database instance.
 * This class is used to pass parameters to a DatabaseProvider.
 */
@Data
@Builder
public class DatabaseInstanceConfig {
    private String instanceType;
    private String version;
    private String environmentId;
    // In a real implementation, this would include storage, networking, etc.
}

package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Provisioned database instance
 */
@Data
@Builder
public class DatabaseInstance {
    private String id;
    private String name;
    private String databaseType;
    private String version;
    private String endpoint;
    private int port;
    private String status;
    private List<String> databases;
    private ConnectionInfo connectionInfo;
}

@Data
@Builder
class ConnectionInfo {
    private String host;
    private int port;
    private String username;
    private String password;
    private String connectionString;
}

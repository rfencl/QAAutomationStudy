package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectionInfo {
    private String host;
    private int port;
    private String username;
    private String password;
    private String connectionString;
}

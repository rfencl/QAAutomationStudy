package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DatabaseConfig {
    private String name;
    private List<UserConfig> users;
}

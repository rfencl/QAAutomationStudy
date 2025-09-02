package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class UserConfig {
    private String username;
    private String password;
    private List<String> permissions;
}
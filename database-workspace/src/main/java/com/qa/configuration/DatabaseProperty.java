package com.qa.configuration;

import java.util.Map;
import java.util.Properties;

public class DatabaseProperty {

    private String DB_PORT;
    private String DB_SERVER_URL;
    private String DB_URL;
    private String DB_USER;
    private String DB_PASSWORD;
    private static final Properties properties;
    private static final Map<String, String> env;
    private static DatabaseProperty instance;

    static {
        properties = new Properties();
        env = System.getenv();
        AppProperty.getProperties("database.properties");
        instance = new DatabaseProperty();
        instance.DB_PORT = properties.getProperty("db_server_port");
        instance.DB_SERVER_URL = properties.getProperty("db_server_url");
        instance.DB_URL = properties.getProperty("db_url");
        instance.DB_USER = properties.getProperty("db_user");
        instance.DB_PASSWORD = properties.getProperty("db_password");

    }

    private DatabaseProperty() {
        // Private constructor to prevent instantiation
    }

    public static String getDbServerUrl() {
        String ip = getIp();
        return instance.DB_SERVER_URL.replace("{IP}", ip).replace("{PORT}", instance.DB_PORT);
    }

    public static String getIp() {
        return env.getOrDefault("MYSQL_IP", "localhost");
    }

    public static String getDbUrl() {
        return instance.DB_URL.replace("{IP}", env.get("MYSQL_IP")).replace("{PORT}", instance.DB_PORT);
    }

    public static String getDbUser() {
        return instance.DB_USER;
    }

    public static String getDbPassword() {
        return instance.DB_PASSWORD;
    }

    public static Properties getProperties() {
        return properties;
    }

}

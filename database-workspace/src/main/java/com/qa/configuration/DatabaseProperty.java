package com.qa.configuration;

import java.util.Map;
import java.util.Properties;

public class DatabaseProperty {

    private static String DB_PORT;
    private static String DB_SERVER_URL;
    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;
    private static final Properties properties;
    private static final Map<String, String> env;
    private static DatabaseProperty instance;

    static {
        properties = new Properties();
        env = System.getenv();
        AppProperty.getProperties("database.properties");
        DB_PORT = properties.getProperty("db_server_port");
        DB_SERVER_URL = properties.getProperty("db_server_url");
        DB_URL = properties.getProperty("db_url");
        DB_USER = properties.getProperty("db_user");
        DB_PASSWORD = properties.getProperty("db_password");

    }

    public static String getDbServerUrl() {
        String ip = getIp();
        return instance.DB_SERVER_URL.replace("{IP}", ip).replace("{PORT}", instance.DB_PORT);
    }

    public static String getIp() {
        String ip =  env.getOrDefault("MYSQL_IP", "localhost");
        return ip;
    }

    public static String getDbUrl() {
        String ip = getIp();
        return instance.DB_URL.replace("{IP}", ip).replace("{PORT}", instance.DB_PORT);
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

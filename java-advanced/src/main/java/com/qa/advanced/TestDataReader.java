package com.qa.advanced;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class to read test data from CSV and JSON files
 * Exercise: Write a utility to read test data from a CSV or JSON file
 */
public class TestDataReader {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Read test data from CSV file
     * @param filePath path to CSV file
     * @return List of maps where each map represents a row with column headers as keys
     */
    public static List<Map<String, String>> readCSVData(String filePath) {
        List<Map<String, String>> testData = new ArrayList<>();
        
        try (CSVReader csvReader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = csvReader.readAll();
            
            if (records.isEmpty()) {
                throw new IllegalArgumentException("CSV file is empty: " + filePath);
            }
            
            // First row contains headers
            String[] headers = records.get(0);
            
            // Process data rows
            for (int i = 1; i < records.size(); i++) {
                String[] row = records.get(i);
                Map<String, String> rowData = new HashMap<>();
                
                for (int j = 0; j < headers.length && j < row.length; j++) {
                    rowData.put(headers[j].trim(), row[j].trim());
                }
                testData.add(rowData);
            }
            
        } catch (IOException | CsvException e) {
            throw new RuntimeException("Failed to read CSV file: " + filePath, e);
        }
        
        return testData;
    }
    
    /**
     * Read test data from JSON file
     * @param filePath path to JSON file
     * @return List of maps containing test data
     */
    public static List<Map<String, Object>> readJSONData(String filePath) {
        try {
            JsonNode rootNode = objectMapper.readTree(new File(filePath));
            List<Map<String, Object>> testData = new ArrayList<>();
            
            if (rootNode.isArray()) {
                // JSON array format
                for (JsonNode node : rootNode) {
                    TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
                    Map<String, Object> rowData = objectMapper.convertValue(node, typeRef);
                    testData.add(rowData);
                }
            } else if (rootNode.has("testData")) {
                // JSON object with testData array
                JsonNode testDataNode = rootNode.get("testData");
                for (JsonNode node : testDataNode) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> rowData = objectMapper.convertValue(node, Map.class);
                    testData.add(rowData);
                }
            } else {
                // Single JSON object
                @SuppressWarnings("unchecked")
                Map<String, Object> rowData = objectMapper.convertValue(rootNode, Map.class);
                testData.add(rowData);
            }
            
            return testData;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file: " + filePath, e);
        }
    }
    
    /**
     * Filter test data based on criteria
     * @param testData original test data
     * @param filterKey key to filter on
     * @param filterValue value to match
     * @return filtered test data
     */
    public static List<Map<String, String>> filterCSVData(List<Map<String, String>> testData, 
                                                          String filterKey, String filterValue) {
        return testData.stream()
                .filter(row -> filterValue.equals(row.get(filterKey)))
                .collect(Collectors.toList());
    }
    
    /**
     * Get specific test data by test case name
     * @param testData all test data
     * @param testCaseName name of the test case
     * @return test data for specific test case
     */
    public static Map<String, String> getTestDataByName(List<Map<String, String>> testData, 
                                                        String testCaseName) {
        return testData.stream()
                .filter(row -> testCaseName.equals(row.get("testCase")))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Test case not found: " + testCaseName));
    }
    
    /**
     * Convert CSV data to TestNG data provider format
     * @param filePath path to CSV file
     * @return Object[][] for TestNG data provider
     */
    public static Object[][] csvToDataProvider(String filePath) {
        List<Map<String, String>> testData = readCSVData(filePath);
        Object[][] dataProvider = new Object[testData.size()][];
        
        for (int i = 0; i < testData.size(); i++) {
            dataProvider[i] = new Object[]{testData.get(i)};
        }
        
        return dataProvider;
    }
    
    /**
     * Read properties file for configuration
     * @param filePath path to properties file
     * @return Properties object
     */
    public static Properties readProperties(String filePath) {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(filePath)) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read properties file: " + filePath, e);
        }
        return properties;
    }
    
    /**
     * Create sample CSV file for testing
     * @param filePath where to create the file
     */
    public static void createSampleCSVFile(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("testCase,username,password,expectedResult,description");
            writer.println("validLogin,qa_user,password123,success,Valid user login");
            writer.println("invalidPassword,qa_user,wrongpass,failure,Invalid password");
            writer.println("invalidUser,invalid_user,password123,failure,Invalid username");
            writer.println("emptyFields,,,,failure,Empty username and password");
            writer.println("sqlInjection,'OR'1'='1,password,failure,SQL injection attempt");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create sample CSV file", e);
        }
    }
    
    /**
     * Create sample JSON file for testing
     * @param filePath where to create the file
     */
    public static void createSampleJSONFile(String filePath) {
        try {
            List<Map<String, Object>> testData = Arrays.asList(
                Map.of("testCase", "validLogin", "username", "qa_user", "password", "password123", 
                       "expectedResult", "success", "description", "Valid user login"),
                Map.of("testCase", "invalidPassword", "username", "qa_user", "password", "wrongpass", 
                       "expectedResult", "failure", "description", "Invalid password"),
                Map.of("testCase", "invalidUser", "username", "invalid_user", "password", "password123", 
                       "expectedResult", "failure", "description", "Invalid username")
            );
            
            Map<String, Object> jsonStructure = Map.of("testData", testData);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), jsonStructure);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to create sample JSON file", e);
        }
    }
}

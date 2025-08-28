package com.qa.advanced;

import org.testng.Assert;
import org.testng.annotations.*;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Test class for TestDataReader utility
 * Demonstrates data-driven testing with CSV and JSON
 */
public class TestDataReaderTest {

    private static final String CSV_FILE_PATH = "src/test/resources/test-data.csv";
    private static final String JSON_FILE_PATH = "src/test/resources/test-data.json";
    private static final String PROPERTIES_FILE_PATH = "src/test/resources/test.properties";

    @BeforeClass
    public void setupTestData() throws Exception {
        // Create test resources directory
        new File("src/test/resources").mkdirs();

        // Create sample test data files
        TestDataReader.createSampleCSVFile(CSV_FILE_PATH);
        TestDataReader.createSampleJSONFile(JSON_FILE_PATH);
        java.nio.file.Files.write(
                java.nio.file.Paths.get(
                        PROPERTIES_FILE_PATH),
                "browser=chrome\ntimeout=30\nbaseUrl=https://example.com".getBytes());
    }

    @Test(priority = 1)
    public void testReadCSVData() {
        List<Map<String, String>> testData = TestDataReader.readCSVData(CSV_FILE_PATH);

        Assert.assertNotNull(testData, "CSV data should not be null");
        Assert.assertTrue(testData.size() > 0, "CSV should contain test data");

        // Verify first row data
        Map<String, String> firstRow = testData.get(0);
        Assert.assertEquals(firstRow.get("testCase"), "validLogin", "First test case should be validLogin");
        Assert.assertEquals(firstRow.get("username"), "qa_user", "Username should match");
        Assert.assertEquals(firstRow.get("expectedResult"), "success", "Expected result should be success");

        System.out.println("CSV Test Data loaded: " + testData.size() + " records");
    }

    @Test(priority = 2)
    public void testReadJSONData() {
        List<Map<String, Object>> testData = TestDataReader.readJSONData(JSON_FILE_PATH);

        Assert.assertNotNull(testData, "JSON data should not be null");
        Assert.assertTrue(testData.size() > 0, "JSON should contain test data");

        // Verify first row data
        Map<String, Object> firstRow = testData.get(0);
        Assert.assertEquals(firstRow.get("testCase"), "validLogin", "First test case should be validLogin");
        Assert.assertEquals(firstRow.get("username"), "qa_user", "Username should match");

        System.out.println("JSON Test Data loaded: " + testData.size() + " records");
    }

    @Test(priority = 3)
    public void testFilterCSVData() {
        List<Map<String, String>> allData = TestDataReader.readCSVData(CSV_FILE_PATH);
        List<Map<String, String>> failureTests = TestDataReader.filterCSVData(allData, "expectedResult", "failure");

        Assert.assertTrue(failureTests.size() > 0, "Should find failure test cases");

        // Verify all filtered results have expectedResult = "failure"
        for (Map<String, String> testCase : failureTests) {
            Assert.assertEquals(testCase.get("expectedResult"), "failure",
                    "All filtered tests should have expectedResult = failure");
        }

        System.out.println("Filtered failure tests: " + failureTests.size());
    }

    @Test(priority = 4)
    public void testGetTestDataByName() {
        List<Map<String, String>> testData = TestDataReader.readCSVData(CSV_FILE_PATH);
        Map<String, String> validLoginTest = TestDataReader.getTestDataByName(testData, "validLogin");

        Assert.assertNotNull(validLoginTest, "Should find validLogin test case");
        Assert.assertEquals(validLoginTest.get("username"), "qa_user", "Username should match");
        Assert.assertEquals(validLoginTest.get("password"), "password123", "Password should match");

        System.out.println("Found test case: " + validLoginTest.get("testCase"));
    }

    @Test(priority = 5, expectedExceptions = IllegalArgumentException.class)
    public void testGetNonExistentTestData() {
        List<Map<String, String>> testData = TestDataReader.readCSVData(CSV_FILE_PATH);
        TestDataReader.getTestDataByName(testData, "nonExistentTest");
    }

    @Test(priority = 6, dataProvider = "csvTestData")
    public void testDataDrivenLogin(Map<String, String> testData) {
        String testCase = testData.get("testCase");
        String username = testData.get("username");
        String password = testData.get("password");
        String expectedResult = testData.get("expectedResult");
        String description = testData.get("description");

        System.out.println("Executing: " + testCase + " - " + description);

        // Simulate login test
        boolean loginResult = simulateLogin(username, password);
        boolean expectedSuccess = "success".equals(expectedResult);

        Assert.assertEquals(loginResult, expectedSuccess,
                "Login result should match expected result for " + testCase);
    }

    @DataProvider(name = "csvTestData")
    public Object[][] getCsvTestData() {
        return TestDataReader.csvToDataProvider(CSV_FILE_PATH);
    }

    @Test(priority = 7, dataProvider = "jsonTestData")
    public void testDataDrivenLoginFromJSON(Map<String, Object> testData) {
        String testCase = (String) testData.get("testCase");
        String username = (String) testData.get("username");
        String password = (String) testData.get("password");
        String expectedResult = (String) testData.get("expectedResult");

        System.out.println("JSON Test - Executing: " + testCase);

        boolean loginResult = simulateLogin(username, password);
        boolean expectedSuccess = "success".equals(expectedResult);

        Assert.assertEquals(loginResult, expectedSuccess,
                "Login result should match expected result for " + testCase);
    }

    @DataProvider(name = "jsonTestData")
    public Object[][] getJsonTestData() {
        List<Map<String, Object>> testData = TestDataReader.readJSONData(JSON_FILE_PATH);
        Object[][] dataProvider = new Object[testData.size()][];

        for (int i = 0; i < testData.size(); i++) {
            dataProvider[i] = new Object[] { testData.get(i) };
        }

        return dataProvider;
    }

    @Test(priority = 8)
    public void testErrorHandling() {
        // Test with non-existent file
        try {
            TestDataReader.readCSVData("non-existent-file.csv");
            Assert.fail("Should throw exception for non-existent file");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to read CSV file"),
                    "Should get appropriate error message");
        }

        // Test with empty file path
        try {
            TestDataReader.readJSONData("");
            Assert.fail("Should throw exception for empty file path");
        } catch (RuntimeException e) {
            // Expected exception
            System.out.println("Correctly handled empty file path error");
        }
    }

    @Test(priority = 9)
    public void testPropertiesReading() {
        try {
            java.util.Properties props = TestDataReader.readProperties(PROPERTIES_FILE_PATH);

            Assert.assertEquals(props.getProperty("browser"), "chrome", "Browser property should match");
            Assert.assertEquals(props.getProperty("timeout"), "30", "Timeout property should match");
            Assert.assertEquals(props.getProperty("baseUrl"), "https://example.com", "Base URL should match");

            System.out.println("Properties loaded successfully: " + props.size() + " properties");

        } catch (Exception e) {
            Assert.fail("Failed to test properties reading: " + e.getMessage());
        }
    }

    // Helper method to simulate login
    private boolean simulateLogin(String username, String password) {
        // Simulate login logic
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }

        if (username.contains("'") || password.contains("'")) {
            return false; // SQL injection attempt
        }

        return "qa_user".equals(username) && "password123".equals(password);
    }

    @AfterClass
    public void cleanup() {
        // Clean up test files
        try {
            new File(CSV_FILE_PATH).delete();
            new File(JSON_FILE_PATH).delete();
            new File("src/test/resources/test.properties").delete();
            System.out.println("Test files cleaned up");
        } catch (Exception e) {
            System.out.println("Cleanup warning: " + e.getMessage());
        }
    }
}

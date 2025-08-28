package com.qa.advanced;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Demonstrates Object-Oriented Programming concepts for QA Automation
 * Focus: classes, objects, inheritance, interfaces, polymorphism, encapsulation
 */

// Interface for test execution
interface TestExecutor {
    void executeTest();
    String getTestName();
    boolean isTestPassed();
}

// Abstract base class demonstrating inheritance and encapsulation
abstract class BaseTest implements TestExecutor {
    protected String testName;
    protected boolean testPassed;
    protected List<String> testSteps;
    private Date executionTime;
    
    /**
     * Constructs a new BaseTest with the specified test name.
     * Initializes the test with default values: testPassed as false,
     * empty test steps list, and no execution time.
     * 
     * @param testName the name of the test to be executed
     */
    public BaseTest(String testName) {
        this.testName = testName;
        this.testSteps = new ArrayList<>();
        this.testPassed = false;
    }
    
    /**
     * Retrieves the name of this test.
     * This method provides read-only access to the test name,
     * demonstrating encapsulation by protecting the internal state.
     * 
     * @return the name of the test as a String
     */
    public String getTestName() {
        return testName;
    }
    
    /**
     * Checks whether this test has passed or failed.
     * Returns the current status of the test execution result.
     * 
     * @return true if the test passed, false if it failed or hasn't been executed
     */
    public boolean isTestPassed() {
        return testPassed;
    }
    
    /**
     * Retrieves the timestamp when this test was executed.
     * The execution time is set when the test result is finalized.
     * 
     * @return the Date object representing when the test was executed,
     *         or null if the test hasn't been executed yet
     */
    public Date getExecutionTime() {
        return executionTime;
    }
    
    /**
     * Adds a test step description to the internal test steps list.
     * This protected method allows subclasses to record the sequence
     * of actions performed during test execution for reporting purposes.
     * 
     * @param step a descriptive string of the test step being performed
     */
    protected void addTestStep(String step) {
        testSteps.add(step);
    }
    
    /**
     * Sets the final result of the test execution and records the execution time.
     * This protected method allows subclasses to finalize the test status
     * and automatically timestamps the completion.
     * 
     * @param passed true if the test should be marked as passed, false for failed
     */
    protected void setTestResult(boolean passed) {
        this.testPassed = passed;
        this.executionTime = new Date();
    }
    
    /**
     * Executes the test logic specific to each test type.
     * This abstract method must be implemented by all concrete subclasses
     * to define their specific test execution behavior. The implementation
     * should use addTestStep() to record actions and setTestResult() to finalize.
     */
    public abstract void executeTest();
    
    /**
     * Prints a formatted summary of the test execution results to the console.
     * Displays the test name, pass/fail status, execution timestamp,
     * and the total number of test steps that were executed.
     * This method provides a standardized way to report test outcomes.
     */
    public void printTestResults() {
        System.out.println("Test: " + testName);
        System.out.println("Status: " + (testPassed ? "PASSED" : "FAILED"));
        System.out.println("Execution Time: " + executionTime);
        System.out.println("Steps executed: " + testSteps.size());
    }
}

// Concrete implementation - Login Test
class LoginTest extends BaseTest {
    private String username;
    private String password;
    
    /**
     * Constructs a new LoginTest with specified credentials.
     * Initializes the test with the provided test name and login credentials
     * that will be used during test execution.
     * 
     * @param testName the descriptive name for this login test
     * @param username the username to be tested during login
     * @param password the password to be tested during login
     */
    public LoginTest(String testName, String username, String password) {
        super(testName);
        this.username = username;
        this.password = password;
    }
    
    /**
     * Executes the login test scenario with the configured credentials.
     * Simulates a complete login workflow including navigation, credential entry,
     * form submission, and result validation. Records each step for reporting
     * and sets the final test result based on login success or failure.
     * 
     * @override executeTest method from BaseTest
     */
    @Override
    public void executeTest() {
        addTestStep("Navigate to login page");
        addTestStep("Enter username: " + username);
        addTestStep("Enter password: " + password);
        addTestStep("Click login button");
        
        // Simulate test logic
        boolean loginSuccessful = validateLogin(username, password);
        
        if (loginSuccessful) {
            addTestStep("Login successful - redirected to dashboard");
            setTestResult(true);
        } else {
            addTestStep("Login failed - error message displayed");
            setTestResult(false);
        }
    }
    
    /**
     * Validates the provided login credentials against expected values.
     * This private method simulates authentication logic by checking
     * if the provided credentials match the predefined valid credentials.
     * 
     * @param user the username to validate
     * @param pass the password to validate
     * @return true if credentials match "qa_user"/"password123", false otherwise
     */
    private boolean validateLogin(String user, String pass) {
        // Simulate login validation
        return "qa_user".equals(user) && "password123".equals(pass);
    }
}

// Another concrete implementation - Database Test
class DatabaseTest extends BaseTest {
    private String query;
    private Object expectedResult;
    
    /**
     * Constructs a new DatabaseTest with specified query and expected result.
     * Initializes the test with a SQL query to execute and the expected
     * result to compare against for validation.
     * 
     * @param testName the descriptive name for this database test
     * @param query the SQL query string to be executed
     * @param expectedResult the expected result object to compare against actual results
     */
    public DatabaseTest(String testName, String query, Object expectedResult) {
        super(testName);
        this.query = query;
        this.expectedResult = expectedResult;
    }
    
    /**
     * Executes the database test by running the configured query and comparing results.
     * Simulates a complete database testing workflow including connection establishment,
     * query execution, result comparison, and test result determination.
     * Records detailed steps for comprehensive test reporting.
     * 
     * @override executeTest method from BaseTest
     */
    @Override
    public void executeTest() {
        addTestStep("Connect to database");
        addTestStep("Execute query: " + query);
        
        // Simulate database query execution
        Object actualResult = executeQuery(query);
        
        addTestStep("Compare results");
        boolean resultsMatch = Objects.equals(expectedResult, actualResult);
        
        if (resultsMatch) {
            addTestStep("Results match expected values");
            setTestResult(true);
        } else {
            addTestStep("Results don't match. Expected: " + expectedResult + ", Actual: " + actualResult);
            setTestResult(false);
        }
    }
    
    /**
     * Simulates database query execution and returns mock results.
     * This private method provides a simplified simulation of database
     * interaction for testing purposes without requiring actual database connectivity.
     * 
     * @param query the SQL query string to simulate execution for
     * @return Integer 5 if query contains "COUNT", otherwise returns "Mock Result" string
     */
    private Object executeQuery(String query) {
        // Simulate query execution
        if (query.contains("COUNT")) {
            return 5; // Mock count result
        }
        return "Mock Result";
    }
}

// Utility class demonstrating static methods and helper functions
class TestUtils {
    
    /**
     * Filters a list of tests to return only those that have passed.
     * Uses Java 8 Stream API to efficiently filter the test collection
     * based on the test execution results.
     * 
     * @param tests the list of BaseTest objects to filter
     * @return a new List containing only the tests that passed
     */
    public static List<BaseTest> filterPassedTests(List<BaseTest> tests) {
        return tests.stream()
                .filter(BaseTest::isTestPassed)
                .collect(Collectors.toList());
    }
    
    /**
     * Filters tests by name pattern matching using substring search.
     * Returns all tests whose names contain the specified pattern,
     * enabling flexible test selection based on naming conventions.
     * 
     * @param tests the list of BaseTest objects to search through
     * @param namePattern the string pattern to search for in test names
     * @return a new List containing tests whose names contain the pattern
     */
    public static List<BaseTest> filterTestsByName(List<BaseTest> tests, String namePattern) {
        return tests.stream()
                .filter(test -> test.getTestName().contains(namePattern))
                .collect(Collectors.toList());
    }
    
    /**
     * Filters tests using a custom predicate condition for maximum flexibility.
     * Demonstrates Java 8+ functional programming features by accepting
     * a Predicate function that defines the filtering criteria.
     * 
     * @param tests the list of BaseTest objects to filter
     * @param condition a Predicate function that returns true for tests to include
     * @return a new List containing tests that satisfy the predicate condition
     */
    public static List<BaseTest> filterTests(List<BaseTest> tests, Predicate<BaseTest> condition) {
        return tests.stream()
                .filter(condition)
                .collect(Collectors.toList());
    }
    
    /**
     * Executes all tests in the provided list and displays their results.
     * Demonstrates polymorphism by accepting TestExecutor interface objects,
     * allowing different test implementations to be processed uniformly.
     * Provides real-time feedback during test execution.
     * 
     * @param tests a List of TestExecutor objects to execute sequentially
     */
    public static void executeAllTests(List<TestExecutor> tests) {
        for (TestExecutor test : tests) {
            System.out.println("Executing: " + test.getTestName());
            test.executeTest();
            System.out.println("Result: " + (test.isTestPassed() ? "PASSED" : "FAILED"));
            System.out.println("---");
        }
    }
}

// Test Suite class demonstrating composition
class TestSuite {
    private String suiteName;
    private List<BaseTest> tests;
    private Map<String, Object> configuration;
    
    /**
     * Constructs a new TestSuite with the specified name.
     * Initializes empty collections for tests and configuration settings,
     * creating a container for organizing and executing related tests.
     * 
     * @param suiteName the descriptive name for this test suite
     */
    public TestSuite(String suiteName) {
        this.suiteName = suiteName;
        this.tests = new ArrayList<>();
        this.configuration = new HashMap<>();
    }
    
    /**
     * Adds a test to this test suite for execution.
     * Allows building a collection of related tests that can be
     * executed together as a cohesive testing unit.
     * 
     * @param test the BaseTest object to add to this suite
     */
    public void addTest(BaseTest test) {
        tests.add(test);
    }
    
    /**
     * Executes all tests in this suite and provides comprehensive reporting.
     * Runs each test sequentially, displays individual test results,
     * and concludes with a summary of overall suite performance.
     * Provides formatted output for clear test execution tracking.
     */
    public void executeAllTests() {
        System.out.println("Executing Test Suite: " + suiteName);
        System.out.println("Total tests: " + tests.size());
        System.out.println("=".repeat(50));
        
        for (BaseTest test : tests) {
            test.executeTest();
            test.printTestResults();
            System.out.println("-".repeat(30));
        }
        
        printSummary();
    }
    
    /**
     * Prints a comprehensive summary of test suite execution results.
     * Calculates and displays statistics including total test count,
     * number of passed/failed tests, and overall success rate percentage.
     * This private method provides formatted summary reporting.
     */
    private void printSummary() {
        long passedTests = tests.stream().filter(BaseTest::isTestPassed).count();
        long failedTests = tests.size() - passedTests;
        
        System.out.println("=".repeat(50));
        System.out.println("Test Suite Summary:");
        System.out.println("Total Tests: " + tests.size());
        System.out.println("Passed: " + passedTests);
        System.out.println("Failed: " + failedTests);
        System.out.println("Success Rate: " + (passedTests * 100.0 / tests.size()) + "%");
    }
    
    /**
     * Sets a configuration parameter for this test suite.
     * Allows storing key-value pairs for test suite settings such as
     * browser type, timeouts, environment URLs, or other test parameters.
     * 
     * @param key the configuration parameter name
     * @param value the configuration parameter value (can be any Object type)
     */
    public void setConfiguration(String key, Object value) {
        configuration.put(key, value);
    }
    
    /**
     * Retrieves a configuration parameter value by its key.
     * Provides access to previously stored configuration settings
     * for use during test execution or setup.
     * 
     * @param key the configuration parameter name to retrieve
     * @return the configuration value associated with the key, or null if not found
     */
    public Object getConfiguration(String key) {
        return configuration.get(key);
    }
}

// Main class to demonstrate all concepts
public class OOPConcepts {
    
    /**
     * Main entry point for the OOP concepts demonstration program.
     * Executes the comprehensive demonstration of object-oriented programming
     * principles including inheritance, polymorphism, encapsulation, and abstraction.
     * 
     * @param args command line arguments (not used in this demonstration)
     */
    public static void main(String[] args) {
        demonstrateOOPConcepts();
    }
    /**
     * Demonstrates comprehensive object-oriented programming concepts through practical examples.
     * Showcases inheritance (BaseTest hierarchy), polymorphism (TestExecutor interface),
     * encapsulation (private fields with public accessors), abstraction (abstract methods),
     * composition (TestSuite containing tests), and modern Java features (streams, lambdas).
     * Creates and executes various test types to illustrate OOP principles in action.
     */
    public static void demonstrateOOPConcepts() {
        System.out.println("=== OOP Concepts Demonstration ===\n");
        
        // Create test suite
        TestSuite loginTestSuite = new TestSuite("Login Functionality Tests");
        
        // Add configuration
        loginTestSuite.setConfiguration("browser", "Chrome");
        loginTestSuite.setConfiguration("timeout", 30);
        
        // Create different types of tests (Polymorphism)
        BaseTest validLogin = new LoginTest("Valid Login Test", "qa_user", "password123");
        BaseTest invalidLogin = new LoginTest("Invalid Login Test", "invalid_user", "wrongpass");
        BaseTest dbTest = new DatabaseTest("User Count Test", "SELECT COUNT(*) FROM users", 5);
        
        // Add tests to suite
        loginTestSuite.addTest(validLogin);
        loginTestSuite.addTest(invalidLogin);
        loginTestSuite.addTest(dbTest);
        
        // Execute all tests
        loginTestSuite.executeAllTests();
        
        // Demonstrate utility methods and Java 8 features
        System.out.println("\n=== Utility Methods Demonstration ===");
        
        List<BaseTest> allTests = Arrays.asList(validLogin, invalidLogin, dbTest);
        // Filter passed tests using static method
        System.out.println("Passed tests: " + TestUtils.filterPassedTests(allTests).size());

        // Filter passed tests using lambda
        List<BaseTest> passedTests = TestUtils.filterTests(allTests, BaseTest::isTestPassed);
        System.out.println("Passed tests: " + passedTests.size());
        
        // Filter tests by name pattern
        List<BaseTest> loginTests = TestUtils.filterTestsByName(allTests, "Login");
        System.out.println("Login tests: " + loginTests.size());
        
        // Demonstrate polymorphism with interface
        List<TestExecutor> executors = new ArrayList<>(allTests);
        System.out.println("\n=== Polymorphism Demonstration ===");
        TestUtils.executeAllTests(executors);
    }
}

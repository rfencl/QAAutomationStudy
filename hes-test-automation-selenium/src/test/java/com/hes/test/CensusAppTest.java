package com.hes.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CensusAppTest {

    private static WebDriver driver;
    private final String fakeFirstName = "FAKE_FIRST_NAME";
    private final String fakeLastName = "FAKE_LAST_NAME";

    @BeforeAll
    public static void setUp() {
        // Please make sure to place the chromedriver executable in the drivers directory.
        System.setProperty("webdriver.chrome.driver", "drivers/chromedriver");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @Test
    @Order(1)
    public void testAddPersonToHouseholdUI() {
        driver.get("http://localhost:3000/");

        // This test assumes the user is already logged in.
        // As per README, the user should manually register and login before running the tests.

        // Navigate to the page to add a record.
        // The exact locator might need to be adjusted based on the actual application.
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Add Record"))).click();

        // Fill out the form
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName"))).sendKeys(fakeFirstName);
        driver.findElement(By.id("lastName")).sendKeys(fakeLastName);

        // Add other form fields as necessary. The locators are placeholders.
        // For example, for a relationship dropdown:
        // new Select(driver.findElement(By.id("relationship"))).selectByVisibleText("SELF");

        // Submit the form
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Verify that the record was added successfully
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("success-message")));
        assertTrue(successMessage.isDisplayed(), "Success message should be displayed after adding a record.");
    }

    @Test
    @Order(2)
    public void testVerifyPersonInAPI() {
        // This test assumes that the testAddPersonToHouseholdUI has been run and the record has been created.
        // The user email is required to get the records.
        // I will assume the user email is known.
        String userEmail = "testuser@example.com"; // This should be the email of the logged-in user.
        given(). 
            baseUri("http://localhost:3000/api").
        when().
            get("/record/user/email/" + userEmail).
        then().
            statusCode(200).
            body("find { it.firstName == '" + fakeFirstName + "' }.lastName", equalTo(fakeLastName));
    }

    @Test
    @Order(3)
    public void testVerifyPersonInDB() throws Exception {
        // This test assumes that the testAddPersonToHouseholdUI has been run and the record has been created.
        String dbUrl = "jdbc:postgresql://localhost:5432/census_db"; // Adjust if necessary
        String user = "user"; // Adjust if necessary
        String password = "password"; // Adjust if necessary

        try (Connection conn = DriverManager.getConnection(dbUrl, user, password)) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM \"Record\" WHERE \"firstName\" = '" + fakeFirstName + "' AND \"lastName\" = '" + fakeLastName + "'";
            ResultSet rs = stmt.executeQuery(sql);

            assertTrue(rs.next(), "Record should exist in the database.");
            assertEquals(fakeFirstName, rs.getString("firstName"), "First name in DB should match.");
            assertEquals(fakeLastName, rs.getString("lastName"), "Last name in DB should match.");
        }
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}

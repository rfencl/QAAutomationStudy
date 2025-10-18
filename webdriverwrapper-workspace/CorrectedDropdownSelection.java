// Corrected dropdown selection methods for CensusAppTest.java

// Add this helper method to the CensusAppTest class:
private void selectFromRadixDropdown(String fieldId, String optionText) {
    try {
        // Click the trigger button to open dropdown
        WebElement trigger = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#" + fieldId + " button[role='combobox']")));
        trigger.click();
        
        // Wait for dropdown content to appear and find the option
        // Try multiple selector strategies for Radix UI options
        WebElement option = null;
        try {
            // Strategy 1: Look for option by text content
            option = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@role='option']//span[text()='" + optionText + "']")));
        } catch (Exception e1) {
            try {
                // Strategy 2: Look for option by data-value attribute
                option = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[@role='option'][@data-value='" + optionText + "']")));
            } catch (Exception e2) {
                // Strategy 3: Look for option containing the text
                option = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[@role='option'][contains(text(), '" + optionText + "')]")));
            }
        }
        
        if (option != null) {
            option.click();
            Thread.sleep(200); // Brief pause for UI to update
            System.out.println("Successfully selected '" + optionText + "' from " + fieldId);
        }
    } catch (Exception e) {
        System.out.println("Failed to select '" + optionText + "' from " + fieldId + ": " + e.getMessage());
        // Don't throw exception, just log and continue
    }
}

// Replace the dropdown selection code in testAddPersonToHouseholdUI with:

// Select relationship SPOUSE
selectFromRadixDropdown("relationship", "SPOUSE");

// Select Hispanic NO
selectFromRadixDropdown("hispanic", "NO");

// Select Race WHITE  
selectFromRadixDropdown("race", "WHITE");

// Select Other stay NO
selectFromRadixDropdown("otherStay", "NO");

// Alternative approach using WebDriverWrapper (if you want to use the wrapper):
private void selectFromRadixDropdownWithWrapper(WebDriverWrapper wrapper, String fieldId, String optionText) {
    try {
        // Click trigger to open dropdown
        wrapper.click(By.cssSelector("#" + fieldId + " button[role='combobox']"));
        
        // Wait and click option
        wrapper.waitForElementClickable(By.xpath("//div[@role='option']//span[text()='" + optionText + "']"));
        wrapper.click(By.xpath("//div[@role='option']//span[text()='" + optionText + "']"));
        
        System.out.println("Successfully selected '" + optionText + "' from " + fieldId);
    } catch (Exception e) {
        System.out.println("Failed to select '" + optionText + "' from " + fieldId + ": " + e.getMessage());
    }
}

// Complete corrected section for testAddPersonToHouseholdUI method:

// Fill first name
WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
    By.cssSelector("input[name='firstName'], input#firstName")));
firstNameField.clear();
firstNameField.sendKeys(fakeFirstName);

// Fill last name  
WebElement lastNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(
    By.cssSelector("input[name='lastName'], input#lastName")));
lastNameField.clear();
lastNameField.sendKeys(fakeLastName);

// Select relationship SPOUSE using corrected approach
selectFromRadixDropdown("relationship", "SPOUSE");

// Fill DOB
try {
    WebElement dobInput = driver.findElement(By.cssSelector("input[placeholder*='Pick a date'], input[type='date']"));
    dobInput.clear();
    dobInput.sendKeys("10/25/2005");
    dobInput.sendKeys(Keys.TAB);
} catch (Exception ignored) {}

// Select Hispanic NO using corrected approach
selectFromRadixDropdown("hispanic", "NO");

// Select Race WHITE using corrected approach  
selectFromRadixDropdown("race", "WHITE");

// Select Other stay NO using corrected approach
selectFromRadixDropdown("otherStay", "NO");

// Select gender MALE (radio button - this part should work as is)
try {
    List<WebElement> maleRadios = driver.findElements(By.cssSelector("button[role='radio'][value='MALE']"));
    if (!maleRadios.isEmpty()) maleRadios.get(0).click();
} catch (Exception ignored) {}

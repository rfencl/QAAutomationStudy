# Dropdown Selection Issue Analysis

## Problem Identified

The test `testAddPersonToHouseholdUI` in `CensusAppTest.java` is failing to select values from dropdowns because it's using the wrong approach for Radix UI Select components.

## Current Problematic Code

```java
// Current approach - INCORRECT for Radix UI Select
WebElement relCombo = driver.findElement(By.cssSelector("#relationship button[role='combobox']"));
relCombo.click();
Thread.sleep(200);
relCombo.sendKeys("SPOUSE");  // This doesn't work with Radix UI Select
Thread.sleep(200);
relCombo.sendKeys(Keys.ENTER);
```

## Root Cause

The application uses **Radix UI Select components**, not traditional HTML select elements or comboboxes. These components:

1. Use a trigger button to open a dropdown portal
2. Render options in a separate DOM portal (outside the normal DOM tree)
3. Don't respond to `sendKeys()` for option selection
4. Require clicking on the actual option elements

## Correct Approach

For Radix UI Select components, you need to:

1. Click the trigger button to open the dropdown
2. Wait for the dropdown content to appear
3. Find and click the specific option element

## Fixed Code

```java
// CORRECT approach for Radix UI Select components
private void selectFromRadixDropdown(String fieldId, String optionText) {
    try {
        // Click the trigger button to open dropdown
        WebElement trigger = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("#" + fieldId + " button[role='combobox']")));
        trigger.click();
        
        // Wait for dropdown content to appear and find the option
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//div[@role='option']//span[text()='" + optionText + "']")));
        option.click();
        
        Thread.sleep(200); // Brief pause for UI to update
    } catch (Exception e) {
        System.out.println("Failed to select " + optionText + " from " + fieldId + ": " + e.getMessage());
    }
}

// Usage in test:
selectFromRadixDropdown("relationship", "SPOUSE");
selectFromRadixDropdown("hispanic", "NO");
selectFromRadixDropdown("race", "WHITE");
selectFromRadixDropdown("otherStay", "NO");
```

## Alternative Approach Using WebDriverWrapper

If using the WebDriverWrapper from this project:

```java
// Using WebDriverWrapper methods
wrapper.click(By.cssSelector("#relationship button[role='combobox']"));
wrapper.waitForElementClickable(By.xpath("//div[@role='option']//span[text()='SPOUSE']"));
wrapper.click(By.xpath("//div[@role='option']//span[text()='SPOUSE']"));
```

## Key Differences

| Traditional Select/Combobox | Radix UI Select |
|----------------------------|-----------------|
| `sendKeys()` works | `sendKeys()` doesn't work |
| Options are in DOM | Options in portal |
| `<select>` element | Custom button + portal |
| `role="combobox"` on input | `role="combobox"` on button |

## Additional Considerations

1. **Wait Strategy**: Always wait for dropdown content to appear before selecting options
2. **Portal Elements**: Radix UI renders dropdown content in a portal, so standard parent-child selectors may not work
3. **Accessibility**: Use `role="option"` to find option elements reliably
4. **Timing**: Add small delays after selections to allow UI state updates

## Recommended Test Structure

```java
@Test
public void testDropdownSelection() {
    // Navigate to form
    driver.get(BASE_URL + "/form-page");
    
    // Open add record dialog
    WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(
        By.xpath("//button[contains(text(), 'Add record')]")));
    addButton.click();
    
    // Fill form fields
    fillTextField("firstName", "John");
    fillTextField("lastName", "Doe");
    
    // Select dropdown values using correct approach
    selectFromRadixDropdown("relationship", "SPOUSE");
    selectFromRadixDropdown("hispanic", "NO");
    selectFromRadixDropdown("race", "WHITE");
    selectFromRadixDropdown("otherStay", "NO");
    
    // Submit form
    WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
        By.cssSelector("button[type='submit']")));
    submitButton.click();
    
    // Verify success
    // ... verification code
}
```

This approach will correctly handle Radix UI Select components and should resolve the dropdown selection failures.

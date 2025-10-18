# WebDriverWrapper Workspace - Enhanced WebDriver Utility

This workspace implements a comprehensive WebDriver wrapper utility that simplifies Selenium automation with built-in waits, error handling, and common operations, as outlined in Practice_Problem_3_WebDriver_Wrapper.md.

## 🎯 Features Implemented

### Core Components
- **WebDriverWrapper** - Main wrapper class with enhanced WebDriver functionality
- **WebDriverFactory** - Factory for creating WebDriver instances and wrappers
- **Comprehensive Test Suite** - Unit tests and real-world usage examples

### Key Features
- ✅ **Built-in Explicit Waits** - All operations include intelligent waiting
- ✅ **Simplified API** - Easy-to-use methods for common operations
- ✅ **Error Handling** - Graceful handling of common WebDriver exceptions
- ✅ **Multiple Browser Support** - Chrome and Firefox with headless options
- ✅ **Alert Handling** - Complete alert interaction methods
- ✅ **Frame & Window Management** - Easy switching between contexts
- ✅ **JavaScript Execution** - Built-in JavaScript execution capabilities

## 🚀 Quick Start

### Run All Tests
```bash
cd webdriverwrapper-workspace
mvn clean test
```

### Run Specific Test Class
```bash
mvn test -Dtest=WebDriverWrapperTest
```

## 📋 Usage Examples

### Basic Setup
```java
// Create wrapper with default settings
WebDriverWrapper wrapper = WebDriverFactory.createWrapper("chrome");

// Create wrapper with headless mode
WebDriverWrapper wrapper = WebDriverFactory.createWrapper("chrome", true);

// Create wrapper with custom timeout
WebDriver driver = WebDriverFactory.createDriver("chrome");
WebDriverWrapper wrapper = new WebDriverWrapper(driver, Duration.ofSeconds(15));
```

### Navigation and Basic Operations
```java
// Navigate to URL
wrapper.navigateTo("https://example.com");

// Get page information
String title = wrapper.getTitle();
String url = wrapper.getCurrentUrl();

// Find and interact with elements
wrapper.type(By.id("username"), "testuser");
wrapper.click(By.id("loginButton"));
```

### Advanced Element Interactions
```java
// Dropdown operations
wrapper.selectByText(By.id("dropdown"), "Option 1");
wrapper.selectByValue(By.id("dropdown"), "value1");
String selected = wrapper.getSelectedText(By.id("dropdown"));

// Checkbox operations
wrapper.check(By.id("checkbox1"));
wrapper.uncheck(By.id("checkbox2"));
boolean isChecked = wrapper.isChecked(By.id("checkbox1"));
```

### Alert Handling
```java
// Handle different types of alerts
wrapper.acceptAlert();
wrapper.dismissAlert();
String alertText = wrapper.getAlertText();
wrapper.typeInAlert("input text");
```

### Wait Operations
```java
// Various wait conditions
wrapper.waitForElementVisible(By.id("element"));
wrapper.waitForElementClickable(By.id("button"));
wrapper.waitForTextPresent(By.id("status"), "Complete");
wrapper.waitForElementInvisible(By.id("loading"));
```

## 🔧 WebDriverWrapper Methods

### Navigation Methods
- `navigateTo(String url)` - Navigate to URL
- `getCurrentUrl()` - Get current page URL
- `getTitle()` - Get page title
- `refresh()` - Refresh current page
- `back()` - Navigate back
- `forward()` - Navigate forward

### Element Finding (with built-in waits)
- `findElement(By locator)` - Find element with presence wait
- `findElements(By locator)` - Find multiple elements
- `findClickableElement(By locator)` - Find clickable element
- `findVisibleElement(By locator)` - Find visible element

### Click Operations
- `click(By locator)` - Click element with clickability wait
- `click(WebElement element)` - Click WebElement with wait

### Text Input Operations
- `type(By locator, String text)` - Clear and type text
- `append(By locator, String text)` - Append text without clearing
- `getText(By locator)` - Get element text
- `getAttribute(By locator, String attribute)` - Get attribute value

### Dropdown Operations
- `selectByText(By locator, String text)` - Select by visible text
- `selectByValue(By locator, String value)` - Select by value
- `selectByIndex(By locator, int index)` - Select by index
- `getSelectedText(By locator)` - Get selected option text

### Checkbox/Radio Operations
- `check(By locator)` - Check if not already checked
- `uncheck(By locator)` - Uncheck if checked
- `isChecked(By locator)` - Check selection state

### Wait Methods
- `waitForElementVisible(By locator)` - Wait for visibility
- `waitForElementClickable(By locator)` - Wait for clickability
- `waitForTextPresent(By locator, String text)` - Wait for text
- `waitForElementInvisible(By locator)` - Wait for invisibility

### Verification Methods
- `isElementPresent(By locator)` - Check element presence
- `isElementVisible(By locator)` - Check element visibility
- `isElementEnabled(By locator)` - Check element enabled state

### Alert Handling
- `acceptAlert()` - Accept alert with wait
- `dismissAlert()` - Dismiss alert with wait
- `getAlertText()` - Get alert text
- `typeInAlert(String text)` - Type in prompt alert

### Window/Frame Management
- `switchToWindow(String handle)` - Switch to specific window
- `switchToNewWindow()` - Switch to newly opened window
- `closeCurrentWindow()` - Close current window
- `switchToFrame(By locator)` - Switch to frame by locator
- `switchToFrame(int index)` - Switch to frame by index
- `switchToDefaultContent()` - Switch back to main content

### JavaScript Execution
- `executeScript(String script, Object... args)` - Execute JavaScript
- `scrollToElement(By locator)` - Scroll element into view

## 📊 Test Scenarios Covered

### WebDriverWrapperTest.java (Unit Tests)
1. **testNavigationMethods()** - Basic navigation functionality
2. **testElementFinding()** - Element location and verification
3. **testTextInput()** - Text input operations
4. **testClickAndLogin()** - Click operations and workflows
5. **testDropdownSelection()** - Dropdown interactions
6. **testCheckboxes()** - Checkbox operations
7. **testAlertHandling()** - Simple alert handling
8. **testConfirmAlert()** - Confirm alert handling
9. **testPromptAlert()** - Prompt alert handling
10. **testScrollToElement()** - JavaScript execution
11. **testWaitMethods()** - Wait condition testing

### WebDriverWrapperExampleTest.java (Real-world Scenarios)
1. **testCompleteLoginWorkflow()** - End-to-end login process
2. **testFormInteractionWorkflow()** - Complex form interactions
3. **testDynamicContentHandling()** - Dynamic content loading
4. **testAlertHandlingWorkflow()** - Complete alert scenarios
5. **testNavigationAndUtilityMethods()** - Navigation operations
6. **testScrollAndJavaScriptExecution()** - Advanced JavaScript usage

## ⚙️ Configuration

### Browser Support
```java
// Chrome (default)
WebDriverWrapper wrapper = WebDriverFactory.createWrapper("chrome");

// Firefox
WebDriverWrapper wrapper = WebDriverFactory.createWrapper("firefox");

// Headless mode
WebDriverWrapper wrapper = WebDriverFactory.createWrapper("chrome", true);
```

### Custom Timeout
```java
// Custom timeout (default is 10 seconds)
WebDriver driver = WebDriverFactory.createDriver("chrome");
WebDriverWrapper wrapper = new WebDriverWrapper(driver, Duration.ofSeconds(20));
```

## Loading test environment variables

You can keep test-specific environment variables in the project root file `test.env`. The repository includes a sample `test.env` with defaults for the local development environment.

To load these variables into your PowerShell session before running tests, run:

```powershell
Get-Content .\test.env | ForEach-Object {
	if ($_ -and ($_ -notmatch '^\s*#')) {
		$parts = $_ -split '=', 2
		if ($parts.Length -eq 2) {
			$name = $parts[0].Trim()
			$value = $parts[1].Trim()
			Write-Host "Setting $name"
			$env:$name = $value
		}
	}
}

# Then run tests (example)
mvn "-Dtest=com.hes.test.CensusAppTest" test
```

Notes:
- `EnvLoader` in `src/test/java/com/hes/test/util/EnvLoader.java` will also read `test.env` at test runtime, so loading into the PowerShell environment is optional but useful for running Maven directly from the shell.
- Modify `test.env` to point to your running app, API base and DB credentials.

## 📈 Expected Output

When running tests, you'll see detailed logging:
```
=== Complete Login Workflow Test ===
Navigated to: https://the-internet.herokuapp.com/login
Login workflow completed successfully!

=== Form Interaction Workflow Test ===
Selected Option 1 from dropdown
Selected Option 2 from dropdown
Checkbox interactions completed successfully!

=== Dynamic Content Handling Test ===
Started dynamic loading...
Dynamic content loaded successfully: Hello World!
```

## 🎓 Key Benefits

### Simplified API
- **Reduced Boilerplate** - No need to write explicit waits repeatedly
- **Intuitive Methods** - Method names clearly indicate functionality
- **Error Handling** - Built-in exception handling for common scenarios

### Enhanced Reliability
- **Automatic Waits** - All operations include appropriate wait conditions
- **Element State Verification** - Methods verify element state before interaction
- **Timeout Management** - Configurable timeouts for different scenarios

### Comprehensive Coverage
- **All Common Operations** - Covers 90% of typical automation needs
- **Advanced Features** - JavaScript execution, frame switching, window handling
- **Cross-browser Support** - Works with Chrome and Firefox

## 🔄 Extension Points

- Add support for more browsers (Edge, Safari)
- Implement custom wait conditions
- Add screenshot capture on failures
- Integrate with logging frameworks
- Add performance monitoring
- Implement element highlighting for debugging

## 🚀 Production Ready Features

- **Thread Safety** - Can be used in parallel test execution
- **Resource Management** - Proper cleanup with quit() method
- **Error Handling** - Graceful degradation for common failures
- **Configurable Timeouts** - Adaptable to different application needs
- **Cross-platform** - Works on Windows, macOS, and Linux

This WebDriverWrapper provides a production-ready foundation for Selenium automation projects, significantly reducing code complexity while improving reliability and maintainability.

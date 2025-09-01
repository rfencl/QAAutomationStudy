# PageObjectFactory Workspace - Factory Pattern for Page Object Management

This workspace implements a comprehensive Page Object Factory solution as outlined in Practice_Problem_7_Page_Object_Factory.md, demonstrating the Factory design pattern, caching mechanisms, and efficient page object management in Selenium automation.

## 🎯 Features Implemented

### Core Components
- **BasePage** - Abstract base class for all page objects with common functionality
- **PageObjectFactory** - Factory class with caching for efficient page object creation
- **WebDriverFactory** - Factory for creating WebDriver instances with different configurations
- **Page Objects** - HomePage, LoginPage, CheckboxesPage, DropdownPage with PageFactory annotations

### Key Features
- ✅ **Factory Pattern** - Centralized page object creation and management
- ✅ **Caching Mechanism** - Thread-safe caching to avoid duplicate page object creation
- ✅ **Generic Page Creation** - Type-safe generic method for creating any page object
- ✅ **Fluent Interface** - Method chaining for readable test code
- ✅ **Cross-Browser Support** - Chrome and Firefox with headless mode
- ✅ **PageFactory Integration** - Selenium PageFactory for element initialization

## 🚀 Quick Start

### Run All Tests
```bash
cd pageobjectfactory-workspace
mvn clean test
```

### Run Specific Test Classes
```bash
# Run factory pattern tests
mvn test -Dtest=PageObjectFactoryTest

# Run functional tests
mvn test -Dtest=LoginTest
mvn test -Dtest=CheckboxesTest
mvn test -Dtest=DropdownTest
```

### Run with Different Browsers
```bash
# Run with Firefox
mvn test -Dbrowser=firefox

# Run in visible mode (non-headless)
mvn test -Dheadless=false
```

## 📊 Page Object Factory Architecture

### 1. Factory Pattern Implementation
```java
public class PageObjectFactory {
    private final WebDriver driver;
    private final ConcurrentHashMap<Class<?>, Object> pageCache;
    
    @SuppressWarnings("unchecked")
    public <T extends BasePage> T getPage(Class<T> pageClass) {
        return (T) pageCache.computeIfAbsent(pageClass, this::createPage);
    }
    
    private Object createPage(Class<?> pageClass) {
        try {
            return pageClass.getConstructor(WebDriver.class).newInstance(driver);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create page object: " + pageClass.getSimpleName(), e);
        }
    }
}
```

### 2. Base Page with PageFactory
```java
public abstract class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
}
```

### 3. Page Object with Fluent Interface
```java
public class LoginPage extends BasePage {
    @FindBy(id = "username")
    private WebElement usernameField;
    
    @FindBy(id = "password")
    private WebElement passwordField;
    
    public LoginPage enterUsername(String username) {
        wait.until(ExpectedConditions.visibilityOf(usernameField));
        usernameField.clear();
        usernameField.sendKeys(username);
        return this;
    }
    
    public LoginPage enterPassword(String password) {
        passwordField.clear();
        passwordField.sendKeys(password);
        return this;
    }
}
```

### 4. Test Implementation
```java
public class LoginTest extends BaseTest {
    @Test
    public void testValidLogin() {
        driver.get("https://the-internet.herokuapp.com/");
        
        HomePage homePage = pageFactory.getHomePage();
        homePage.clickFormAuthentication();
        
        LoginPage loginPage = pageFactory.getLoginPage();
        loginPage.enterUsername("tomsmith")
                .enterPassword("SuperSecretPassword!")
                .clickLogin();
        
        Assert.assertTrue(loginPage.isLoginSuccessful());
    }
}
```

## 🔧 Core Implementation

### Factory Pattern with Caching
```java
public class PageObjectFactory {
    private final ConcurrentHashMap<Class<?>, Object> pageCache;
    
    public <T extends BasePage> T getPage(Class<T> pageClass) {
        return (T) pageCache.computeIfAbsent(pageClass, this::createPage);
    }
    
    // Convenience methods
    public HomePage getHomePage() {
        return getPage(HomePage.class);
    }
    
    public LoginPage getLoginPage() {
        return getPage(LoginPage.class);
    }
}
```

### WebDriver Factory
```java
public class WebDriverFactory {
    public static WebDriver createDriver(String browserName, boolean headless) {
        switch (browserName.toLowerCase()) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                if (headless) {
                    chromeOptions.addArguments("--headless");
                }
                return new ChromeDriver(chromeOptions);
                
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) {
                    firefoxOptions.addArguments("--headless");
                }
                return new FirefoxDriver(firefoxOptions);
        }
    }
}
```

### Page Objects with PageFactory
```java
public class CheckboxesPage extends BasePage {
    @FindBy(css = "input[type='checkbox']")
    private List<WebElement> checkboxes;
    
    @FindBy(css = "h3")
    private WebElement pageHeading;
    
    public void clickCheckbox(int index) {
        wait.until(ExpectedConditions.visibilityOfAllElements(checkboxes));
        if (index >= 0 && index < checkboxes.size()) {
            checkboxes.get(index).click();
        }
    }
    
    public boolean isCheckboxSelected(int index) {
        if (index >= 0 && index < checkboxes.size()) {
            return checkboxes.get(index).isSelected();
        }
        return false;
    }
}
```

### Dropdown Page with Select
```java
public class DropdownPage extends BasePage {
    @FindBy(id = "dropdown")
    private WebElement dropdownElement;
    
    public void selectByValue(String value) {
        wait.until(ExpectedConditions.visibilityOf(dropdownElement));
        Select dropdown = new Select(dropdownElement);
        dropdown.selectByValue(value);
    }
    
    public void selectByText(String text) {
        Select dropdown = new Select(dropdownElement);
        dropdown.selectByVisibleText(text);
    }
    
    public String getSelectedOption() {
        Select dropdown = new Select(dropdownElement);
        return dropdown.getFirstSelectedOption().getText();
    }
}
```

## 📈 Architecture Diagrams

### Class Diagram
The class diagram shows the relationships between core components:
- **BasePage** - Abstract base class with common functionality
- **Page Objects** - Concrete implementations extending BasePage
- **PageObjectFactory** - Factory with caching mechanism
- **WebDriverFactory** - WebDriver creation factory
- **Test Classes** - Test implementations using the factory

![Class Diagram](docs/class-diagram.puml)

### Sequence Diagram
The sequence diagram illustrates the complete lifecycle:
1. **Test Setup** - WebDriver and factory creation
2. **Page Object Creation** - First-time creation with caching
3. **Page Object Reuse** - Cached instance retrieval
4. **Page Interactions** - Element interactions and validations
5. **Cache Management** - Cache size monitoring and clearing
6. **Test Teardown** - Resource cleanup

![Sequence Diagram](docs/sequence-diagram.puml)

## 🧪 Test Coverage

### Factory Pattern Tests
```java
@Test
public void testPageObjectCreation() {
    HomePage homePage1 = pageFactory.getHomePage();
    HomePage homePage2 = pageFactory.getHomePage();
    
    // Should return same instance (cached)
    Assert.assertSame(homePage1, homePage2);
    Assert.assertEquals(pageFactory.getCacheSize(), 1);
}

@Test
public void testMultiplePageObjects() {
    HomePage homePage = pageFactory.getHomePage();
    LoginPage loginPage = pageFactory.getLoginPage();
    
    Assert.assertNotSame(homePage, loginPage);
    Assert.assertEquals(pageFactory.getCacheSize(), 2);
}
```

### Functional Tests
```java
@Test
public void testValidLogin() {
    HomePage homePage = pageFactory.getHomePage();
    homePage.clickFormAuthentication();
    
    LoginPage loginPage = pageFactory.getLoginPage();
    loginPage.enterUsername("tomsmith")
            .enterPassword("SuperSecretPassword!")
            .clickLogin();
    
    Assert.assertTrue(loginPage.isLoginSuccessful());
}

@Test
public void testCheckboxInteraction() {
    HomePage homePage = pageFactory.getHomePage();
    homePage.clickCheckboxes();
    
    CheckboxesPage checkboxesPage = pageFactory.getCheckboxesPage();
    Assert.assertFalse(checkboxesPage.isCheckboxSelected(0));
    
    checkboxesPage.clickCheckbox(0);
    Assert.assertTrue(checkboxesPage.isCheckboxSelected(0));
}
```

### Dropdown Tests
```java
@Test
public void testDropdownSelectionByValue() {
    HomePage homePage = pageFactory.getHomePage();
    homePage.clickDropdown();
    
    DropdownPage dropdownPage = pageFactory.getDropdownPage();
    dropdownPage.selectByValue("1");
    
    Assert.assertEquals(dropdownPage.getSelectedOption(), "Option 1");
}
```

## 📊 Key Features Demonstrated

### Factory Pattern Benefits
- **Centralized Creation** - Single point for page object instantiation
- **Caching Mechanism** - Avoid duplicate object creation
- **Type Safety** - Generic methods with compile-time type checking
- **Lazy Loading** - Objects created only when needed

### Page Object Model
- **Encapsulation** - Page-specific logic contained within page classes
- **Reusability** - Page objects can be reused across multiple tests
- **Maintainability** - Changes to UI require updates only in page objects
- **Readability** - Tests read like business workflows

### Selenium Integration
- **PageFactory** - Automatic element initialization with annotations
- **Explicit Waits** - Reliable element interaction strategies
- **Cross-Browser Support** - Chrome and Firefox compatibility
- **Headless Execution** - CI/CD friendly testing

### Test Organization
- **Base Test Class** - Common setup and teardown logic
- **Parameterized Tests** - Browser and headless mode configuration
- **Fluent Interface** - Method chaining for readable assertions
- **Comprehensive Coverage** - Factory, functional, and edge case testing

## 🔧 Configuration Options

### Browser Configuration
```xml
<!-- TestNG XML parameters -->
<parameter name="browser" value="chrome"/>
<parameter name="headless" value="true"/>
```

### WebDriver Options
```java
// Chrome with headless mode
ChromeOptions chromeOptions = new ChromeOptions();
chromeOptions.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");

// Firefox with headless mode
FirefoxOptions firefoxOptions = new FirefoxOptions();
firefoxOptions.addArguments("--headless");
```

### Factory Configuration
```java
// Generic page creation
HomePage homePage = pageFactory.getPage(HomePage.class);

// Convenience methods
LoginPage loginPage = pageFactory.getLoginPage();

// Cache management
pageFactory.clearCache();
int cacheSize = pageFactory.getCacheSize();
```

## 📈 Performance Benefits

### Caching Advantages
- **Memory Efficiency** - Single instance per page object type
- **Performance Optimization** - Avoid repeated object creation overhead
- **Thread Safety** - ConcurrentHashMap for concurrent access
- **Resource Management** - Controlled object lifecycle

### Factory Pattern Benefits
- **Loose Coupling** - Tests don't directly instantiate page objects
- **Flexibility** - Easy to change page object creation logic
- **Consistency** - Standardized object creation process
- **Extensibility** - Easy to add new page objects

## 🎓 Key Learning Points

### Design Patterns
- **Factory Pattern** - Centralized object creation with caching
- **Page Object Model** - Encapsulation of page-specific functionality
- **Fluent Interface** - Method chaining for readable code
- **Template Method** - Base class with common functionality

### Selenium Best Practices
- **PageFactory** - Annotation-based element initialization
- **Explicit Waits** - Reliable synchronization strategies
- **Element Encapsulation** - Private WebElement fields with public methods
- **Cross-Browser Testing** - Abstracted WebDriver creation

### Test Architecture
- **Base Test Class** - Common setup and teardown logic
- **Factory Integration** - Centralized page object management
- **Parameterized Configuration** - Flexible browser and mode selection
- **Comprehensive Testing** - Factory behavior and functional validation

### Code Quality
- **Type Safety** - Generic methods with compile-time checking
- **Exception Handling** - Graceful error management
- **Resource Management** - Proper WebDriver cleanup
- **Documentation** - Clear method names and comprehensive comments

## 🚀 Production Ready Features

### Reliability
- **Exception Handling** - Graceful failure management in page creation
- **Element Validation** - Explicit waits for element availability
- **Cross-Browser Support** - Chrome and Firefox compatibility
- **Headless Execution** - CI/CD pipeline integration

### Performance
- **Caching Mechanism** - Efficient object reuse
- **Lazy Loading** - Objects created only when needed
- **Memory Management** - Controlled object lifecycle
- **Thread Safety** - Concurrent access support

### Maintainability
- **Centralized Management** - Single factory for all page objects
- **Consistent Interface** - Standardized page object creation
- **Easy Extension** - Simple addition of new page objects
- **Clear Separation** - Factory, page objects, and tests are decoupled

### Testing
- **Comprehensive Coverage** - Factory behavior and functional tests
- **Multiple Scenarios** - Valid/invalid inputs, edge cases
- **Cross-Browser Testing** - Chrome and Firefox validation
- **Parameterized Execution** - Flexible test configuration

## 📝 Advanced Use Cases

### Custom Page Creation
```java
// Generic page creation with type safety
public <T extends BasePage> T getPage(Class<T> pageClass) {
    return (T) pageCache.computeIfAbsent(pageClass, this::createPage);
}

// Usage in tests
CheckboxesPage checkboxesPage = pageFactory.getPage(CheckboxesPage.class);
DropdownPage dropdownPage = pageFactory.getPage(DropdownPage.class);
```

### Cache Management
```java
// Monitor cache usage
System.out.println("Cache size: " + pageFactory.getCacheSize());

// Clear cache when needed
pageFactory.clearCache();

// Verify cache behavior
HomePage page1 = pageFactory.getHomePage();
HomePage page2 = pageFactory.getHomePage();
Assert.assertSame(page1, page2); // Same cached instance
```

### Fluent Interface Usage
```java
// Method chaining for readable tests
LoginPage loginPage = pageFactory.getLoginPage();
loginPage.enterUsername("tomsmith")
        .enterPassword("SuperSecretPassword!")
        .clickLogin();

// Validation
Assert.assertTrue(loginPage.isLoginSuccessful());
Assert.assertTrue(loginPage.getFlashMessage().contains("You logged into a secure area!"));
```

### Cross-Browser Testing
```java
// Chrome headless
WebDriver chromeDriver = WebDriverFactory.createDriver("chrome", true);
PageObjectFactory chromeFactory = new PageObjectFactory(chromeDriver);

// Firefox visible
WebDriver firefoxDriver = WebDriverFactory.createDriver("firefox", false);
PageObjectFactory firefoxFactory = new PageObjectFactory(firefoxDriver);
```

This PageObjectFactory workspace provides a comprehensive, production-ready solution for page object management, demonstrating the Factory design pattern, caching mechanisms, and efficient Selenium automation architecture with minimal code implementation.

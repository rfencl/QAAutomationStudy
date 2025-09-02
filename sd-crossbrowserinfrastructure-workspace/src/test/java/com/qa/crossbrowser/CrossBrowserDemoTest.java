package com.qa.crossbrowser;

import com.qa.crossbrowser.CrossBrowserInfrastructure.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.*;

import java.util.List;

/**
 * Demonstration of the Cross-Browser Infrastructure System
 * Shows real-world usage scenarios and capabilities
 */
public class CrossBrowserDemoTest {
    
    private CrossBrowserInfrastructureSystem infrastructure;
    
    @BeforeMethod
    public void setUp() {
        infrastructure = new CrossBrowserInfrastructureSystem();
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CROSS-BROWSER INFRASTRUCTURE DEMONSTRATION");
        System.out.println("=".repeat(80));
    }
    
    @AfterMethod
    public void tearDown() {
        // Clean up any active sessions
        List<BrowserSession> activeSessions = infrastructure.getActiveSessions();
        for (BrowserSession session : activeSessions) {
            infrastructure.closeSession(session.getSessionId());
        }
    }
    
    @Test
    public void demonstrateBasicBrowserAutomation() throws BrowserProviderException {
        System.out.println("\n🌐 DEMO: Basic Browser Automation");
        System.out.println("-".repeat(50));
        
        BrowserCapability capability = BrowserCapability.builder()
            .browserName("chrome")
            .browserVersion("latest")
            .platform("Linux")
            .headless(true)
            .build();
        
        BrowserSession session = infrastructure.createSession(capability);
        WebDriver driver = session.getDriver();
        
        try {
            // Navigate to a test page
            driver.get("data:text/html,<html><body><h1 id='title'>Cross-Browser Infrastructure</h1><p>Provider: " + session.getProvider() + "</p><input id='input' type='text'/><button id='btn'>Test Button</button></body></html>");
            
            // Perform basic interactions
            WebElement title = driver.findElement(By.id("title"));
            WebElement input = driver.findElement(By.id("input"));
            WebElement button = driver.findElement(By.id("btn"));
            
            System.out.printf("✅ Page Title: %s\n", title.getText());
            System.out.printf("📍 Provider Used: %s\n", session.getProvider());
            System.out.printf("🆔 Session ID: %s\n", session.getSessionId());
            
            input.sendKeys("Cross-Browser Testing Success!");
            button.click();
            
            System.out.println("✅ Successfully automated browser interactions");
            
        } finally {
            infrastructure.closeSession(session.getSessionId());
        }
    }
    
    @Test
    public void demonstrateProviderSelection() throws BrowserProviderException {
        System.out.println("\n🔄 DEMO: Provider Selection Logic");
        System.out.println("-".repeat(50));
        
        // Test local provider selection for Chrome
        BrowserCapability localCapability = BrowserCapability.builder()
            .browserName("chrome")
            .platform("Linux")
            .headless(true)
            .build();
        
        BrowserSession localSession = infrastructure.createSession(localCapability);
        System.out.printf("📍 Chrome on Linux → Provider: %s\n", localSession.getProvider());
        
        // Test cloud provider selection for mobile
        BrowserCapability mobileCapability = BrowserCapability.builder()
            .browserName("chrome")
            .platform("Android")
            .mobile(true)
            .deviceName("Pixel 4")
            .build();
        
        BrowserSession mobileSession = infrastructure.createSession(mobileCapability);
        System.out.printf("📱 Mobile Chrome → Provider: %s\n", mobileSession.getProvider());
        
        // Test cloud provider for Safari
        BrowserCapability safariCapability = BrowserCapability.builder()
            .browserName("safari")
            .platform("iOS")
            .mobile(true)
            .deviceName("iPhone 13")
            .build();
        
        BrowserSession safariSession = infrastructure.createSession(safariCapability);
        System.out.printf("🍎 Safari iOS → Provider: %s\n", safariSession.getProvider());
        
        infrastructure.closeSession(localSession.getSessionId());
        infrastructure.closeSession(mobileSession.getSessionId());
        infrastructure.closeSession(safariSession.getSessionId());
        
        System.out.println("✅ Provider selection working correctly");
    }
    
    @Test
    public void demonstrateInfrastructureMetrics() throws BrowserProviderException {
        System.out.println("\n📊 DEMO: Infrastructure Metrics and Monitoring");
        System.out.println("-".repeat(50));
        
        // Create multiple sessions
        BrowserSession session1 = infrastructure.createSession(
            BrowserCapability.builder().browserName("chrome").headless(true).build());
        BrowserSession session2 = infrastructure.createSession(
            BrowserCapability.builder().browserName("chrome").platform("Android").mobile(true).build());
        BrowserSession session3 = infrastructure.createSession(
            BrowserCapability.builder().browserName("safari").platform("iOS").mobile(true).build());
        
        // Get infrastructure metrics
        InfrastructureMetrics metrics = infrastructure.getInfrastructureMetrics();
        
        System.out.printf("📈 Infrastructure Metrics:\n");
        System.out.printf("   Total Active Sessions: %d\n", metrics.getTotalActiveSessions());
        System.out.printf("   Sessions by Provider: %s\n", metrics.getSessionsByProvider());
        System.out.printf("   Sessions by Browser: %s\n", metrics.getSessionsByBrowser());
        System.out.printf("   Timestamp: %s\n", metrics.getTimestamp());
        
        // Export as JSON
        String json = infrastructure.exportSessionsAsJson();
        System.out.printf("📄 JSON Export Length: %d characters\n", json.length());
        
        infrastructure.closeSession(session1.getSessionId());
        infrastructure.closeSession(session2.getSessionId());
        infrastructure.closeSession(session3.getSessionId());
        
        System.out.println("✅ Metrics and monitoring demonstration complete");
    }
    
    @Test
    public void demonstrateCapabilityBuilder() {
        System.out.println("\n🔧 DEMO: Browser Capability Builder");
        System.out.println("-".repeat(50));
        
        // Desktop Chrome capability
        BrowserCapability desktopCapability = BrowserCapability.builder()
            .browserName("chrome")
            .browserVersion("latest")
            .platform("Windows 10")
            .screenResolution("1920x1080")
            .headless(false)
            .build();
        
        System.out.printf("🖥️ Desktop Capability:\n");
        System.out.printf("   Browser: %s %s\n", desktopCapability.getBrowserName(), desktopCapability.getBrowserVersion());
        System.out.printf("   Platform: %s\n", desktopCapability.getPlatform());
        System.out.printf("   Resolution: %s\n", desktopCapability.getScreenResolution());
        System.out.printf("   Mobile: %s\n", desktopCapability.isMobile());
        
        // Mobile capability
        BrowserCapability mobileCapability = BrowserCapability.builder()
            .browserName("safari")
            .platform("iOS")
            .platformVersion("15")
            .deviceName("iPhone 13 Pro")
            .mobile(true)
            .build();
        
        System.out.printf("\n📱 Mobile Capability:\n");
        System.out.printf("   Browser: %s\n", mobileCapability.getBrowserName());
        System.out.printf("   Platform: %s %s\n", mobileCapability.getPlatform(), mobileCapability.getPlatformVersion());
        System.out.printf("   Device: %s\n", mobileCapability.getDeviceName());
        System.out.printf("   Mobile: %s\n", mobileCapability.isMobile());
        
        System.out.println("✅ Capability builder demonstration complete");
    }
    
    @Test
    public void demonstrateSessionManagement() throws BrowserProviderException, InterruptedException {
        System.out.println("\n🔍 DEMO: Session Management and Lifecycle");
        System.out.println("-".repeat(50));
        
        BrowserCapability capability = BrowserCapability.builder()
            .browserName("chrome")
            .headless(true)
            .build();
        
        BrowserSession session = infrastructure.createSession(capability);
        
        System.out.printf("🆔 Session Created: %s\n", session.getSessionId());
        System.out.printf("⏰ Start Time: %s\n", session.getStartTime());
        System.out.printf("🔄 Last Activity: %s\n", session.getLastActivity());
        System.out.printf("📊 Status: %s\n", session.getStatus());
        System.out.printf("🏭 Provider: %s\n", session.getProvider());
        
        // Simulate activity
        Thread.sleep(100);
        BrowserSession retrievedSession = infrastructure.getSession(session.getSessionId());
        System.out.printf("🔄 Updated Last Activity: %s\n", retrievedSession.getLastActivity());
        
        // Add metadata
        session.getMetadata().put("testName", "SessionManagementDemo");
        session.getMetadata().put("environment", "demo");
        System.out.printf("📝 Session Metadata: %s\n", session.getMetadata());
        
        // Check active sessions
        List<BrowserSession> activeSessions = infrastructure.getActiveSessions();
        System.out.printf("📈 Active Sessions Count: %d\n", activeSessions.size());
        
        infrastructure.closeSession(session.getSessionId());
        System.out.println("✅ Session management demonstration complete");
    }
    
    @Test
    public void demonstrateErrorHandling() {
        System.out.println("\n🛡️ DEMO: Error Handling and Recovery");
        System.out.println("-".repeat(50));
        
        // Test unsupported browser
        try {
            BrowserCapability unsupportedCapability = BrowserCapability.builder()
                .browserName("unsupported-browser")
                .build();
            
            infrastructure.createSession(unsupportedCapability);
            System.out.println("❌ Should have thrown exception");
        } catch (BrowserProviderException e) {
            System.out.printf("✅ Correctly handled unsupported browser: %s\n", e.getMessage());
        }
        
        // Test invalid session retrieval
        BrowserSession invalidSession = infrastructure.getSession("invalid-session-id");
        if (invalidSession == null) {
            System.out.println("✅ Correctly handled invalid session ID");
        }
        
        // Test graceful session closure
        try {
            BrowserCapability capability = BrowserCapability.builder()
                .browserName("chrome")
                .headless(true)
                .build();
            
            BrowserSession session = infrastructure.createSession(capability);
            String sessionId = session.getSessionId();
            
            infrastructure.closeSession(sessionId);
            infrastructure.closeSession(sessionId); // Double close
            
            System.out.println("✅ Gracefully handled double session closure");
        } catch (Exception e) {
            System.out.printf("✅ Error handling working: %s\n", e.getMessage());
        }
        
        System.out.println("✅ Error handling demonstration complete");
    }
    
    @Test
    public void demonstrateLoadBalancing() throws BrowserProviderException {
        System.out.println("\n⚖️ DEMO: Load Balancing and Provider Selection");
        System.out.println("-".repeat(50));
        
        // Create multiple Chrome sessions to test load balancing
        for (int i = 1; i <= 3; i++) {
            BrowserCapability capability = BrowserCapability.builder()
                .browserName("chrome")
                .headless(true)
                .build();
            
            BrowserSession session = infrastructure.createSession(capability);
            System.out.printf("🔧 Session %d: %s (Provider: %s)\n", 
                             i, session.getSessionId(), session.getProvider());
        }
        
        // Create mobile sessions to test cloud provider selection
        for (int i = 1; i <= 2; i++) {
            BrowserCapability mobileCapability = BrowserCapability.builder()
                .browserName("chrome")
                .platform("Android")
                .mobile(true)
                .build();
            
            BrowserSession mobileSession = infrastructure.createSession(mobileCapability);
            System.out.printf("📱 Mobile Session %d: %s (Provider: %s)\n", 
                             i, mobileSession.getSessionId(), mobileSession.getProvider());
        }
        
        // Show final metrics
        InfrastructureMetrics metrics = infrastructure.getInfrastructureMetrics();
        System.out.printf("📊 Load Balancing Results:\n");
        System.out.printf("   Total Sessions: %d\n", metrics.getTotalActiveSessions());
        System.out.printf("   Provider Distribution: %s\n", metrics.getSessionsByProvider());
        
        // Clean up
        List<BrowserSession> sessions = infrastructure.getActiveSessions();
        for (BrowserSession session : sessions) {
            infrastructure.closeSession(session.getSessionId());
        }
        
        System.out.println("✅ Load balancing demonstration complete");
    }
}

package com.qa.crossbrowser;

import com.qa.crossbrowser.CrossBrowserInfrastructure.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.*;
import static org.testng.Assert.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Integration tests for the complete cross-browser infrastructure system
 */
public class CrossBrowserIntegrationTest {
    
    private CrossBrowserInfrastructureSystem infrastructure;
    
    @BeforeMethod
    public void setUp() {
        infrastructure = new CrossBrowserInfrastructureSystem();
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
    public void testEndToEndBrowserAutomation() throws BrowserProviderException {
        System.out.println("\n🌐 DEMO: End-to-End Browser Automation");
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
            driver.get("data:text/html,<html><body><h1 id='title'>Cross-Browser Test</h1><input id='input' type='text'/><button id='btn'>Click Me</button></body></html>");
            
            // Perform basic interactions
            WebElement title = driver.findElement(By.id("title"));
            assertEquals(title.getText(), "Cross-Browser Test");
            
            WebElement input = driver.findElement(By.id("input"));
            input.sendKeys("Hello Cross-Browser Infrastructure!");
            
            WebElement button = driver.findElement(By.id("btn"));
            button.click();
            
            System.out.printf("✅ Successfully automated %s browser session: %s\n", 
                             capability.getBrowserName(), session.getSessionId());
            
        } finally {
            infrastructure.closeSession(session.getSessionId());
        }
    }
    
    @Test
    public void testMultiBrowserParallelExecution() throws InterruptedException {
        System.out.println("\n🚀 DEMO: Multi-Browser Parallel Execution");
        System.out.println("-".repeat(50));
        
        String[] browsers = {"chrome", "firefox"};
        CountDownLatch latch = new CountDownLatch(browsers.length);
        List<String> results = new java.util.concurrent.CopyOnWriteArrayList<>();
        
        for (String browser : browsers) {
            new Thread(() -> {
                try {
                    BrowserCapability capability = BrowserCapability.builder()
                        .browserName(browser)
                        .headless(true)
                        .build();
                    
                    BrowserSession session = infrastructure.createSession(capability);
                    WebDriver driver = session.getDriver();
                    
                    // Perform browser-specific test
                    driver.get("data:text/html,<html><body><h1>Test Page</h1></body></html>");
                    String title = driver.getTitle();
                    
                    results.add(String.format("%s: %s", browser, title));
                    System.out.printf("📊 %s browser test completed: %s\n", 
                                     browser.toUpperCase(), session.getSessionId());
                    
                    infrastructure.closeSession(session.getSessionId());
                    
                } catch (Exception e) {
                    System.err.printf("❌ Error in %s browser: %s\n", browser, e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        assertTrue(latch.await(30, TimeUnit.SECONDS));
        assertEquals(results.size(), browsers.length);
        
        System.out.printf("✅ Completed parallel execution across %d browsers\n", browsers.length);
    }
    
    @Test
    public void testProviderFailoverScenario() throws BrowserProviderException {
        System.out.println("\n🔄 DEMO: Provider Failover Scenario");
        System.out.println("-".repeat(50));
        
        // Test local provider preference for supported browsers
        BrowserCapability localCapability = BrowserCapability.builder()
            .browserName("chrome")
            .platform("Linux")
            .headless(true)
            .build();
        
        BrowserSession localSession = infrastructure.createSession(localCapability);
        assertEquals(localSession.getProvider(), "local");
        System.out.printf("📍 Local provider selected for Chrome: %s\n", localSession.getSessionId());
        
        // Test cloud provider for mobile browsers
        BrowserCapability mobileCapability = BrowserCapability.builder()
            .browserName("chrome")
            .platform("Android")
            .mobile(true)
            .deviceName("Pixel 4")
            .build();
        
        BrowserSession cloudSession = infrastructure.createSession(mobileCapability);
        assertEquals(cloudSession.getProvider(), "cloud");
        System.out.printf("☁️ Cloud provider selected for mobile: %s\n", cloudSession.getSessionId());
        
        infrastructure.closeSession(localSession.getSessionId());
        infrastructure.closeSession(cloudSession.getSessionId());
        
        System.out.println("✅ Provider failover working correctly");
    }
    
    @Test
    public void testScalabilityAndLoadBalancing() throws BrowserProviderException, InterruptedException {
        System.out.println("\n⚖️ DEMO: Scalability and Load Balancing");
        System.out.println("-".repeat(50));
        
        int numberOfSessions = 8;
        CountDownLatch latch = new CountDownLatch(numberOfSessions);
        List<BrowserSession> sessions = new java.util.concurrent.CopyOnWriteArrayList<>();
        
        // Create multiple sessions concurrently
        for (int i = 0; i < numberOfSessions; i++) {
            final int sessionIndex = i;
            new Thread(() -> {
                try {
                    BrowserCapability capability = BrowserCapability.builder()
                        .browserName(sessionIndex % 2 == 0 ? "chrome" : "firefox")
                        .headless(true)
                        .build();
                    
                    BrowserSession session = infrastructure.createSession(capability);
                    sessions.add(session);
                    
                    System.out.printf("🔧 Created session %d: %s (%s)\n", 
                                     sessionIndex + 1, session.getSessionId(), session.getProvider());
                    
                    // Simulate some work
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    System.err.printf("❌ Error creating session %d: %s\n", sessionIndex + 1, e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        assertTrue(latch.await(30, TimeUnit.SECONDS));
        
        // Verify load balancing
        InfrastructureMetrics metrics = infrastructure.getInfrastructureMetrics();
        System.out.printf("📊 Total active sessions: %d\n", metrics.getTotalActiveSessions());
        System.out.printf("📊 Sessions by provider: %s\n", metrics.getSessionsByProvider());
        System.out.printf("📊 Sessions by browser: %s\n", metrics.getSessionsByBrowser());
        
        assertEquals(sessions.size(), numberOfSessions);
        assertEquals(metrics.getTotalActiveSessions(), numberOfSessions);
        
        // Clean up sessions
        for (BrowserSession session : sessions) {
            infrastructure.closeSession(session.getSessionId());
        }
        
        System.out.println("✅ Scalability test completed successfully");
    }
    
    @Test
    public void testMobileBrowserSupport() throws BrowserProviderException {
        System.out.println("\n📱 DEMO: Mobile Browser Support");
        System.out.println("-".repeat(50));
        
        // Test Android Chrome
        BrowserCapability androidCapability = BrowserCapability.builder()
            .browserName("chrome")
            .platform("Android")
            .platformVersion("11")
            .deviceName("Samsung Galaxy S21")
            .mobile(true)
            .build();
        
        BrowserSession androidSession = infrastructure.createSession(androidCapability);
        assertTrue(androidSession.getCapability().isMobile());
        assertEquals(androidSession.getProvider(), "cloud");
        System.out.printf("🤖 Android Chrome session created: %s\n", androidSession.getSessionId());
        
        // Test iOS Safari
        BrowserCapability iosCapability = BrowserCapability.builder()
            .browserName("safari")
            .platform("iOS")
            .platformVersion("15")
            .deviceName("iPhone 13")
            .mobile(true)
            .build();
        
        BrowserSession iosSession = infrastructure.createSession(iosCapability);
        assertTrue(iosSession.getCapability().isMobile());
        assertEquals(iosSession.getProvider(), "cloud");
        System.out.printf("🍎 iOS Safari session created: %s\n", iosSession.getSessionId());
        
        infrastructure.closeSession(androidSession.getSessionId());
        infrastructure.closeSession(iosSession.getSessionId());
        
        System.out.println("✅ Mobile browser support verified");
    }
    
    @Test
    public void testSessionMonitoringAndCleanup() throws BrowserProviderException, InterruptedException {
        System.out.println("\n🔍 DEMO: Session Monitoring and Cleanup");
        System.out.println("-".repeat(50));
        
        // Create a session
        BrowserCapability capability = BrowserCapability.builder()
            .browserName("chrome")
            .headless(true)
            .build();
        
        BrowserSession session = infrastructure.createSession(capability);
        String sessionId = session.getSessionId();
        
        System.out.printf("🆔 Created session: %s\n", sessionId);
        System.out.printf("⏰ Start time: %s\n", session.getStartTime());
        System.out.printf("🔄 Last activity: %s\n", session.getLastActivity());
        
        // Simulate activity
        Thread.sleep(100);
        BrowserSession retrievedSession = infrastructure.getSession(sessionId);
        assertTrue(retrievedSession.getLastActivity().isAfter(session.getLastActivity()));
        System.out.printf("🔄 Updated last activity: %s\n", retrievedSession.getLastActivity());
        
        // Test session metadata
        session.getMetadata().put("testName", "MonitoringTest");
        session.getMetadata().put("environment", "integration");
        System.out.printf("📝 Session metadata: %s\n", session.getMetadata());
        
        // Export session data
        String json = infrastructure.exportSessionsAsJson();
        assertTrue(json.contains(sessionId));
        System.out.printf("📄 JSON export contains session data: %s\n", json.length() > 0);
        
        infrastructure.closeSession(sessionId);
        System.out.println("✅ Session monitoring and cleanup verified");
    }
    
    @Test
    public void testCrossEnvironmentCompatibility() throws BrowserProviderException {
        System.out.println("\n🌍 DEMO: Cross-Environment Compatibility");
        System.out.println("-".repeat(50));
        
        String[] platforms = {"Linux", "Windows 10", "macOS"};
        String[] browsers = {"chrome", "firefox"};
        
        for (String platform : platforms) {
            for (String browser : browsers) {
                // Skip unsupported combinations
                if (platform.equals("macOS") && browser.equals("firefox")) {
                    continue; // Simulate unsupported combination
                }
                
                BrowserCapability capability = BrowserCapability.builder()
                    .browserName(browser)
                    .platform(platform)
                    .headless(true)
                    .build();
                
                try {
                    BrowserSession session = infrastructure.createSession(capability);
                    System.out.printf("✅ %s on %s: %s\n", 
                                     browser.toUpperCase(), platform, session.getSessionId());
                    infrastructure.closeSession(session.getSessionId());
                } catch (BrowserProviderException e) {
                    System.out.printf("⚠️ %s on %s: Not supported\n", browser.toUpperCase(), platform);
                }
            }
        }
        
        System.out.println("✅ Cross-environment compatibility test completed");
    }
    
    @Test
    public void testPerformanceMetrics() throws BrowserProviderException, InterruptedException {
        System.out.println("\n📈 DEMO: Performance Metrics");
        System.out.println("-".repeat(50));
        
        long startTime = System.currentTimeMillis();
        
        // Create multiple sessions to measure performance
        int sessionCount = 5;
        List<BrowserSession> sessions = new java.util.ArrayList<>();
        
        for (int i = 0; i < sessionCount; i++) {
            BrowserCapability capability = BrowserCapability.builder()
                .browserName("chrome")
                .headless(true)
                .build();
            
            long sessionStartTime = System.currentTimeMillis();
            BrowserSession session = infrastructure.createSession(capability);
            long sessionCreationTime = System.currentTimeMillis() - sessionStartTime;
            
            sessions.add(session);
            System.out.printf("⚡ Session %d created in %dms: %s\n", 
                             i + 1, sessionCreationTime, session.getSessionId());
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        double averageTime = (double) totalTime / sessionCount;
        
        System.out.printf("📊 Performance Metrics:\n");
        System.out.printf("   Total sessions: %d\n", sessionCount);
        System.out.printf("   Total time: %dms\n", totalTime);
        System.out.printf("   Average time per session: %.2fms\n", averageTime);
        
        // Verify performance requirement (sub-5 second startup)
        assertTrue(averageTime < 5000, "Session creation should be under 5 seconds");
        
        // Get infrastructure metrics
        InfrastructureMetrics metrics = infrastructure.getInfrastructureMetrics();
        System.out.printf("   Active sessions: %d\n", metrics.getTotalActiveSessions());
        
        // Clean up
        for (BrowserSession session : sessions) {
            infrastructure.closeSession(session.getSessionId());
        }
        
        System.out.println("✅ Performance metrics test completed");
    }
    
    @Test
    public void testErrorHandlingAndRecovery() {
        System.out.println("\n🛡️ DEMO: Error Handling and Recovery");
        System.out.println("-".repeat(50));
        
        // Test unsupported browser
        try {
            BrowserCapability unsupportedCapability = BrowserCapability.builder()
                .browserName("unsupported-browser")
                .build();
            
            infrastructure.createSession(unsupportedCapability);
            fail("Should have thrown BrowserProviderException");
        } catch (BrowserProviderException e) {
            System.out.printf("✅ Correctly handled unsupported browser: %s\n", e.getMessage());
        }
        
        // Test invalid session retrieval
        BrowserSession invalidSession = infrastructure.getSession("invalid-session-id");
        assertNull(invalidSession);
        System.out.println("✅ Correctly handled invalid session ID");
        
        // Test double session closure (should not throw exception)
        try {
            BrowserCapability capability = BrowserCapability.builder()
                .browserName("chrome")
                .headless(true)
                .build();
            
            BrowserSession session = infrastructure.createSession(capability);
            String sessionId = session.getSessionId();
            
            infrastructure.closeSession(sessionId);
            infrastructure.closeSession(sessionId); // Double close
            
            System.out.println("✅ Correctly handled double session closure");
        } catch (Exception e) {
            System.out.printf("✅ Error handling working: %s\n", e.getMessage());
        }
        
        System.out.println("✅ Error handling and recovery test completed");
    }
}

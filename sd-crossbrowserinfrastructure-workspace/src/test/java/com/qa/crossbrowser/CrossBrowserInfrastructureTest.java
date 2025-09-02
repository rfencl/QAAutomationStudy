package com.qa.crossbrowser;

import com.qa.crossbrowser.CrossBrowserInfrastructure.*;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.*;
import static org.testng.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive test suite for Cross-Browser Infrastructure System
 */
public class CrossBrowserInfrastructureTest {
    
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
    public void testBasicSessionCreation() throws BrowserProviderException {
        BrowserCapability capability = BrowserCapability.builder()
            .browserName("chrome")
            .browserVersion("latest")
            .platform("Linux")
            .headless(true)
            .build();
        
        BrowserSession session = infrastructure.createSession(capability);
        
        assertNotNull(session);
        assertNotNull(session.getSessionId());
        assertNotNull(session.getDriver());
        assertEquals(session.getCapability().getBrowserName(), "chrome");
        assertEquals(session.getStatus(), SessionStatus.ACTIVE);
        
        // Verify session is tracked
        List<BrowserSession> activeSessions = infrastructure.getActiveSessions();
        assertEquals(activeSessions.size(), 1);
        assertEquals(activeSessions.get(0).getSessionId(), session.getSessionId());
        
        infrastructure.closeSession(session.getSessionId());
    }
    
    @Test
    public void testMultipleBrowserSupport() throws BrowserProviderException {
        // Test Chrome
        BrowserCapability chromeCapability = BrowserCapability.builder()
            .browserName("chrome")
            .headless(true)
            .build();
        
        BrowserSession chromeSession = infrastructure.createSession(chromeCapability);
        assertNotNull(chromeSession);
        assertEquals(chromeSession.getCapability().getBrowserName(), "chrome");
        
        // Test Firefox
        BrowserCapability firefoxCapability = BrowserCapability.builder()
            .browserName("firefox")
            .headless(true)
            .build();
        
        BrowserSession firefoxSession = infrastructure.createSession(firefoxCapability);
        assertNotNull(firefoxSession);
        assertEquals(firefoxSession.getCapability().getBrowserName(), "firefox");
        
        // Verify both sessions are active
        List<BrowserSession> activeSessions = infrastructure.getActiveSessions();
        assertEquals(activeSessions.size(), 2);
        
        infrastructure.closeSession(chromeSession.getSessionId());
        infrastructure.closeSession(firefoxSession.getSessionId());
    }
    
    @Test
    public void testProviderSelection() throws BrowserProviderException {
        // Test local provider selection for supported browsers
        BrowserCapability localCapability = BrowserCapability.builder()
            .browserName("chrome")
            .platform("Linux")
            .headless(true)
            .build();
        
        BrowserSession localSession = infrastructure.createSession(localCapability);
        assertEquals(localSession.getProvider(), "local");
        
        // Test cloud provider selection for mobile browsers
        BrowserCapability mobileCapability = BrowserCapability.builder()
            .browserName("chrome")
            .platform("Android")
            .mobile(true)
            .deviceName("Pixel 4")
            .build();
        
        BrowserSession mobileSession = infrastructure.createSession(mobileCapability);
        assertEquals(mobileSession.getProvider(), "cloud");
        
        infrastructure.closeSession(localSession.getSessionId());
        infrastructure.closeSession(mobileSession.getSessionId());
    }
    
    @Test
    public void testSessionManagement() throws BrowserProviderException {
        BrowserCapability capability = BrowserCapability.builder()
            .browserName("chrome")
            .headless(true)
            .build();
        
        BrowserSession session = infrastructure.createSession(capability);
        String sessionId = session.getSessionId();
        
        // Test session retrieval
        BrowserSession retrievedSession = infrastructure.getSession(sessionId);
        assertNotNull(retrievedSession);
        assertEquals(retrievedSession.getSessionId(), sessionId);
        
        // Test session closure
        infrastructure.closeSession(sessionId);
        
        // Verify session is no longer active
        List<BrowserSession> activeSessions = infrastructure.getActiveSessions();
        assertTrue(activeSessions.stream().noneMatch(s -> s.getSessionId().equals(sessionId)));
    }
    
    @Test
    public void testConcurrentSessionCreation() throws InterruptedException {
        int numberOfThreads = 5;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        List<BrowserSession> createdSessions = new java.util.concurrent.CopyOnWriteArrayList<>();
        
        // Create sessions concurrently
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    BrowserCapability capability = BrowserCapability.builder()
                        .browserName("chrome")
                        .headless(true)
                        .build();
                    
                    BrowserSession session = infrastructure.createSession(capability);
                    createdSessions.add(session);
                    
                    // Simulate some work
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        // Wait for all threads to complete
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        
        // Verify all sessions were created
        assertEquals(createdSessions.size(), numberOfThreads);
        assertEquals(infrastructure.getActiveSessions().size(), numberOfThreads);
        
        // Clean up sessions
        for (BrowserSession session : createdSessions) {
            infrastructure.closeSession(session.getSessionId());
        }
    }
    
    @Test
    public void testCapabilityBuilder() {
        BrowserCapability capability = BrowserCapability.builder()
            .browserName("chrome")
            .browserVersion("95.0")
            .platform("Windows 10")
            .screenResolution("1920x1080")
            .headless(false)
            .mobile(false)
            .build();
        
        assertEquals(capability.getBrowserName(), "chrome");
        assertEquals(capability.getBrowserVersion(), "95.0");
        assertEquals(capability.getPlatform(), "Windows 10");
        assertEquals(capability.getScreenResolution(), "1920x1080");
        assertFalse(capability.isHeadless());
        assertFalse(capability.isMobile());
    }
    
    @Test
    public void testMobileCapabilities() throws BrowserProviderException {
        BrowserCapability mobileCapability = BrowserCapability.builder()
            .browserName("chrome")
            .platform("Android")
            .platformVersion("11")
            .deviceName("Samsung Galaxy S21")
            .mobile(true)
            .build();
        
        assertTrue(mobileCapability.isMobile());
        assertEquals(mobileCapability.getDeviceName(), "Samsung Galaxy S21");
        
        BrowserSession mobileSession = infrastructure.createSession(mobileCapability);
        assertNotNull(mobileSession);
        assertTrue(mobileSession.getCapability().isMobile());
        
        infrastructure.closeSession(mobileSession.getSessionId());
    }
    
    @Test
    public void testProviderMetrics() throws BrowserProviderException {
        // Create some sessions to generate metrics
        BrowserSession session1 = infrastructure.createSession(
            BrowserCapability.builder().browserName("chrome").headless(true).build());
        BrowserSession session2 = infrastructure.createSession(
            BrowserCapability.builder().browserName("firefox").headless(true).build());
        
        InfrastructureMetrics metrics = infrastructure.getInfrastructureMetrics();
        
        assertEquals(metrics.getTotalActiveSessions(), 2);
        assertTrue(metrics.getSessionsByBrowser().containsKey("chrome"));
        assertTrue(metrics.getSessionsByBrowser().containsKey("firefox"));
        assertTrue(metrics.getSessionsByProvider().containsKey("local"));
        
        infrastructure.closeSession(session1.getSessionId());
        infrastructure.closeSession(session2.getSessionId());
    }
    
    @Test
    public void testJsonExport() throws BrowserProviderException {
        BrowserSession session = infrastructure.createSession(
            BrowserCapability.builder().browserName("chrome").headless(true).build());
        
        String json = infrastructure.exportSessionsAsJson();
        
        assertNotNull(json);
        assertFalse(json.isEmpty());
        assertTrue(json.contains("sessionId"));
        assertTrue(json.contains("chrome"));
        
        infrastructure.closeSession(session.getSessionId());
    }
    
    @Test
    public void testSessionLifecycle() throws BrowserProviderException, InterruptedException {
        BrowserCapability capability = BrowserCapability.builder()
            .browserName("chrome")
            .headless(true)
            .build();
        
        BrowserSession session = infrastructure.createSession(capability);
        
        // Verify initial state
        assertEquals(session.getStatus(), SessionStatus.ACTIVE);
        assertNotNull(session.getStartTime());
        assertNotNull(session.getLastActivity());
        
        // Simulate activity
        Thread.sleep(100);
        BrowserSession retrievedSession = infrastructure.getSession(session.getSessionId());
        assertTrue(retrievedSession.getLastActivity().isAfter(session.getLastActivity()));
        
        // Close session
        infrastructure.closeSession(session.getSessionId());
        
        // Verify session is terminated
        List<BrowserSession> activeSessions = infrastructure.getActiveSessions();
        assertTrue(activeSessions.stream().noneMatch(s -> s.getSessionId().equals(session.getSessionId())));
    }
    
    @Test
    public void testLoadBalancing() throws BrowserProviderException {
        // Create multiple sessions to test load balancing
        for (int i = 0; i < 3; i++) {
            BrowserCapability capability = BrowserCapability.builder()
                .browserName("chrome")
                .headless(true)
                .build();
            
            BrowserSession session = infrastructure.createSession(capability);
            assertNotNull(session);
            
            // All should use local provider for Chrome
            assertEquals(session.getProvider(), "local");
        }
        
        // Clean up
        List<BrowserSession> sessions = infrastructure.getActiveSessions();
        for (BrowserSession session : sessions) {
            infrastructure.closeSession(session.getSessionId());
        }
    }
    
    @Test(expectedExceptions = BrowserProviderException.class)
    public void testUnsupportedBrowser() throws BrowserProviderException {
        BrowserCapability capability = BrowserCapability.builder()
            .browserName("unsupported-browser")
            .build();
        
        infrastructure.createSession(capability);
    }
    
    @Test
    public void testSessionMetadata() throws BrowserProviderException {
        BrowserCapability capability = BrowserCapability.builder()
            .browserName("chrome")
            .headless(true)
            .build();
        
        BrowserSession session = infrastructure.createSession(capability);
        
        // Test metadata manipulation
        session.getMetadata().put("testName", "CrossBrowserTest");
        session.getMetadata().put("environment", "staging");
        
        assertEquals(session.getMetadata().get("testName"), "CrossBrowserTest");
        assertEquals(session.getMetadata().get("environment"), "staging");
        
        infrastructure.closeSession(session.getSessionId());
    }
}

package com.qa.crossbrowser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Cross-Browser Testing Infrastructure System
 * 
 * Design Decisions:
 * 1. Provider abstraction for multiple browser sources (local, cloud, mobile)
 * 2. Session management with automatic cleanup and monitoring
 * 3. Load balancing based on provider metrics and capabilities
 * 4. Configuration-driven browser matrix support
 * 5. Thread-safe concurrent session management
 */
public class CrossBrowserInfrastructure {
    
    private static final Logger logger = LoggerFactory.getLogger(CrossBrowserInfrastructure.class);
    
    // Core Models
    public static class BrowserCapability {
        private String browserName;
        private String browserVersion;
        private String platform;
        private String platformVersion;
        private String screenResolution;
        private String deviceName;
        private boolean mobile;
        private boolean headless;
        private Map<String, Object> additionalCapabilities;
        
        public BrowserCapability(String browserName, String browserVersion, String platform, 
                               String platformVersion, String screenResolution, String deviceName,
                               boolean mobile, boolean headless, Map<String, Object> additionalCapabilities) {
            this.browserName = browserName;
            this.browserVersion = browserVersion;
            this.platform = platform;
            this.platformVersion = platformVersion;
            this.screenResolution = screenResolution;
            this.deviceName = deviceName;
            this.mobile = mobile;
            this.headless = headless;
            this.additionalCapabilities = additionalCapabilities != null ? additionalCapabilities : new HashMap<>();
        }
        
        // Getters
        public String getBrowserName() { return browserName; }
        public String getBrowserVersion() { return browserVersion; }
        public String getPlatform() { return platform; }
        public String getPlatformVersion() { return platformVersion; }
        public String getScreenResolution() { return screenResolution; }
        public String getDeviceName() { return deviceName; }
        public boolean isMobile() { return mobile; }
        public boolean isHeadless() { return headless; }
        public Map<String, Object> getAdditionalCapabilities() { return additionalCapabilities; }
        
        public static BrowserCapabilityBuilder builder() {
            return new BrowserCapabilityBuilder();
        }
        
        public static class BrowserCapabilityBuilder {
            private String browserName;
            private String browserVersion = "latest";
            private String platform = "ANY";
            private String platformVersion;
            private String screenResolution = "1920x1080";
            private String deviceName;
            private boolean mobile = false;
            private boolean headless = true;
            private Map<String, Object> additionalCapabilities = new HashMap<>();
            
            public BrowserCapabilityBuilder browserName(String browserName) { this.browserName = browserName; return this; }
            public BrowserCapabilityBuilder browserVersion(String browserVersion) { this.browserVersion = browserVersion; return this; }
            public BrowserCapabilityBuilder platform(String platform) { this.platform = platform; return this; }
            public BrowserCapabilityBuilder platformVersion(String platformVersion) { this.platformVersion = platformVersion; return this; }
            public BrowserCapabilityBuilder screenResolution(String screenResolution) { this.screenResolution = screenResolution; return this; }
            public BrowserCapabilityBuilder deviceName(String deviceName) { this.deviceName = deviceName; return this; }
            public BrowserCapabilityBuilder mobile(boolean mobile) { this.mobile = mobile; return this; }
            public BrowserCapabilityBuilder headless(boolean headless) { this.headless = headless; return this; }
            public BrowserCapabilityBuilder additionalCapabilities(Map<String, Object> additionalCapabilities) { 
                this.additionalCapabilities = additionalCapabilities; return this; 
            }
            
            public BrowserCapability build() {
                return new BrowserCapability(browserName, browserVersion, platform, platformVersion, 
                                           screenResolution, deviceName, mobile, headless, additionalCapabilities);
            }
        }
    }
    
    public static class BrowserSession {
        private String sessionId;
        private WebDriver driver;
        private BrowserCapability capability;
        private String provider;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private Instant startTime;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private Instant lastActivity;
        private SessionStatus status;
        private Map<String, Object> metadata;
        
        public BrowserSession(String sessionId, WebDriver driver, BrowserCapability capability, 
                            String provider, Instant startTime) {
            this.sessionId = sessionId;
            this.driver = driver;
            this.capability = capability;
            this.provider = provider;
            this.startTime = startTime;
            this.lastActivity = startTime;
            this.status = SessionStatus.ACTIVE;
            this.metadata = new HashMap<>();
        }
        
        // Getters and setters
        public String getSessionId() { return sessionId; }
        public WebDriver getDriver() { return driver; }
        public BrowserCapability getCapability() { return capability; }
        public String getProvider() { return provider; }
        public Instant getStartTime() { return startTime; }
        public Instant getLastActivity() { return lastActivity; }
        public void setLastActivity(Instant lastActivity) { this.lastActivity = lastActivity; }
        public SessionStatus getStatus() { return status; }
        public void setStatus(SessionStatus status) { this.status = status; }
        public Map<String, Object> getMetadata() { return metadata; }
        
        public static BrowserSessionBuilder builder() {
            return new BrowserSessionBuilder();
        }
        
        public static class BrowserSessionBuilder {
            private String sessionId;
            private WebDriver driver;
            private BrowserCapability capability;
            private String provider;
            private Instant startTime;
            
            public BrowserSessionBuilder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
            public BrowserSessionBuilder driver(WebDriver driver) { this.driver = driver; return this; }
            public BrowserSessionBuilder capability(BrowserCapability capability) { this.capability = capability; return this; }
            public BrowserSessionBuilder provider(String provider) { this.provider = provider; return this; }
            public BrowserSessionBuilder startTime(Instant startTime) { this.startTime = startTime; return this; }
            
            public BrowserSession build() {
                return new BrowserSession(sessionId, driver, capability, provider, startTime);
            }
        }
    }
    
    public enum SessionStatus {
        ACTIVE, IDLE, TERMINATED, ERROR
    }
    
    public static class ProviderMetrics {
        private String providerName;
        private int currentLoad;
        private int maxCapacity;
        private double averageLatency;
        private double successRate;
        private double costPerSession;
        private Instant lastUpdated;
        
        public ProviderMetrics(String providerName, int currentLoad, int maxCapacity, 
                             double averageLatency, double successRate, double costPerSession) {
            this.providerName = providerName;
            this.currentLoad = currentLoad;
            this.maxCapacity = maxCapacity;
            this.averageLatency = averageLatency;
            this.successRate = successRate;
            this.costPerSession = costPerSession;
            this.lastUpdated = Instant.now();
        }
        
        // Getters
        public String getProviderName() { return providerName; }
        public int getCurrentLoad() { return currentLoad; }
        public int getMaxCapacity() { return maxCapacity; }
        public double getAverageLatency() { return averageLatency; }
        public double getSuccessRate() { return successRate; }
        public double getCostPerSession() { return costPerSession; }
        public Instant getLastUpdated() { return lastUpdated; }
    }
    
    // Core Interfaces
    public interface BrowserProvider {
        String getName();
        boolean supports(BrowserCapability capability);
        BrowserSession createSession(BrowserCapability capability) throws BrowserProviderException;
        void closeSession(String sessionId);
        double getCostPerSession(BrowserCapability capability);
        ProviderMetrics getMetrics();
        boolean hasCapacity();
    }
    
    public static class BrowserProviderException extends Exception {
        public BrowserProviderException(String message) { super(message); }
        public BrowserProviderException(String message, Throwable cause) { super(message, cause); }
    }
    
    // Core Services
    public static class CrossBrowserManager {
        private final Map<String, BrowserProvider> providers = new ConcurrentHashMap<>();
        private final BrowserSessionManager sessionManager;
        private final BrowserLoadBalancer loadBalancer;
        private final BrowserMatrixConfig config;
        
        public CrossBrowserManager() {
            this.sessionManager = new BrowserSessionManager();
            this.loadBalancer = new BrowserLoadBalancer();
            this.config = loadBrowserMatrix();
            initializeProviders();
        }
        
        public BrowserSession createSession(BrowserCapability capability) throws BrowserProviderException {
            logger.info("Creating browser session for: {} {} on {}", 
                       capability.getBrowserName(), capability.getBrowserVersion(), capability.getPlatform());
            
            BrowserProvider provider = selectOptimalProvider(capability);
            BrowserSession session = provider.createSession(capability);
            sessionManager.registerSession(session);
            
            logger.info("Browser session created: {} using provider: {}", 
                       session.getSessionId(), provider.getName());
            return session;
        }
        
        public void closeSession(String sessionId) {
            sessionManager.closeSession(sessionId);
        }
        
        public List<BrowserSession> getActiveSessions() {
            return sessionManager.getActiveSessions();
        }
        
        public BrowserSession getSession(String sessionId) {
            return sessionManager.getSession(sessionId);
        }
        
        private BrowserProvider selectOptimalProvider(BrowserCapability capability) throws BrowserProviderException {
            List<BrowserProvider> compatibleProviders = providers.values().stream()
                .filter(provider -> provider.supports(capability) && provider.hasCapacity())
                .collect(Collectors.toList());
            
            if (compatibleProviders.isEmpty()) {
                throw new BrowserProviderException("No available provider for capability: " + capability.getBrowserName());
            }
            
            return loadBalancer.selectProvider(compatibleProviders, capability);
        }
        
        private void initializeProviders() {
            // Initialize local provider
            LocalBrowserProvider localProvider = new LocalBrowserProvider();
            providers.put(localProvider.getName(), localProvider);
            
            // Initialize cloud provider (simulated)
            CloudBrowserProvider cloudProvider = new CloudBrowserProvider();
            providers.put(cloudProvider.getName(), cloudProvider);
            
            logger.info("Initialized {} browser providers", providers.size());
        }
        
        private BrowserMatrixConfig loadBrowserMatrix() {
            try {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                mapper.registerModule(new JavaTimeModule());
                
                InputStream configStream = getClass().getResourceAsStream("/browser-matrix.yml");
                if (configStream != null) {
                    return mapper.readValue(configStream, BrowserMatrixConfig.class);
                }
            } catch (Exception e) {
                logger.warn("Failed to load browser matrix config, using defaults", e);
            }
            
            return createDefaultConfig();
        }
        
        private BrowserMatrixConfig createDefaultConfig() {
            BrowserMatrixConfig config = new BrowserMatrixConfig();
            config.browsers = new HashMap<>();
            
            // Default Chrome config
            BrowserConfig chrome = new BrowserConfig();
            chrome.versions = Arrays.asList("latest", "latest-1");
            chrome.platforms = Arrays.asList("Windows 10", "Linux");
            chrome.mobile = false;
            config.browsers.put("chrome", chrome);
            
            // Default Firefox config
            BrowserConfig firefox = new BrowserConfig();
            firefox.versions = Arrays.asList("latest", "latest-1");
            firefox.platforms = Arrays.asList("Windows 10", "Linux");
            firefox.mobile = false;
            config.browsers.put("firefox", firefox);
            
            return config;
        }
    }
    
    public static class BrowserSessionManager {
        private final Map<String, BrowserSession> activeSessions = new ConcurrentHashMap<>();
        private final ScheduledExecutorService cleanupExecutor = Executors.newScheduledThreadPool(2);
        
        public BrowserSessionManager() {
            startCleanupTask();
        }
        
        public void registerSession(BrowserSession session) {
            activeSessions.put(session.getSessionId(), session);
            logger.info("Registered session: {}", session.getSessionId());
        }
        
        public void closeSession(String sessionId) {
            BrowserSession session = activeSessions.remove(sessionId);
            if (session != null) {
                try {
                    session.setStatus(SessionStatus.TERMINATED);
                    session.getDriver().quit();
                    logger.info("Closed session: {}", sessionId);
                } catch (Exception e) {
                    logger.error("Error closing session: {}", sessionId, e);
                }
            }
        }
        
        public BrowserSession getSession(String sessionId) {
            BrowserSession session = activeSessions.get(sessionId);
            if (session != null) {
                session.setLastActivity(Instant.now());
            }
            return session;
        }
        
        public List<BrowserSession> getActiveSessions() {
            return new ArrayList<>(activeSessions.values());
        }
        
        private void startCleanupTask() {
            cleanupExecutor.scheduleAtFixedRate(this::cleanupIdleSessions, 5, 5, TimeUnit.MINUTES);
        }
        
        private void cleanupIdleSessions() {
            Instant cutoff = Instant.now().minus(Duration.ofMinutes(30));
            
            List<String> idleSessions = activeSessions.values().stream()
                .filter(session -> session.getLastActivity().isBefore(cutoff))
                .map(BrowserSession::getSessionId)
                .collect(Collectors.toList());
            
            idleSessions.forEach(sessionId -> {
                logger.info("Cleaning up idle session: {}", sessionId);
                closeSession(sessionId);
            });
        }
    }
    
    public static class BrowserLoadBalancer {
        public BrowserProvider selectProvider(List<BrowserProvider> providers, BrowserCapability capability) {
            return providers.stream()
                .min(Comparator.comparing(provider -> calculateProviderScore(provider, capability)))
                .orElseThrow(() -> new RuntimeException("No provider available"));
        }
        
        private double calculateProviderScore(BrowserProvider provider, BrowserCapability capability) {
            ProviderMetrics metrics = provider.getMetrics();
            
            double loadScore = (double) metrics.getCurrentLoad() / metrics.getMaxCapacity();
            double latencyScore = metrics.getAverageLatency() / 1000.0;
            double costScore = provider.getCostPerSession(capability) / 100.0;
            double reliabilityScore = 1.0 - metrics.getSuccessRate();
            
            // Weighted scoring: load 30%, latency 20%, cost 30%, reliability 20%
            return (loadScore * 0.3) + (latencyScore * 0.2) + (costScore * 0.3) + (reliabilityScore * 0.2);
        }
    }
    
    // Provider Implementations
    public static class LocalBrowserProvider implements BrowserProvider {
        private final Map<String, Integer> sessionCounts = new ConcurrentHashMap<>();
        private final int maxSessions = 10;
        
        @Override
        public String getName() {
            return "local";
        }
        
        @Override
        public boolean supports(BrowserCapability capability) {
            return Arrays.asList("chrome", "firefox").contains(capability.getBrowserName().toLowerCase()) 
                   && !capability.isMobile();
        }
        
        @Override
        public BrowserSession createSession(BrowserCapability capability) throws BrowserProviderException {
            if (!hasCapacity()) {
                throw new BrowserProviderException("Local provider at capacity");
            }
            
            try {
                WebDriver driver = createWebDriver(capability);
                String sessionId = UUID.randomUUID().toString();
                
                sessionCounts.merge(capability.getBrowserName(), 1, Integer::sum);
                
                return BrowserSession.builder()
                    .sessionId(sessionId)
                    .driver(driver)
                    .capability(capability)
                    .provider(getName())
                    .startTime(Instant.now())
                    .build();
                    
            } catch (Exception e) {
                throw new BrowserProviderException("Failed to create local session", e);
            }
        }
        
        @Override
        public void closeSession(String sessionId) {
            // Session cleanup handled by session manager
        }
        
        @Override
        public double getCostPerSession(BrowserCapability capability) {
            return 0.0; // Local sessions are free
        }
        
        @Override
        public ProviderMetrics getMetrics() {
            int currentLoad = sessionCounts.values().stream().mapToInt(Integer::intValue).sum();
            return new ProviderMetrics(getName(), currentLoad, maxSessions, 500.0, 0.95, 0.0);
        }
        
        @Override
        public boolean hasCapacity() {
            int currentLoad = sessionCounts.values().stream().mapToInt(Integer::intValue).sum();
            return currentLoad < maxSessions;
        }
        
        private WebDriver createWebDriver(BrowserCapability capability) {
            String browserName = capability.getBrowserName().toLowerCase();
            
            switch (browserName) {
                case "chrome":
                    ChromeOptions chromeOptions = new ChromeOptions();
                    if (capability.isHeadless()) {
                        chromeOptions.addArguments("--headless");
                    }
                    chromeOptions.addArguments("--no-sandbox", "--disable-dev-shm-usage");
                    return new ChromeDriver(chromeOptions);
                    
                case "firefox":
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    if (capability.isHeadless()) {
                        firefoxOptions.addArguments("--headless");
                    }
                    return new FirefoxDriver(firefoxOptions);
                    
                default:
                    throw new RuntimeException("Unsupported browser: " + browserName);
            }
        }
    }
    
    public static class CloudBrowserProvider implements BrowserProvider {
        private final Map<String, Integer> sessionCounts = new ConcurrentHashMap<>();
        private final int maxSessions = 50;
        
        @Override
        public String getName() {
            return "cloud";
        }
        
        @Override
        public boolean supports(BrowserCapability capability) {
            // Cloud provider supports all browsers including mobile
            return true;
        }
        
        @Override
        public BrowserSession createSession(BrowserCapability capability) throws BrowserProviderException {
            if (!hasCapacity()) {
                throw new BrowserProviderException("Cloud provider at capacity");
            }
            
            try {
                // Simulate cloud session creation
                WebDriver driver = createCloudWebDriver(capability);
                String sessionId = UUID.randomUUID().toString();
                
                sessionCounts.merge(capability.getBrowserName(), 1, Integer::sum);
                
                return BrowserSession.builder()
                    .sessionId(sessionId)
                    .driver(driver)
                    .capability(capability)
                    .provider(getName())
                    .startTime(Instant.now())
                    .build();
                    
            } catch (Exception e) {
                throw new BrowserProviderException("Failed to create cloud session", e);
            }
        }
        
        @Override
        public void closeSession(String sessionId) {
            // Session cleanup handled by session manager
        }
        
        @Override
        public double getCostPerSession(BrowserCapability capability) {
            return capability.isMobile() ? 2.0 : 1.0; // Mobile sessions cost more
        }
        
        @Override
        public ProviderMetrics getMetrics() {
            int currentLoad = sessionCounts.values().stream().mapToInt(Integer::intValue).sum();
            return new ProviderMetrics(getName(), currentLoad, maxSessions, 2000.0, 0.98, 1.0);
        }
        
        @Override
        public boolean hasCapacity() {
            int currentLoad = sessionCounts.values().stream().mapToInt(Integer::intValue).sum();
            return currentLoad < maxSessions;
        }
        
        private WebDriver createCloudWebDriver(BrowserCapability capability) {
            // Simulate cloud WebDriver creation
            // In real implementation, this would create RemoteWebDriver with cloud provider URL
            
            if (capability.isMobile()) {
                // For demo, create local driver but mark as mobile simulation
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--headless");
                options.addArguments("--user-agent=Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)");
                return new ChromeDriver(options);
            } else {
                // Create regular browser session
                return new LocalBrowserProvider().createWebDriver(capability);
            }
        }
    }
    
    // Configuration Classes
    public static class BrowserMatrixConfig {
        public Map<String, BrowserConfig> browsers;
        public Map<String, ProviderConfig> providers;
    }
    
    public static class BrowserConfig {
        public List<String> versions;
        public List<String> platforms;
        public boolean mobile;
        public List<String> devices;
    }
    
    public static class ProviderConfig {
        public boolean enabled;
        public int maxSessions;
        public List<String> browsers;
        public Map<String, String> credentials;
    }
    
    // Main Infrastructure Class
    public static class CrossBrowserInfrastructureSystem {
        private final CrossBrowserManager browserManager;
        private final ObjectMapper objectMapper;
        
        public CrossBrowserInfrastructureSystem() {
            this.browserManager = new CrossBrowserManager();
            this.objectMapper = new ObjectMapper();
            this.objectMapper.registerModule(new JavaTimeModule());
        }
        
        public BrowserSession createSession(BrowserCapability capability) throws BrowserProviderException {
            return browserManager.createSession(capability);
        }
        
        public void closeSession(String sessionId) {
            browserManager.closeSession(sessionId);
        }
        
        public BrowserSession getSession(String sessionId) {
            return browserManager.getSession(sessionId);
        }
        
        public List<BrowserSession> getActiveSessions() {
            return browserManager.getActiveSessions();
        }
        
        public String exportSessionsAsJson() {
            try {
                return objectMapper.writeValueAsString(getActiveSessions());
            } catch (Exception e) {
                logger.error("Error exporting sessions as JSON", e);
                return "[]";
            }
        }
        
        public InfrastructureMetrics getInfrastructureMetrics() {
            List<BrowserSession> sessions = getActiveSessions();
            
            Map<String, Long> sessionsByProvider = sessions.stream()
                .collect(Collectors.groupingBy(BrowserSession::getProvider, Collectors.counting()));
            
            Map<String, Long> sessionsByBrowser = sessions.stream()
                .collect(Collectors.groupingBy(s -> s.getCapability().getBrowserName(), Collectors.counting()));
            
            return new InfrastructureMetrics(
                sessions.size(),
                sessionsByProvider,
                sessionsByBrowser,
                Instant.now()
            );
        }
    }
    
    public static class InfrastructureMetrics {
        private int totalActiveSessions;
        private Map<String, Long> sessionsByProvider;
        private Map<String, Long> sessionsByBrowser;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private Instant timestamp;
        
        public InfrastructureMetrics(int totalActiveSessions, Map<String, Long> sessionsByProvider, 
                                   Map<String, Long> sessionsByBrowser, Instant timestamp) {
            this.totalActiveSessions = totalActiveSessions;
            this.sessionsByProvider = sessionsByProvider;
            this.sessionsByBrowser = sessionsByBrowser;
            this.timestamp = timestamp;
        }
        
        // Getters
        public int getTotalActiveSessions() { return totalActiveSessions; }
        public Map<String, Long> getSessionsByProvider() { return sessionsByProvider; }
        public Map<String, Long> getSessionsByBrowser() { return sessionsByBrowser; }
        public Instant getTimestamp() { return timestamp; }
    }
}

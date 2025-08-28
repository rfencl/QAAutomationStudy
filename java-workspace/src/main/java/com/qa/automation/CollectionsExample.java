package com.qa.automation;

import java.util.*;
import java.util.stream.Collectors;

public class CollectionsExample {
    
    public static void main(String[] args) {
        demonstrateCollections();
        demonstrateStreams();
        demonstrateStringComparison();
    }
    
    public static void demonstrateCollections() {
        System.out.println("=== Collections Demo ===");
        
        // List - allows duplicates, maintains order
        List<String> browsers = Arrays.asList("Chrome", "Firefox", "Edge", "Chrome");
        System.out.println("List (allows duplicates): " + browsers);
        
        // Set - no duplicates, no guaranteed order
        Set<String> uniqueIds = new HashSet<>(Arrays.asList("ID1", "ID2", "ID1", "ID3"));
        System.out.println("Set (no duplicates): " + uniqueIds);
        
        // Map - key-value pairs
        Map<String, String> testData = new HashMap<>();
        testData.put("username", "qa_user");
        testData.put("password", "pass123");
        testData.put("environment", "staging");
        System.out.println("Map (key-value): " + testData);
    }
    
    public static void demonstrateStreams() {
        System.out.println("\n=== Java 8 Streams Demo ===");
        
        List<String> users = Arrays.asList("qa1", "qa2", "dev1", "qa3", "admin1");
        
        // Filter QA users using streams
        List<String> qaUsers = users.stream()
                .filter(u -> u.startsWith("qa"))
                .collect(Collectors.toList());
        
        System.out.println("All users: " + users);
        System.out.println("QA users only: " + qaUsers);
        
        // Count QA users
        long qaCount = users.stream()
                .filter(u -> u.startsWith("qa"))
                .count();
        System.out.println("Number of QA users: " + qaCount);
    }
    
    public static void demonstrateStringComparison() {
        System.out.println("\n=== String Comparison Demo ===");
        
        String a = new String("test");
        String b = new String("test");
        String c = "test";
        String d = "test";
        
        System.out.println("a == b (different objects): " + (a == b)); // false
        System.out.println("a.equals(b) (same content): " + a.equals(b)); // true
        System.out.println("c == d (string pool): " + (c == d)); // true
        System.out.println("a == c (object vs pool): " + (a == c)); // false
        System.out.println("a.equals(c) (same content): " + a.equals(c)); // true
    }
}

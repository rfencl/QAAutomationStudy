package com.qa.automation;

import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.*;

public class JavaBasicsTest {
    
    @Test
    public void testCollectionsDifferences() {
        // Test List behavior
        List<String> list = new ArrayList<>();
        list.add("item1");
        list.add("item1"); // duplicates allowed
        Assert.assertEquals(list.size(), 2);
        
        // Test Set behavior
        Set<String> set = new HashSet<>();
        set.add("item1");
        set.add("item1"); // duplicates not allowed
        Assert.assertEquals(set.size(), 1);
        
        // Test Map behavior
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key1", "value2"); // overwrites previous value
        Assert.assertEquals(map.get("key1"), "value2");
    }
    
    @Test
    public void testStringComparison() {
        String str1 = new String("test");
        String str2 = new String("test");
        String str3 = "test";
        String str4 = "test";
        
        // == compares references
        Assert.assertFalse(str1 == str2, "Different String objects should not be equal with ==");
        Assert.assertTrue(str3 == str4, "String literals should be equal with == (string pool)");
        
        // .equals() compares content
        Assert.assertTrue(str1.equals(str2), "String objects with same content should be equal with .equals()");
        Assert.assertTrue(str1.equals(str3), "String object and literal with same content should be equal");
    }
    
    @Test
    public void testStreamFiltering() {
        List<String> testUsers = Arrays.asList("qa1", "qa2", "dev1", "qa3", "admin1");
        
        List<String> qaUsers = testUsers.stream()
                .filter(user -> user.startsWith("qa"))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        
        Assert.assertEquals(qaUsers.size(), 3, "Should find 3 QA users");
        Assert.assertTrue(qaUsers.contains("qa1"), "Should contain qa1");
        Assert.assertTrue(qaUsers.contains("qa2"), "Should contain qa2");
        Assert.assertTrue(qaUsers.contains("qa3"), "Should contain qa3");
        Assert.assertFalse(qaUsers.contains("dev1"), "Should not contain dev1");
    }
    
    @Test
    public void testExceptionHandling() {
        try {
            // Simulate a scenario that might throw an exception
            String nullString = null;
            int length = nullString.length(); // This will throw NullPointerException
            Assert.fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // Expected exception - test passes
            Assert.assertTrue(true, "Correctly caught NullPointerException");
        } catch (Exception e) {
            Assert.fail("Unexpected exception type: " + e.getClass().getSimpleName());
        }
    }
}

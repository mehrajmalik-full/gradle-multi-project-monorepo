/*
 * This Java source file tests the application.
 */
package inventory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InventoryAppTest {
    @Test
    public void testAppHasAGreeting() {
        InventoryApp classUnderTest = new InventoryApp();
        assertNotNull(classUnderTest.getGreeting(), "app should have a greeting");
    }

    @Test
    public void testGreetingContainsProfile() {
        InventoryApp classUnderTest = new InventoryApp();
        String greeting = classUnderTest.getGreeting();
        assertTrue(greeting.contains("Alice"), "greeting should contain profile name");
    }
}

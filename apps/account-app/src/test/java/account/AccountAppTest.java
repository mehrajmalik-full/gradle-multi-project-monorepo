/*
 * This Java source file tests the application.
 */
package account;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountAppTest {
    @Test
    public void testAppHasAGreeting() {
        AccountApp classUnderTest = new AccountApp();
        assertNotNull(classUnderTest.getGreeting(), "app should have a greeting");
    }

    @Test
    public void testGreetingContainsProfile() {
        AccountApp classUnderTest = new AccountApp();
        String greeting = classUnderTest.getGreeting();
        assertTrue(greeting.contains("Alice"), "greeting should contain profile name");
    }

    @Test
    public void testGreetingContainsGreeterMessage() {
        AccountApp classUnderTest = new AccountApp();
        String greeting = classUnderTest.getGreeting();
        assertTrue(greeting.contains("Hello world"), "greeting should contain greeter message");
    }
}

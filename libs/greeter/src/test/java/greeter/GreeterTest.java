/*
 * This Java source file was converted from Kotlin.
 */
package greeter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GreeterTest {
    @Test
    public void testGetGreeting() {
        Greeter classUnderTest = new Greeter();
        assertNotNull(classUnderTest.getGreeting(), "getGreeting should return non-null value");
    }
}

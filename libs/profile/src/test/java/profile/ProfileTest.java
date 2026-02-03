/*
 * This Java source file was converted from Kotlin.
 */
package profile;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProfileTest {
    @Test
    public void testGetCurrentProfile() {
        Profile classUnderTest = new Profile();
        assertNotNull(classUnderTest.getCurrentProfile(), "getCurrentProfile should return non-null value");
    }
}

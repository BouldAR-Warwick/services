package pbrg.webservices.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ServletUtilsTest {

    @BeforeAll
    static void setUp() {
        if (!TestUtils.python3Installed()) {
            throw new RuntimeException("Python 3 not installed.");
        }
    }

    @Test
    void getContentType() {
        assertEquals("image/jpeg", ServletUtils.getContentType("jpg"));

        // ensure non keys are mapped to null
        String[] nonKeys = {null, "", "not-in-map"};
        for (String nonKey : nonKeys) {
            assertNull(ServletUtils.getContentType(nonKey));
        }
    }
}

package pbrg.webservices.utils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DatabaseControllerTest {

    @Disabled
    @Test
    void getDbConnection() {
        assertDoesNotThrow(() -> DatabaseController.getDbConnection());
    }
}

package pbrg.webservices.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class DatabaseControllerTest {

    @Test
    @Disabled
    void getDbConnection() throws SQLException {
        Connection connection = DatabaseController.getDbConnection();
        assertNotNull(connection);
    }
}

package pbrg.webservices.utils;

import org.junit.jupiter.api.Test;

class DatabaseControllerTest {

    @Test
    void getDbConnection() {
        assert DatabaseController.getDbConnection() != null;
    }
}

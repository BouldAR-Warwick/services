package pbrg.webservices.database;

import static org.junit.jupiter.api.Assertions.assertNull;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;
import static pbrg.webservices.database.WallController.getWallIdFromRouteId;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WallControllerTest {

    @BeforeAll
    static void startResources() throws IllegalStateException, SQLException {
        startTestDatabaseInThread();

        // use the test database
        DatabaseController.setDataSource(getTestDataSource());
    }

    @AfterAll
    static void closeResources() {
        closeTestDatabaseInThread();
    }

    @Test
    void getWallIdFromRouteIdTestNonExistentRoute() {
        // given an invalid routeId
        int routeId = -1;

        // when: get wallId from routeId
        Integer wallId = getWallIdFromRouteId(routeId);

        // then: wall should be null
        assertNull(wallId);
    }
}

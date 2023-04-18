package pbrg.webservices.database;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;

import java.sql.SQLException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


class DatabaseControllerTest {

    /** The test username. */
    private static final String TEST_USERNAME = "username_test";

    /** The test email. */
    private static final String TEST_EMAIL = "email_test";

    @BeforeAll
    static void startResources() throws SQLException {
        startTestDatabaseInThread();

        // use the test database
        DatabaseController.setDataSource(getTestDataSource());

        // remove the test user credentials if they exist
        if (DatabaseController.usernameExists(TEST_USERNAME)) {
            Integer uid =
                DatabaseController.getUserIDFromUsername(TEST_USERNAME);
            assert uid != null;
            boolean deleted = DatabaseController.deleteUser(uid);
            assert deleted;
            assertFalse(DatabaseController.usernameExists(TEST_USERNAME));
        }
        if (DatabaseController.emailExists(TEST_EMAIL)) {
            Integer uid = DatabaseController.getUserIDFromEmail(TEST_EMAIL);
            assert uid != null;
            boolean deleted = DatabaseController.deleteUser(uid);
            assert deleted;
            assertFalse(DatabaseController.emailExists(TEST_EMAIL));
        }
    }

    @AfterAll
    static void closeResources() {
         closeTestDatabaseInThread();
    }

    @Test
    void signUpNewUser() throws SQLException {
        // given: a new user with unique credentials

        // then: no exception is thrown
        assertDoesNotThrow(() -> {
            // when: signing up
            boolean added = DatabaseController.signUp(
                TEST_USERNAME, TEST_EMAIL, "password"
            );

            // then: the user is added
            assertTrue(added);
        });

        // remove the test user
        Integer uid = DatabaseController.getUserIDFromUsername(TEST_USERNAME);
        assert uid != null;
        DatabaseController.deleteUser(uid);
    }

    @Test
    void signUpConflictingCredentials() throws SQLException {
        // given: an existing user
        String username = "username_existing";
        String email = "email_existing";
        DatabaseController.signUp(
            username, email, "password"
        );
        assert DatabaseController.usernameExists(username);
        assert DatabaseController.emailExists(email);

        // when: signing up with the same credentials
        boolean added = DatabaseController.signUp(
            username, email, "password"
        );

        // then: the user is not added
        assertFalse(added);
    }
}

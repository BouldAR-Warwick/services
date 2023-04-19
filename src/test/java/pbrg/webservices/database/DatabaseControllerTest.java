package pbrg.webservices.database;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import javax.naming.InitialContext;
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
    public void testPrivateConstructor() {
        // get constructor
        Constructor<DatabaseController> constructor;
        try {
            constructor = DatabaseController.class.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            fail("DatabaseUtils should have a private constructor");
            throw new RuntimeException(e);
        }

        // ensure calling constructor throws an IllegalStateException exception
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            fail("Expected IllegalStateException to be thrown");
        } catch (
            InvocationTargetException | InstantiationException
            | IllegalAccessException e
        ) {
            assertTrue(e.getCause() instanceof IllegalStateException);
        }
    }

    @Test
    void setDataSourceToProduction() {
        // given: in production
        boolean inProduction = true;

        // mock an InitialContext
        InitialContext prodContext =
            ProductionDatabaseTest.createProdContext();
        ProductionDatabase.setInitialContext(prodContext);

        // then: no exception is thrown
        assertDoesNotThrow(
            // when: setting the data source to the production database
            () -> DatabaseController.setDataSource(inProduction)
        );

        // after: reset context to use the test database, use the test database
        ProductionDatabase.setInitialContext(
            ProductionDatabase.getDefaultContext()
        );
        DatabaseController.setDataSource(getTestDataSource());
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

        // given: a non-existing username, email
        assert !DatabaseController.usernameExists(TEST_USERNAME);
        assert !DatabaseController.emailExists(TEST_EMAIL);

        // when: signing up with the same credentials
        boolean[] invalidRequests = {
            // both match
            DatabaseController.signUp(
                username, email, "password"
            ),

            // email matches
            DatabaseController.signUp(
                TEST_USERNAME, email, "password"
            ),

            // username matches
            DatabaseController.signUp(
                username, TEST_EMAIL, "password"
            )
        };

        for (boolean invalidRequest : invalidRequests) {
            // then: the user is not added
            assertFalse(invalidRequest);
        }

        // after: remove the users
        assert !DatabaseController.usernameExists(TEST_USERNAME);

        Integer uid = DatabaseController.getUserIDFromUsername(username);
        assert uid != null;
        DatabaseController.deleteUser(uid);
    }
}

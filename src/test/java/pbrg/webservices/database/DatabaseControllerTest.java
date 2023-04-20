package pbrg.webservices.database;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pbrg.webservices.models.User;


class DatabaseControllerTest {

    /** The test username. */
    private static final String TEST_USERNAME = "username_test";

    /** The test email. */
    private static final String TEST_EMAIL = "email_test";

    /** The test password. */
    private static final String TEST_PASSWORD = "password";

    static void clearTestUser() throws SQLException {
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

    @BeforeAll
    static void startResources() throws SQLException {
        startTestDatabaseInThread();

        // use the test database
        DatabaseController.setDataSource(getTestDataSource());

        // remove the test user credentials if they exist
        clearTestUser();
    }

    @AfterAll
    static void closeResources() {
         closeTestDatabaseInThread();
    }

    @AfterEach
    void ensureTestUserIsRemoved() throws SQLException {
        assertFalse(DatabaseController.usernameExists(TEST_USERNAME));
        assertFalse(DatabaseController.emailExists(TEST_EMAIL));
    }

    @Test
    void testPrivateConstructor() {
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

    // nested class for sign in method
    @Nested
    class signIn {

        @Test
        void signInValidUser() throws SQLException {
            // given: a user that exists in the system
            assertDoesNotThrow(() -> {
                // when: signing up
                boolean added = DatabaseController.signUp(
                    TEST_USERNAME, TEST_EMAIL, "password"
                );

                // then: the user is added
                assertTrue(added);
            });
            assertTrue(DatabaseController.usernameExists(TEST_USERNAME));
            assertTrue(DatabaseController.emailExists(TEST_EMAIL));

            // when: signing in
            User testUser = DatabaseController.signIn(TEST_USERNAME, TEST_PASSWORD);

            // then: should retrieve user
            assertNotNull(testUser);
            assertEquals(TEST_USERNAME, testUser.getUsername());
            Integer testUid = DatabaseController.getUserIDFromUsername(TEST_USERNAME);
            assert testUid != null;
            assertEquals(testUser.getUid(), testUid);

            // after: remove the test user
            assertTrue(DatabaseController.deleteUser(testUid));
        }

        @Test
        void signInConnectionCloseFails() throws SQLException {
            // mock database objects
            Connection connection = mock(Connection.class);
            doThrow(SQLException.class).when(connection).close();

            DataSource mockedDataSource = mock(DataSource.class);
            when(mockedDataSource.getConnection())
                .thenReturn(connection);

            // verify
            Connection mockedConnection = mockedDataSource.getConnection();
            assertThrows(
                // then: an exception is thrown
                SQLException.class,

                // when: closing the connection
                mockedConnection::close
            );


            DataSource originalDataSource = DatabaseController.getDataSource();
            DatabaseController.setDataSource(mockedDataSource);

            assertThrows(
                // then: an exception is thrown
                NullPointerException.class,

                // when: signing in
                () -> DatabaseController.signIn(TEST_USERNAME, TEST_PASSWORD)
            );

            // after: reset data source
            DatabaseController.setDataSource(originalDataSource);
        }

        @Test
        void signInInvalidUser() throws SQLException {
            // given: a user that does not exist in the system
            assertFalse(DatabaseController.usernameExists(TEST_USERNAME));
            assertFalse(DatabaseController.emailExists(TEST_EMAIL));

            // when: signing in
            User testUser =
                DatabaseController.signIn(TEST_USERNAME, TEST_PASSWORD);

            // then: should not retrieve user
            assertNull(testUser);
            Integer testUid =
                DatabaseController.getUserIDFromUsername(TEST_USERNAME);
            assertNull(testUid);
        }

        @Test
        void signInExecuteQueryFails() throws SQLException {
            // mock database objects

            // data source -> connection -> prepared statement -> result set
            PreparedStatement mockedPreparedStatement = mock(PreparedStatement.class);
            when(mockedPreparedStatement.executeQuery())
                .thenThrow(new SQLException());

            Connection mockedConnection = mock(Connection.class);
            when(mockedConnection.prepareStatement(anyString()))
                .thenReturn(mockedPreparedStatement);

            DataSource mockedDataSource = mock(DataSource.class);
            when(mockedDataSource.getConnection())
                .thenReturn(mockedConnection);

            assertThrows(
                // then: an exception is thrown
                SQLException.class,

                // when: signing in
                mockedDataSource.getConnection().prepareStatement("")::executeQuery
            );

            DataSource originalDataSource = DatabaseController.getDataSource();
            DatabaseController.setDataSource(mockedDataSource);

            assertThrows(
                // then: an exception is thrown
                SQLException.class,

                // when: signing in
                () -> DatabaseController.signIn(TEST_USERNAME, TEST_PASSWORD)
            );

            // after: reset data source
            DatabaseController.setDataSource(originalDataSource);
        }

        @Test
        void signInSetStringFails() throws SQLException {
            // mock database objects

            // data source -> connection -> prepared statement -> result set
            PreparedStatement mockedPreparedStatement = mock(PreparedStatement.class);
            doThrow(new SQLException()).when(mockedPreparedStatement)
                .setString(anyInt(), anyString());

            Connection mockedConnection = mock(Connection.class);
            when(mockedConnection.prepareStatement(anyString()))
                .thenReturn(mockedPreparedStatement);

            DataSource mockedDataSource = mock(DataSource.class);
            when(mockedDataSource.getConnection())
                .thenReturn(mockedConnection);

            // validate
            PreparedStatement preparedStatement = mockedDataSource.getConnection()
                .prepareStatement("");
            assertThrows(
                // then: an exception is thrown
                SQLException.class,

                // when: signing in
                () -> preparedStatement.setString(1, "")
            );

            DataSource originalDataSource = DatabaseController.getDataSource();
            DatabaseController.setDataSource(mockedDataSource);

            assertThrows(
                // then: an exception is thrown
                SQLException.class,

                // when: signing in
                () -> DatabaseController.signIn(TEST_USERNAME, TEST_PASSWORD)
            );

            // after: reset data source
            DatabaseController.setDataSource(originalDataSource);
        }

        @Test
        void signInPreparedStatementFails() throws SQLException {
            // mock database objects
            Connection mockedConnection = mock(Connection.class);
            when(mockedConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException());

            DataSource mockedDataSource = mock(DataSource.class);
            when(mockedDataSource.getConnection())
                .thenReturn(mockedConnection);

            DataSource originalDataSource = DatabaseController.getDataSource();
            DatabaseController.setDataSource(mockedDataSource);

            assertThrows(
                // then: an exception is thrown
                SQLException.class,

                // when: signing in
                () -> DatabaseController.signIn(TEST_USERNAME, TEST_PASSWORD)
            );

            // after: reset data source
            DatabaseController.setDataSource(originalDataSource);
        }

        @Test
        void signInResultsSetThrowsException() throws SQLException {
            // mock database objects

            // data source -> connection -> prepared statement -> result set
            ResultSet mockedResultSet = mock(ResultSet.class);
            when(mockedResultSet.next())
                .thenThrow(new SQLException());

            PreparedStatement mockedPreparedStatement = mock(PreparedStatement.class);
            when(mockedPreparedStatement.executeQuery())
                .thenReturn(mockedResultSet);

            Connection mockedConnection = mock(Connection.class);
            when(mockedConnection.prepareStatement(anyString()))
                .thenReturn(mockedPreparedStatement);

            DataSource mockedDataSource = mock(DataSource.class);
            when(mockedDataSource.getConnection())
                .thenReturn(mockedConnection);

            // verify
            ResultSet resultSet = mockedDataSource.getConnection().prepareStatement("")
                .executeQuery();
            assertThrows(
                // then: an exception is thrown
                SQLException.class,

                // when: signing in
                resultSet::next
            );

            DataSource originalDataSource = DatabaseController.getDataSource();
            DatabaseController.setDataSource(mockedDataSource);

            assertThrows(
                // then: an exception is thrown
                SQLException.class,

                // when: signing in
                () -> DatabaseController.signIn(TEST_USERNAME, TEST_PASSWORD)
            );

            // after: reset data source
            DatabaseController.setDataSource(originalDataSource);
        }

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

        // after: remove the test user
        Integer uid = DatabaseController.getUserIDFromUsername(TEST_USERNAME);
        assertNotNull(uid);
        assertTrue(DatabaseController.deleteUser(uid));
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
        assertFalse(DatabaseController.usernameExists(TEST_USERNAME));

        Integer uid = DatabaseController.getUserIDFromUsername(username);
        assert uid != null;
        DatabaseController.deleteUser(uid);
    }

    @Test
    void usernameExistsUserDoesNotExist() throws SQLException {
        // given: a non-existing username
        // when: checking if the username exists
        // then: the username does not exist
        assertFalse(DatabaseController.usernameExists(TEST_USERNAME));
    }

    @Test
    void usernameExistsNoResults() throws SQLException {
        // given: a data source such that the result set is empty
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // use the mocked data source, storing the original
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(dataSource);

        // when: checking if the username exists
        // then: should not exist
        assertFalse(DatabaseController.usernameExists(TEST_USERNAME));

        // after: reset data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void emailExistsNoResults() throws SQLException {
        // given: a data source such that the result set is empty
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // use the mocked data source, storing the original
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(dataSource);

        // when: checking if the email exists
        // then: should not exist
        assertFalse(DatabaseController.emailExists(TEST_EMAIL));

        // after: reset data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Nested
    class getUserIDFromEmail {

        @Test
        void nonExistentEmail() throws SQLException {
            // given: a non-existing email
            assertFalse(DatabaseController.emailExists(TEST_EMAIL));

            // when: getting the user ID from the email
            // then: the user ID is null
            assertNull(DatabaseController.getUserIDFromEmail(TEST_EMAIL));
        }

        @Test
        void validEmail() throws SQLException {
            // given: an existing email

            // add the test user
            boolean added = DatabaseController.signUp(
                TEST_USERNAME, TEST_EMAIL, "password"
            );
            assertTrue(added);
            assertTrue(DatabaseController.emailExists(TEST_EMAIL));

            // when: getting the user ID from the email
            // then: the user ID is not null
            Integer uid = DatabaseController.getUserIDFromEmail(TEST_EMAIL);
            assertNotNull(uid);
            assertEquals(
                uid,
                DatabaseController.getUserIDFromUsername(TEST_USERNAME)
            );

            // after: remove the test user
            assertTrue(DatabaseController.deleteUser(uid));
        }
    }

    @Test
    void deleteUserThatDoesNotExist() throws SQLException {
        // given: a non-existing user (uid = -1)
        // when: deleting the user
        // then: the user is not deleted
        assertFalse(DatabaseController.deleteUser(-1));
    }
}

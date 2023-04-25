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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pbrg.webservices.database.CredentialController.addUser;
import static pbrg.webservices.database.CredentialController.deleteUser;
import static pbrg.webservices.database.CredentialController.emailExists;
import static pbrg.webservices.database.CredentialController.getUserIDFromEmail;
import static pbrg.webservices.database.CredentialController
    .getUserIDFromUsername;
import static pbrg.webservices.database.CredentialController.signUp;
import static pbrg.webservices.database.CredentialController.userExists;
import static pbrg.webservices.database.CredentialController.usernameExists;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pbrg.webservices.models.User;

public final class CredentialControllerTest {

    /** The test username. */
    private static final String TEST_USERNAME = "username_test";

    /** The test email. */
    private static final String TEST_EMAIL = "email_test";

    /** The test password. */
    private static final String TEST_PASSWORD = "password";

    static void clearTestUser() {
        // remove the test user credentials if they exist
        if (usernameExists(TEST_USERNAME)) {
            Integer uid =
                getUserIDFromUsername(TEST_USERNAME);
            assert uid != null;
            boolean deleted = deleteUser(uid);
            assert deleted;
            assertFalse(usernameExists(TEST_USERNAME));
        }
        if (emailExists(TEST_EMAIL)) {
            Integer uid = getUserIDFromEmail(TEST_EMAIL);
            assert uid != null;
            boolean deleted = deleteUser(uid);
            assert deleted;
            assertFalse(emailExists(TEST_EMAIL));
        }
    }

    /**
     * Creates a test user in the database.
     * @return the test user's uid
     */
    public static int createTestUser() {
        // remove the test user credentials if they exist
        clearTestUser();

        // create the test user
        boolean added = signUp(
            TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD
        );
        assertTrue(added);
        assertTrue(usernameExists(TEST_USERNAME));
        assertTrue(emailExists(TEST_EMAIL));

        // get the test user's uid
        Integer uid = getUserIDFromUsername(TEST_USERNAME);
        assertNotNull(uid);
        return uid;
    }

    /**
     * Creates a mock DataSource that throws an SQLException when
     * getConnection() is called.
     * @return the mock DataSource
     * @throws SQLException if an error occurs while creating the mock
     */
    static @NotNull DataSource mockEmptyResultSet() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString()))
            .thenReturn(preparedStatement);
        when(connection.prepareStatement(
            anyString(), eq(Statement.RETURN_GENERATED_KEYS))
        ).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        return dataSource;
    }

    @BeforeAll
    static void startResources() {
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
    void ensureTestUserIsRemoved() {
        assertFalse(usernameExists(TEST_USERNAME));
        assertFalse(emailExists(TEST_EMAIL));
    }

    @Test
    void testPrivateConstructor() {
        // get constructor
        Constructor<CredentialController> constructor;
        try {
            constructor = CredentialController.class.getDeclaredConstructor();
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

    /** nested class for sign in method tests. */
    @Nested
    class SignIn {

        @Test
        void signInValidUser() {
            // given: a user that exists in the system
            assertDoesNotThrow(() -> {
                // when: signing up
                boolean added = signUp(
                    TEST_USERNAME, TEST_EMAIL, "password"
                );

                // then: the user is added
                assertTrue(added);
            });
            assertTrue(usernameExists(TEST_USERNAME));
            assertTrue(emailExists(TEST_EMAIL));

            // when: signing in
            User testUser =
                CredentialController.signIn(TEST_USERNAME, TEST_PASSWORD);

            // then: should retrieve user
            assertNotNull(testUser);
            assertEquals(TEST_USERNAME, testUser.getUsername());
            Integer testUid = getUserIDFromUsername(TEST_USERNAME);
            assert testUid != null;
            assertEquals(testUser.getUid(), testUid);

            // after: remove the test user
            assertTrue(deleteUser(testUid));
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
                () -> CredentialController.signIn(TEST_USERNAME, TEST_PASSWORD)
            );

            // after: reset data source
            DatabaseController.setDataSource(originalDataSource);
        }

        @Test
        void signInInvalidUser() {
            // given: a user that does not exist in the system
            assertFalse(usernameExists(TEST_USERNAME));
            assertFalse(emailExists(TEST_EMAIL));

            // when: signing in
            User testUser =
                CredentialController.signIn(TEST_USERNAME, TEST_PASSWORD);

            // then: should not retrieve user
            assertNull(testUser);
            Integer testUid =
                getUserIDFromUsername(TEST_USERNAME);
            assertNull(testUid);
        }

        @Test
        void signInExecuteQueryFails() throws SQLException {
            // mock database objects

            // data source -> connection -> prepared statement -> result set
            PreparedStatement mockedPreparedStatement =
                mock(PreparedStatement.class);
            when(mockedPreparedStatement.executeQuery())
                .thenThrow(new SQLException());

            Connection mockedConnection = mock(Connection.class);
            when(mockedConnection.prepareStatement(anyString()))
                .thenReturn(mockedPreparedStatement);

            DataSource mockedDataSource = mock(DataSource.class);
            when(mockedDataSource.getConnection())
                .thenReturn(mockedConnection);

            // verify
            PreparedStatement mockedStatement =
                mockedDataSource.getConnection().prepareStatement("");
            assertThrows(
                // then: an exception is thrown
                SQLException.class,

                // when: signing in
                mockedStatement::executeQuery
            );

            DataSource originalDataSource = DatabaseController.getDataSource();
            DatabaseController.setDataSource(mockedDataSource);

            // when: signing in and experience SQLException
            User user =
                CredentialController.signIn(TEST_USERNAME, TEST_PASSWORD);

            // then: should not retrieve user
            assertNull(user);

            // after: reset data source
            DatabaseController.setDataSource(originalDataSource);
        }

        @Test
        void signInSetStringFails() throws SQLException {
            // mock database objects

            // data source -> connection -> prepared statement -> result set
            PreparedStatement mockedPreparedStatement =
                mock(PreparedStatement.class);
            doThrow(new SQLException()).when(mockedPreparedStatement)
                .setString(anyInt(), anyString());

            Connection mockedConnection = mock(Connection.class);
            when(mockedConnection.prepareStatement(anyString()))
                .thenReturn(mockedPreparedStatement);

            DataSource mockedDataSource = mock(DataSource.class);
            when(mockedDataSource.getConnection())
                .thenReturn(mockedConnection);

            // validate
            PreparedStatement preparedStatement =
                mockedDataSource.getConnection()
                .prepareStatement("");
            assertThrows(
                // then: an exception is thrown
                SQLException.class,

                // when: signing in
                () -> preparedStatement.setString(1, "")
            );

            DataSource originalDataSource = DatabaseController.getDataSource();
            DatabaseController.setDataSource(mockedDataSource);

            // when: signing in
            User user =
                CredentialController.signIn(TEST_USERNAME, TEST_PASSWORD);

            // then: should not retrieve user
            assertNull(user);

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

            // when: signing in
            User user =
                CredentialController.signIn(TEST_USERNAME, TEST_PASSWORD);

            // then: should not retrieve user
            assertNull(user);

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

            PreparedStatement mockedPreparedStatement =
                mock(PreparedStatement.class);
            when(mockedPreparedStatement.executeQuery())
                .thenReturn(mockedResultSet);

            Connection mockedConnection = mock(Connection.class);
            when(mockedConnection.prepareStatement(anyString()))
                .thenReturn(mockedPreparedStatement);

            DataSource mockedDataSource = mock(DataSource.class);
            when(mockedDataSource.getConnection())
                .thenReturn(mockedConnection);

            // verify
            ResultSet resultSet =
                mockedDataSource.getConnection().prepareStatement("")
                .executeQuery();
            assertThrows(
                // then: an exception is thrown
                SQLException.class,

                // when: signing in
                resultSet::next
            );

            DataSource originalDataSource = DatabaseController.getDataSource();
            DatabaseController.setDataSource(mockedDataSource);

            // when: signing in
            User user =
                CredentialController.signIn(TEST_USERNAME, TEST_PASSWORD);

            // then: should not retrieve user
            assertNull(user);

            // after: reset data source
            DatabaseController.setDataSource(originalDataSource);
        }

    }

    @Test
    void signUpNewUser() {
        // given: a new user with unique credentials

        // then: no exception is thrown
        assertDoesNotThrow(() -> {
            // when: signing up
            boolean added = signUp(
                TEST_USERNAME, TEST_EMAIL, "password"
            );

            // then: the user is added
            assertTrue(added);
        });

        // after: remove the test user
        Integer uid = getUserIDFromUsername(TEST_USERNAME);
        assertNotNull(uid);
        assertTrue(deleteUser(uid));
    }

    @Test
    void signUpConflictingCredentials() {
        // given: an existing user
        String username = "username_existing";
        String email = "email_existing";
        signUp(
            username, email, "password"
        );
        assert usernameExists(username);
        assert emailExists(email);

        // given: a non-existing username, email
        assert !usernameExists(TEST_USERNAME);
        assert !emailExists(TEST_EMAIL);

        // when: signing up with the same credentials
        boolean[] invalidRequests = {
            // both match
            signUp(
                username, email, "password"
            ),

            // email matches
            signUp(
                TEST_USERNAME, email, "password"
            ),

            // username matches
            signUp(
                username, TEST_EMAIL, "password"
            )
        };

        for (boolean invalidRequest : invalidRequests) {
            // then: the user is not added
            assertFalse(invalidRequest);
        }

        // after: remove the users
        assertFalse(usernameExists(TEST_USERNAME));

        Integer uid = getUserIDFromUsername(username);
        assert uid != null;
        deleteUser(uid);
    }

    @Test
    void usernameExistsUserDoesNotExist() {
        // given: a non-existing username
        // when: checking if the username exists
        // then: the username does not exist
        assertFalse(usernameExists(TEST_USERNAME));
    }

    @Test
    void usernameExistsNoResults() throws SQLException {
        // given: a data source such that the result set is empty
        DataSource dataSource = mockEmptyResultSet();

        // use the mocked data source, storing the original
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(dataSource);

        // when: checking if the username exists
        // then: should not exist
        assertFalse(usernameExists(TEST_USERNAME));

        // after: reset data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void emailExistsNoResults() throws SQLException {
        // given: a data source such that the result set is empty
        DataSource dataSource = mockEmptyResultSet();

        // use the mocked data source, storing the original
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(dataSource);

        // when: checking if the email exists
        // then: should not exist
        assertFalse(emailExists(TEST_EMAIL));

        // after: reset data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void userExistsNoResults() throws SQLException {
        // given: a data source such that the result set is empty
        DataSource dataSource = mockEmptyResultSet();

        // use the mocked data source, storing the original
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(dataSource);

        // when: checking if the email exists
        // then: should not exist
        assertFalse(userExists(-1));

        // after: reset data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Nested
    class GetUserIDFromEmail {

        @Test
        void nonExistentEmail() {
            // given: a non-existing email
            assertFalse(emailExists(TEST_EMAIL));

            // when: getting the user ID from the email
            // then: the user ID is null
            assertNull(getUserIDFromEmail(TEST_EMAIL));
        }

        @Test
        void validEmail() {
            // given: an existing email

            // add the test user
            boolean added = signUp(
                TEST_USERNAME, TEST_EMAIL, "password"
            );
            assertTrue(added);
            assertTrue(emailExists(TEST_EMAIL));

            // when: getting the user ID from the email
            // then: the user ID is not null
            Integer uid = getUserIDFromEmail(TEST_EMAIL);
            assertNotNull(uid);
            assertEquals(
                uid,
                getUserIDFromUsername(TEST_USERNAME)
            );

            // after: remove the test user
            assertTrue(deleteUser(uid));
        }
    }

    @Test
    void addUserNoResults() throws SQLException {
        // given: a data source such that the result set is empty
        DataSource dataSource = mockEmptyResultSet();

        // use the mocked data source, storing the original
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(dataSource);

        // when: checking if the email exists
        // then: should not exist
        Integer userId = addUser(TEST_USERNAME, TEST_EMAIL, "password");
        assertNull(userId);

        // after: reset data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void deleteUserThatDoesNotExist() {
        // given: a non-existing user (uid = -1)
        int userId = -1;

        // when: deleting the user, then: the user is not deleted
        assertFalse(deleteUser(userId));
    }
}

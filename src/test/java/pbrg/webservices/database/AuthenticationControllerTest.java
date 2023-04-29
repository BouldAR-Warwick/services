package pbrg.webservices.database;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static pbrg.webservices.database.AuthenticationController.addUser;
import static pbrg.webservices.database.AuthenticationController.deleteUser;
import static pbrg.webservices.database.AuthenticationController.emailExists;
import static pbrg.webservices.database.AuthenticationController.getUserIDFromEmail;
import static pbrg.webservices.database.AuthenticationController.getUserIDFromUsername;
import static pbrg.webservices.database.AuthenticationController.signUp;
import static pbrg.webservices.database.AuthenticationController.userExists;
import static pbrg.webservices.database.AuthenticationController.usernameExists;
import static pbrg.webservices.database.DatabaseTestMethods.mockThrowsExceptionOnGetConnection;
import static pbrg.webservices.database.DatabaseTestMethods.mockThrowsExceptionOnConnectionClose;
import static pbrg.webservices.database.DatabaseTestMethods.mockThrowsExceptionOnExecuteQuery;
import static pbrg.webservices.database.DatabaseTestMethods.mockThrowsExceptionOnNext;
import static pbrg.webservices.database.DatabaseTestMethods.mockThrowsExceptionOnSetString;
import static pbrg.webservices.database.DatabaseTestMethods.mockEmptyResultSet;
import static pbrg.webservices.database.DatabaseTestMethods.mockThrowsExceptionOnPrepareStatement;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pbrg.webservices.models.User;

public final class AuthenticationControllerTest {

    /** The test username. */
    public static final String TEST_USERNAME = "username_test";

    /** The test email. */
    public static final String TEST_EMAIL = "email_test";

    /** The test password. */
    public static final String TEST_PASSWORD = "password";

    static void clearTestUser() {
        // remove the test user credentials if they exist
        if (usernameExists(TEST_USERNAME)) {
            Integer uid =
                getUserIDFromUsername(TEST_USERNAME);
            assertNotNull(uid);
            assertTrue(deleteUser(uid));
            assertFalse(userExists(uid));
            assertFalse(usernameExists(TEST_USERNAME));
        }
        if (emailExists(TEST_EMAIL)) {
            Integer uid = getUserIDFromEmail(TEST_EMAIL);
            assertNotNull(uid);
            assertTrue(deleteUser(uid));
            assertFalse(userExists(uid));
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
        assertFalse(usernameExists(TEST_USERNAME));
        assertFalse(emailExists(TEST_EMAIL));
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

    @BeforeEach
    void ensureTestUserIsValid() {
        ensureTestUserIsRemoved();
    }

    @AfterEach
    void ensureTestUserIsRemoved() {
        assertNotNull(TEST_USERNAME);
        assertFalse(usernameExists(TEST_USERNAME));
        assertFalse(emailExists(TEST_EMAIL));
    }

    @Test
    void testPrivateConstructor() {
        // get constructor
        Constructor<AuthenticationController> constructor;
        try {
            constructor = AuthenticationController.class
                .getDeclaredConstructor();
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
            int userId = createTestUser();

            // when: signing in
            User testUser = AuthenticationController
                .signIn(TEST_USERNAME, TEST_PASSWORD);

            // then: should retrieve user
            assertNotNull(testUser);
            assertEquals(TEST_USERNAME, testUser.getUsername());
            Integer userIdFromUsername = getUserIDFromUsername(TEST_USERNAME);
            assertNotNull(userIdFromUsername);
            assertEquals(userId, userIdFromUsername);
            assertEquals(testUser.getUid(), userId);

            // after: remove the test user
            assertTrue(deleteUser(userId));
            assertFalse(userExists(userId));
            assertFalse(usernameExists(TEST_USERNAME));
            assertFalse(emailExists(TEST_EMAIL));
        }

        @Test
        void signInConnectionCloseFails() {
            assertFalse(usernameExists(TEST_USERNAME));
            assertFalse(emailExists(TEST_EMAIL));

            DatabaseController.setDataSource(
                mockThrowsExceptionOnConnectionClose()
            );

            assertThrows(
                // then: an exception is thrown
                NullPointerException.class,

                // when: signing in
                () -> AuthenticationController
                    .signIn(TEST_USERNAME, TEST_PASSWORD)
            );

            // after: reset data source
            DatabaseController.setDataSource(getTestDataSource());

            // after: clean up
            clearTestUser();
        }

        @Test
        void signInInvalidUser() {
            // given: a user that does not exist in the system
            assertFalse(usernameExists(TEST_USERNAME));
            assertFalse(emailExists(TEST_EMAIL));

            // when: signing in
            User testUser =
                AuthenticationController.signIn(TEST_USERNAME, TEST_PASSWORD);

            // then: should not retrieve user
            assertNull(testUser);
            Integer testUid =
                getUserIDFromUsername(TEST_USERNAME);
            assertNull(testUid);
        }

        @Test
        void signInExecuteQueryFails() {
            // mock database objects
            DatabaseController.setDataSource(
                mockThrowsExceptionOnExecuteQuery()
            );

            // when: signing in and experience SQLException
            User user = AuthenticationController
                .signIn(TEST_USERNAME, TEST_PASSWORD);

            // then: should not retrieve user
            assertNull(user);

            // after: reset data source
            DatabaseController.setDataSource(getTestDataSource());
        }

        @Test
        void signInSetStringFails() {
            // mock database objects
            DatabaseController.setDataSource(
                mockThrowsExceptionOnSetString()
            );

            // when: signing in
            User user = AuthenticationController
                .signIn(TEST_USERNAME, TEST_PASSWORD);

            // then: should not retrieve user
            assertNull(user);

            // after: reset data source
            DatabaseController.setDataSource(getTestDataSource());
        }

        @Test
        void signInPreparedStatementFails() {
            // mock database objects
            DatabaseController.setDataSource(
                mockThrowsExceptionOnPrepareStatement()
            );

            // when: signing in
            User user = AuthenticationController
                .signIn(TEST_USERNAME, TEST_PASSWORD);

            // then: should not retrieve user
            assertNull(user);

            // after: reset data source
            DatabaseController.setDataSource(getTestDataSource());
        }

        @Test
        void signInResultsSetThrowsException() {
            assertFalse(usernameExists(TEST_USERNAME));
            assertFalse(emailExists(TEST_EMAIL));

            // mock database objects
            DatabaseController.setDataSource(
                mockThrowsExceptionOnNext()
            );

            // when: signing in
            User user = AuthenticationController
                .signIn(TEST_USERNAME, TEST_PASSWORD);

            // then: should not retrieve user
            assertNull(user);

            // after: reset data source
            DatabaseController.setDataSource(getTestDataSource());
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
        assertFalse(userExists(uid));
    }

    @Test
    void signUpConflictingCredentials() {
        // given: an existing user
        String username = "username_existing";
        String email = "email_existing";
        assertFalse(usernameExists(username));
        assertFalse(emailExists(email));

        boolean added = signUp(
            username, email, "password"
        );
        assertTrue(added);
        assertTrue(usernameExists(username));
        assertTrue(emailExists(email));

        // given: a non-existing username, email
        assertFalse(usernameExists(TEST_USERNAME));
        assertFalse(emailExists(TEST_EMAIL));

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

        for (boolean request : invalidRequests) {
            // then: the user is not added
            assertFalse(request);
        }

        // after: ensure test user not created
        assertFalse(usernameExists(TEST_USERNAME));
        assertFalse(emailExists(TEST_EMAIL));

        // after: remove the existing user
        Integer uid = getUserIDFromUsername(username);
        assertNotNull(uid);
        assertTrue(deleteUser(uid));
        assertFalse(userExists(uid));
        assertFalse(usernameExists(username));
        assertFalse(emailExists(email));
    }

    @Test
    void usernameExistsUserDoesNotExist() {
        // given: a non-existing username
        // when: checking if the username exists
        // then: the username does not exist
        assertFalse(usernameExists(TEST_USERNAME));
    }

    @Test
    void usernameExistsNoResults() {
        // given: a data source such that the result set is empty
        DatabaseController.setDataSource(mockEmptyResultSet());

        // when: checking if the username exists
        // then: should not exist
        assertFalse(usernameExists(TEST_USERNAME));

        // after: reset data source
        DatabaseController.setDataSource(getTestDataSource());
    }

    @Test
    void emailExistsNoResults() {
        // given: a data source such that the result set is empty
        DatabaseController.setDataSource(mockEmptyResultSet());

        // when: checking if the email exists
        // then: should not exist
        assertFalse(emailExists(TEST_EMAIL));

        // after: reset data source
        DatabaseController.setDataSource(getTestDataSource());
    }

    @Test
    void userExistsNoResults() {
        // given: a data source such that the result set is empty
        DatabaseController.setDataSource(mockEmptyResultSet());
        int userId = -1;

        // when: checking if the email exists
        // then: should not exist
        assertFalse(userExists(userId));

        // after: reset data source
        DatabaseController.setDataSource(getTestDataSource());
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
            assertFalse(usernameExists(TEST_USERNAME));
            assertFalse(emailExists(TEST_EMAIL));

            // add the test user
            boolean added = signUp(TEST_USERNAME, TEST_EMAIL, "password");
            assertTrue(added);
            assertTrue(emailExists(TEST_EMAIL));

            // when: getting the user ID from the email
            // then: the user ID is not null
            Integer userId = getUserIDFromEmail(TEST_EMAIL);
            assertNotNull(userId);
            Integer userIdFromUsername = getUserIDFromUsername(TEST_USERNAME);
            assertNotNull(userIdFromUsername);
            assertEquals(userId, userIdFromUsername);

            // after: remove the test user
            assertTrue(deleteUser(userIdFromUsername));
            assertFalse(usernameExists(TEST_USERNAME));
            assertFalse(emailExists(TEST_EMAIL));
        }
    }

    @Test
    void addUserNoResults() {
        // given: a data source such that the result set is empty
        DatabaseController.setDataSource(mockEmptyResultSet());

        // when: checking if the email exists
        // then: should not exist
        Integer userId = addUser(TEST_USERNAME, TEST_EMAIL, "password");
        assertNull(userId);

        // after: reset data source
        DatabaseController.setDataSource(getTestDataSource());
    }

    @Test
    void deleteUserThatDoesNotExist() {
        // given: a non-existing user (uid = -1)
        int userId = -1;

        // when: deleting the user
        boolean deleted = deleteUser(userId);

        // then: the user is not deleted
        assertFalse(deleted);
    }

    @Test
    void userExistsThrowing() {
        // given: a data source that throws an exception
        DatabaseController.setDataSource(mockThrowsExceptionOnGetConnection());

        int userId = -1;

        // when: checking if the user exists
        boolean userExists = userExists(userId);

        // then: the user does not exist
        assertFalse(userExists);

        // after: reset data source
        DatabaseController.setDataSource(getTestDataSource());
    }

    @Test
    void getUserIDFromUsernameThrowing() {
        // given: a data source that throws an exception
        DatabaseController.setDataSource(mockThrowsExceptionOnGetConnection());
        String username = TEST_USERNAME;
        assertFalse(usernameExists(username));

        // when: getting the user ID from the username
        Integer userId = getUserIDFromUsername(username);

        // then: the user ID is null
        assertNull(userId);

        // after: reset data source
        DatabaseController.setDataSource(getTestDataSource());
    }

    @Test
    void getUserIDFromEmailThrowing() {
        // given: a data source that throws an exception
        DatabaseController.setDataSource(mockThrowsExceptionOnGetConnection());
        String email = TEST_EMAIL;
        assertFalse(emailExists(email));

        // when: getting the user ID from the email
        Integer userId = getUserIDFromEmail(email);

        // then: the user ID is null
        assertNull(userId);

        // after: reset data source
        DatabaseController.setDataSource(getTestDataSource());
    }

    @Test
    void deleteUserThrowing() {
        // given: a data source that throws an exception
        DatabaseController.setDataSource(mockThrowsExceptionOnGetConnection());
        int userId = -1;

        // when: deleting the user
        boolean deleted = deleteUser(userId);

        // then: the user is not deleted
        assertFalse(deleted);

        // after: reset data source
        DatabaseController.setDataSource(getTestDataSource());
    }
}

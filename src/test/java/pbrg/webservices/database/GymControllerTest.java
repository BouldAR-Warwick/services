package pbrg.webservices.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pbrg.webservices.database.AuthenticationController.deleteUser;
import static pbrg.webservices.database.AuthenticationController.userExists;
import static pbrg.webservices.database.AuthenticationControllerTest
    .createTestUser;
import static pbrg.webservices.database.DatabaseTestMethods.mockEmptyResultSet;
import static pbrg.webservices.database.GymController.addGym;
import static pbrg.webservices.database.GymController.deleteGym;
import static pbrg.webservices.database.GymController.getGym;
import static pbrg.webservices.database.GymController.getPrimaryGymOfUser;
import static pbrg.webservices.database.GymController.getGymIdByGymName;
import static pbrg.webservices.database.GymController.gymExists;
import static pbrg.webservices.database.GymController.removeUserPrimaryGym;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pbrg.webservices.models.Gym;

public final class GymControllerTest {

    /** The test gym name. */
    public static final String TEST_GYM_NAME = "gym_name";

    /** The test gym location. */
    private static final String TEST_GYM_LOCATION = "Warwick, UK";

    /**
     * Create a test gym using the test gym credentials.
     * @return the gym ID
     */
    public static int createTestGym() {
        // ensure the test gym does not exist
        if (gymExists(TEST_GYM_NAME)) {
            // get the gym id, delete
            Integer gymId = getGymIdByGymName(TEST_GYM_NAME);
            assertNotNull(gymId);
            assertTrue(deleteGym(gymId));
        }
        assertFalse(gymExists(TEST_GYM_NAME));

        Integer gymId = addGym(TEST_GYM_NAME, TEST_GYM_LOCATION);
        assertNotNull(gymId);
        return gymId;
    }

    /**
     * Mocks a data source that returns an empty result set.
     *
     * @return the mocked data source
     * @throws SQLException if the data source cannot be mocked
     */
    private static @NotNull DataSource mockExecuteUpdateReturningZero()
        throws SQLException {
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        Connection connection = mock(Connection.class);
        when(connection.prepareStatement(anyString()))
            .thenReturn(preparedStatement);

        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(connection);

        return dataSource;
    }

    @BeforeAll
    static void startResources() throws IllegalStateException {
        startTestDatabaseInThread();

        // use the test database
        DatabaseController.setDataSource(getTestDataSource());

        // ensure the test gym does not exist
        if (gymExists(TEST_GYM_NAME)) {
            // get the gym id, delete
            Integer gymId = getGymIdByGymName(TEST_GYM_NAME);
            assertNotNull(gymId);
            assertTrue(deleteGym(gymId));
        }
        assertFalse(gymExists(TEST_GYM_NAME));
    }

    @BeforeEach
    void dbSanityCheck() {
        // ensure the test gym does not exist
        assertFalse(gymExists(TEST_GYM_NAME));
    }

    @AfterAll
    static void closeResources() {
        closeTestDatabaseInThread();
    }

    @Test
    void testPrivateConstructor() {
        // get constructor
        Constructor<GymController> constructor;
        try {
            constructor = GymController.class.getDeclaredConstructor();
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
    void addNewGym() {
        // given: a new gym
        // when: adding the gym
        int gymId = createTestGym();

        // ensure the gym exists
        assertTrue(gymExists(gymId));
        assertTrue(GymController
            .getGymsByQueryWord(TEST_GYM_NAME).size() > 0);

        // after: remove the gym
        assertTrue(deleteGym(gymId));
        assertFalse(gymExists(gymId));
        assertFalse(gymExists(TEST_GYM_NAME));
    }

    @Test
    void addNewGymInvalidName() {
        // given: a name that exceeds the length cap
        String gymName = "a".repeat(GymController.GYM_NAME_LENGTH_CAP + 1);

        // when: adding the gym
        boolean added = addGym(gymName, TEST_GYM_LOCATION) != null;

        // then: the gym is not added
        assertFalse(added);
        assertFalse(gymExists(gymName));
    }

    @Test
    void addNewGymInvalidLocation() {
        // given: a location that exceeds the length cap
        String gymLocation =
            "a".repeat(GymController.GYM_LOCATION_LENGTH_CAP + 1);

        // when: adding the gym
        boolean added = addGym(TEST_GYM_NAME, gymLocation) != null;

        // then: the gym is not added
        assertFalse(added);
        assertFalse(gymExists(TEST_GYM_NAME));
    }

    @Test
    void addNewGymNameExists() {
        // given: a conflicting name

        // add the test gym
        int gymId = createTestGym();
        assertTrue(gymExists(gymId));

        // when: adding a gym with a conflicting name
        boolean added = addGym(TEST_GYM_NAME, TEST_GYM_LOCATION) != null;

        // then: the gym is not added
        assertFalse(added);

        // after: remove the gym
        assertTrue(deleteGym(gymId));
        assertFalse(gymExists(TEST_GYM_NAME));
    }

    @Test
    void insertGymConflictingName()
        throws IllegalAccessException {
        // given: a conflicting name

        // add the test gym
        Integer gymId = addGym(TEST_GYM_NAME, TEST_GYM_LOCATION);
        assertNotNull(gymId);
        assertTrue(gymExists(gymId));

        // use reflection to access insertGym
        Method insertGym;
        try {
            insertGym = GymController.class.getDeclaredMethod(
                "insertGym", String.class, String.class);
        } catch (NoSuchMethodException e) {
            fail("GymController should have a private insertGym method");
            throw new RuntimeException(e);
        }
        insertGym.setAccessible(true);

        try {
            // when: adding a gym with a conflicting name
            insertGym.invoke(null, TEST_GYM_NAME, TEST_GYM_LOCATION);
        } catch (InvocationTargetException e) {
            // then: the gym is not added
            assertTrue(e.getCause() instanceof SQLException);
        }

        // after: remove the gym
        assertTrue(deleteGym(gymId));
        assertFalse(gymExists(TEST_GYM_NAME));
    }

    @Test
    void insertGymEmptyResultSet() throws
        IllegalAccessException, InvocationTargetException {

        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(mockEmptyResultSet());

        // use reflection to access insertGym
        Method insertGym;
        try {
            insertGym = GymController.class.getDeclaredMethod(
                "insertGym", String.class, String.class);
        } catch (NoSuchMethodException e) {
            fail("GymController should have a private insertGym method");
            throw new RuntimeException(e);
        }
        insertGym.setAccessible(true);

        // when: adding a gym
        Integer gymId =
            (Integer) insertGym.invoke(null, TEST_GYM_NAME, TEST_GYM_LOCATION);

        // then: the gym is not added
        assertNull(gymId);

        // after: restore the original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void gymExistsEmptyResultSet() {
        // add the test gym
        Integer gymId = addGym(TEST_GYM_NAME, TEST_GYM_LOCATION);
        assertNotNull(gymId);

        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(mockEmptyResultSet());

        // when: checking if the gym exists
        boolean exists = gymExists(gymId);

        // then: the gym does not exist
        assertFalse(exists);

        // after: restore the original data source
        DatabaseController.setDataSource(originalDataSource);

        // after: remove the gym
        assertTrue(deleteGym(gymId));
        assertFalse(gymExists(gymId));
    }

    @Test
    void gymExistsByNameEmptyResultSet() {
        // add the test gym
        Integer gymId = addGym(TEST_GYM_NAME, TEST_GYM_LOCATION);
        assertNotNull(gymId);

        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(mockEmptyResultSet());

        // when: checking if the gym exists
        boolean exists = gymExists(TEST_GYM_NAME);

        // then: the gym does not exist
        assertFalse(exists);

        // after: restore the original data source
        DatabaseController.setDataSource(originalDataSource);

        // after: remove the gym
        assertTrue(deleteGym(gymId));
        assertFalse(gymExists(gymId));
    }

    @Test
    void deleteNonExistentGym() {
        // given: a non-existent gym id
        int gymId = -1;
        assertFalse(gymExists(gymId));

        // when: deleting the gym
        boolean deleted = deleteGym(gymId);

        // then: the gym is not deleted
        assertFalse(deleted);
    }

    @Test
    void getGymByGymNameNonExistentGym() {
        // given: a non-existent gym name
        assertFalse(gymExists(TEST_GYM_NAME));

        // when: getting the gym
        Gym gym = GymController.getGymByGymName(TEST_GYM_NAME);

        // then: the gym is not found
        assertNull(gym);
    }

    @Test
    void getGymByGymNameExistingGym() {
        // given: an existing gym
        int gymId = createTestGym();
        assertTrue(gymExists(gymId));

        // when: getting the gym
        Gym gym = GymController.getGymByGymName(TEST_GYM_NAME);

        // then: the gym is found
        assertNotNull(gym);
        assertEquals(gymId, gym.getGid());
        assertEquals(TEST_GYM_NAME, gym.getGymName());
        assertEquals(TEST_GYM_LOCATION, gym.getGymLocation());

        // after: remove the gym
        assertTrue(deleteGym(gymId));
        assertFalse(gymExists(TEST_GYM_NAME));
    }

    @Test
    void addGymToUserBothValid() {
        // given: a valid user and gym without a primary gym relationship
        int uid = createTestUser();
        int gid = createTestGym();
        assertTrue(userExists(uid));
        assertTrue(gymExists(gid));
        assertNull(getPrimaryGymOfUser(uid));

        // when: adding the gym to the user
        boolean added = GymController.setPrimaryGym(uid, gid);

        // then: the gym is added to the user
        assertTrue(added);

        // after: remove both and connection
        assertTrue(removeUserPrimaryGym(uid));
        assertTrue(deleteUser(uid));
        assertTrue(deleteGym(gid));
        assertFalse(gymExists(gid));
    }

    @Test
    void addGymToUserNoResult() throws SQLException {
        // mock a executeUpdate that returns 0
        DataSource dataSource = mockExecuteUpdateReturningZero();

        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(dataSource);

        // when: adding gym to user
        boolean added = GymController.setPrimaryGym(-1, -1);

        // then: the gym is not added to the user
        assertFalse(added);

        // after: restore the original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void addGymToUserInvalidGym() {
        // given: a valid user and an invalid gym
        int uid = createTestUser();
        int gid = -1;

        // when: adding the gym to the user
        boolean added = GymController.setPrimaryGym(uid, gid);

        // then: the gym is not added to the user
        assertFalse(added);

        // after: remove user
        assertTrue(deleteUser(uid));
    }

    @Test
    void addGymToUserInvalidUser() {
        // given: an invalid user and a valid gym
        int uid = -1;
        int gid = createTestGym();

        // when: adding the gym to the user
        boolean added = GymController.setPrimaryGym(uid, gid);

        // then: the gym is not added to the user
        assertFalse(added);

        // after: remove gym
        assertTrue(deleteGym(gid));
    }

    @Test
    void getGymByUserIdTest() {
        // given: a valid user and gym, gym is user's primary gym
        int uid = createTestUser();
        int gid = createTestGym();
        assertTrue(userExists(uid));
        assertTrue(gymExists(gid));
        assertNull(getPrimaryGymOfUser(uid));
        boolean added = GymController.setPrimaryGym(uid, gid);
        assertTrue(added);

        // when: getting the gym by user id
        // then: the gym is found
        Integer gymId = getPrimaryGymOfUser(uid);
        assertNotNull(gymId);
        Gym gym = getGym(gymId);
        assertNotNull(gym);
        assertEquals(gid, gym.getGid());

        // after: remove both and connection
        assertTrue(removeUserPrimaryGym(uid));
        assertTrue(deleteUser(uid));
        assertTrue(deleteGym(gid));
    }

    @Test
    void removeUserPrimaryGymExecuteUpdateEmpty() throws SQLException {
        // mock a executeUpdate that returns 0
        DataSource dataSource = mockExecuteUpdateReturningZero();

        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(dataSource);

        // when: adding gym to user
        boolean added = GymController.removeUserPrimaryGym(-1);

        // then: the gym is not added to the user
        assertFalse(added);

        // after: restore the original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void getGymIdByGymNameNoGym() {
        // given: a non-existent gym name
        assertFalse(gymExists(TEST_GYM_NAME));

        // when: getting the gym id
        Integer gymId = GymController.getGymIdByGymName(TEST_GYM_NAME);

        // then: the gym id is not found
        assertNull(gymId);
    }

    @Test
    void getGymIdByGymNameValid() {
        // given: an existing gym
        int gymId = createTestGym();
        assertTrue(gymExists(gymId));

        // when: getting the gym id
        Integer gymIdByName = getGymIdByGymName(TEST_GYM_NAME);

        // then: the gym id is found
        assertNotNull(gymIdByName);
        assertEquals(gymId, gymIdByName);

        // after: remove the gym
        assertTrue(deleteGym(gymId));
        assertFalse(gymExists(gymId));
        assertFalse(gymExists(TEST_GYM_NAME));
    }
}

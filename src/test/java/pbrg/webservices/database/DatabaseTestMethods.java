package pbrg.webservices.database;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.jetbrains.annotations.NotNull;

public final class DatabaseTestMethods {

    /** Private constructor to prevent instantiation. */
    private DatabaseTestMethods() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates a mock DataSource that throws an SQLException when
     * getConnection() is called.
     * @return the mock DataSource
     */
    public static @NotNull DataSource mockEmptyResultSet() {
        // mock the result set
        ResultSet resultSet = mock(ResultSet.class);
        try {
            when(resultSet.next()).thenReturn(false);
        } catch (SQLException e) {
            fail("SQLException should not be thrown");
        }

        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        try {
            when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
        } catch (SQLException e) {
            fail("SQLException should not be thrown");
        }

        Connection connection = mock(Connection.class);
        try {
            when(connection.prepareStatement(
                anyString(), eq(Statement.RETURN_GENERATED_KEYS)
            )).thenReturn(preparedStatement);
            when(connection.prepareStatement(anyString()))
                .thenReturn(preparedStatement);
        } catch (SQLException e) {
            fail("SQLException should not be thrown");
        }

        DataSource dataSource = mock(DataSource.class);
        try {
            when(dataSource.getConnection()).thenReturn(connection);
        } catch (SQLException e) {
            fail("SQLException should not be thrown");
        }

        return dataSource;
    }

    /**
     * Mocks a data source that affects no rows.
     * @return the mocked data source
     */
    public static @NotNull DataSource mockNoAffectedRows() {
        DataSource mockDataSource = mockEmptyResultSet();
        PreparedStatement mockPreparedStatement = null;
        try {
            mockPreparedStatement =
                mockDataSource.getConnection().prepareStatement("");
        } catch (SQLException e) {
            fail("SQLException should not be thrown");
        }
        try {
            when(mockPreparedStatement.executeUpdate())
                .thenReturn(0);
        } catch (SQLException e) {
            fail("SQLException should not be thrown");
        }
        return mockDataSource;
    }

    /**
     * Mocks a data source that throws an exception when getConnection() is
     * called.
     * @return the mocked data source
     */
    public static @NotNull DataSource mockThrowsExceptionOnGetConnection() {
        DataSource mockDataSource = mock(DataSource.class);
        try {
            when(mockDataSource.getConnection()).thenThrow(SQLException.class);
        } catch (SQLException e) {
            fail("SQLException should not be thrown");
        }
        return mockDataSource;
    }

    /**
     * Mocks a data source that throws an exception when closing the connection.
     * @return the mocked data source
     */
    public static @NotNull DataSource mockThrowsExceptionOnConnectionClose() {
        Connection connection = mock(Connection.class);
        try {
            doThrow(SQLException.class).when(connection).close();
        } catch (SQLException e) {
            fail("SQLException should not be thrown");
        }

        DataSource mockedDataSource = mock(DataSource.class);
        try {
            when(mockedDataSource.getConnection())
                .thenReturn(connection);
        } catch (SQLException e) {
            fail("SQLException should not be thrown");
        }

        // verify
        Connection mockedConnection = null;
        try {
            mockedConnection = mockedDataSource.getConnection();
        } catch (SQLException e) {
            fail("SQLException should not be thrown");
        }
        assertThrows(
            // then: an exception is thrown
            SQLException.class,

            // when: closing the connection
            mockedConnection::close
        );

        return mockedDataSource;
    }

    /**
     * Mocks a data source that throws an exception when checking if the
     * result set has a next row.
     * @return the mocked data source
     */
    public static @NotNull DataSource mockThrowsExceptionOnNext() {
        // data source -> connection -> prepared statement -> result set
        ResultSet mockedResultSet = mock(ResultSet.class);
        PreparedStatement mockedPreparedStatement =
            mock(PreparedStatement.class);
        Connection mockedConnection = mock(Connection.class);
        DataSource mockedDataSource = mock(DataSource.class);

        try {
            when(mockedResultSet.next())
                .thenThrow(new SQLException());
            when(mockedPreparedStatement.executeQuery())
                .thenReturn(mockedResultSet);
            when(mockedConnection.prepareStatement(anyString()))
                .thenReturn(mockedPreparedStatement);
            when(mockedDataSource.getConnection())
                .thenReturn(mockedConnection);
        } catch (SQLException e) {
            fail("SQLException should not be thrown");
        }

        // verify
        ResultSet resultSet = null;
        try {
            resultSet = mockedDataSource.getConnection()
                .prepareStatement("").executeQuery();
        } catch (SQLException e) {
            fail("SQLException should not be thrown");
        }
        assertThrows(
            // then: an exception is thrown
            SQLException.class,

            // when: signing in
            resultSet::next
        );

        return mockedDataSource;
    }

    /**
     * Mocks a data source that throws an exception when executing a query.
     * @return the mocked data source
     */
    public static @NotNull DataSource mockThrowsExceptionOnExecuteQuery() {
        // data source -> connection -> prepared statement -> result set
        PreparedStatement mockedPreparedStatement =
            mock(PreparedStatement.class);
        Connection mockedConnection = mock(Connection.class);
        DataSource mockedDataSource = mock(DataSource.class);

        try {
            when(mockedPreparedStatement.executeQuery())
                .thenThrow(new SQLException());
            when(mockedConnection.prepareStatement(anyString()))
                .thenReturn(mockedPreparedStatement);
            when(mockedDataSource.getConnection())
                .thenReturn(mockedConnection);
        } catch (SQLException e) {
            fail("SQLException should not be thrown");
        }

        // verify
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = mockedDataSource.getConnection()
                .prepareStatement("");
        } catch (SQLException e) {
            fail("SQLException should not be thrown");
        }
        assertThrows(
            // then: an exception is thrown
            SQLException.class,

            // when: signing in
            preparedStatement::executeQuery
        );

        return mockedDataSource;
    }

    /**
     * Mocks a data source that throws an exception when setting a string
     * on the prepared statement.
     * @return the mocked data source
     */
    public static @NotNull DataSource mockThrowsExceptionOnSetString() {
        // data source -> connection -> prepared statement -> result set
        PreparedStatement mockedPreparedStatement =
            mock(PreparedStatement.class);
        Connection mockedConnection = mock(Connection.class);
        DataSource mockedDataSource = mock(DataSource.class);

        try {
        doThrow(new SQLException()).when(mockedPreparedStatement)
            .setString(anyInt(), anyString());
        when(mockedConnection.prepareStatement(anyString()))
            .thenReturn(mockedPreparedStatement);
        when(mockedDataSource.getConnection()).thenReturn(mockedConnection);
        } catch (SQLException e) {
            fail("SQLException should not be thrown");
        }

        // validate
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = mockedDataSource.getConnection()
                    .prepareStatement("");
        } catch (SQLException e) {
            fail("SQLException should not be thrown");
        }
        PreparedStatement finalPreparedStatement = preparedStatement;
        assertThrows(
            // then: an exception is thrown
            SQLException.class,

            // when: signing in
            () -> finalPreparedStatement.setString(1, "")
        );

        return mockedDataSource;
    }

    /**
     * Mocks a data source that throws and exception
     * when calling prepare statement on its connection.
     * @return the mocked data source
     */
    public static @NotNull DataSource mockThrowsExceptionOnPrepareStatement() {
        Connection mockedConnection = mock(Connection.class);
        DataSource mockedDataSource = mock(DataSource.class);

        try {
            when(mockedConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException());
            when(mockedDataSource.getConnection())
                .thenReturn(mockedConnection);
        } catch (SQLException e) {
            fail("SQLException should not be thrown");
        }

        // validate
        Connection connection = null;
        try {
            connection = mockedDataSource.getConnection();
        } catch (SQLException e) {
            fail("SQLException should not be thrown");
        }
        Connection finalConnection = connection;
        assertThrows(
            // then: an exception is thrown
            SQLException.class,

            // when: signing in
            () -> finalConnection.prepareStatement("")
        );

        return mockedDataSource;
    }
}

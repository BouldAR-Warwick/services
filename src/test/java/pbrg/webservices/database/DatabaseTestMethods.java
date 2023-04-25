package pbrg.webservices.database;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
     * Mocks a data source that returns an empty result set.
     * @return the mocked data source
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
    public static @NotNull DataSource mockConnectionThrowsException() {
        DataSource mockDataSource = mock(DataSource.class);
        try {
            when(mockDataSource.getConnection()).thenThrow(SQLException.class);
        } catch (SQLException e) {
            fail("SQLException should not be thrown");
        }
        return mockDataSource;
    }
}

package pbrg.webservices.database;

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
     * @throws SQLException if the data source cannot be mocked
     */
    public static @NotNull DataSource mockEmptyResultSet()
        throws SQLException {
        // mock the result set
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);

        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        Connection connection = mock(Connection.class);
        when(connection.prepareStatement(
            anyString(), eq(Statement.RETURN_GENERATED_KEYS)
        )).thenReturn(preparedStatement);
        when(connection.prepareStatement(anyString()))
            .thenReturn(preparedStatement);

        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(connection);

        return dataSource;
    }

    /**
     * Mocks a data source that affects no rows.
     * @return the mocked data source
     * @throws SQLException if the data source cannot be mocked
     */
    public static @NotNull DataSource mockNoAffectedRows()
        throws SQLException {
        DataSource mockDataSource = mockEmptyResultSet();
        PreparedStatement mockPreparedStatement =
            mockDataSource.getConnection().prepareStatement("");
        when(mockPreparedStatement.executeUpdate())
            .thenReturn(0);
        return mockDataSource;
    }

    /**
     * Mocks a data source that throws an exception when getConnection() is
     * called.
     * @return the mocked data source
     * @throws SQLException if the data source cannot be mocked
     */
    public static @NotNull DataSource mockConnectionThrowsException()
        throws SQLException {
        DataSource mockDataSource = mock(DataSource.class);
        when(mockDataSource.getConnection()).thenThrow(SQLException.class);
        return mockDataSource;
    }
}

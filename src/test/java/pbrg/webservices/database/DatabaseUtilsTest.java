package pbrg.webservices.database;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class DatabaseUtilsTest {

    @Test
    void invalidDataSources() throws SQLException {
        // DataSource without a connection
        DataSource sourceWithoutConn = mock(DataSource.class);
        when(sourceWithoutConn.getConnection()).thenThrow(new SQLException("Connection failed"));

        // DataSource with a null connection
        DataSource sourceWithNullConn = mock(DataSource.class);
        when(sourceWithNullConn.getConnection()).thenReturn(null);

        DataSource[] invalidSources = {null, sourceWithoutConn, sourceWithNullConn};

        for (DataSource source : invalidSources) {
            assertFalse(DatabaseUtils.dataSourceIsValid(source));
        }
    }

    @Test
    void validDataSource() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(mock(java.sql.Connection.class));

        assertTrue(DatabaseUtils.dataSourceIsValid(dataSource));
    }
}
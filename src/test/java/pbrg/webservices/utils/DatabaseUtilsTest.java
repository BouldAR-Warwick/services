package pbrg.webservices.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

final class DatabaseUtilsTest {

    @Test
    void testPrivateConstructor() {
        // get constructor
        Constructor<DatabaseUtils> constructor;
        try {
            constructor = DatabaseUtils.class.getDeclaredConstructor();
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
    void invalidDataSources() throws SQLException {
        // DataSource without a connection
        DataSource sourceWithoutConn = mock(DataSource.class);
        when(sourceWithoutConn.getConnection())
            .thenThrow(new SQLException("Connection failed"));

        // DataSource with a null connection
        DataSource sourceWithNullConn = mock(DataSource.class);
        when(sourceWithNullConn.getConnection()).thenReturn(null);

        DataSource[] invalidSources =
            {null, sourceWithoutConn, sourceWithNullConn};

        for (DataSource source : invalidSources) {
            assertFalse(DatabaseUtils.dataSourceIsValid(source));
        }
    }

    @Test
    void validDataSource() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection())
            .thenReturn(mock(java.sql.Connection.class));

        assertTrue(DatabaseUtils.dataSourceIsValid(dataSource));
    }
}

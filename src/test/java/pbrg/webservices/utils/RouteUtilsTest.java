package pbrg.webservices.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static pbrg.webservices.utils.RouteUtils.generateRouteMoonBoard;
import static pbrg.webservices.utils.RouteUtils.plotHoldsOnImagePython;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;

final class RouteUtilsTest {
    /** The average bouldering grade worldwide is V5. */
    private static final int AVERAGE_GRADE = 5;

    /** The example route ID. */
    private static final int EXAMPLE_ROUTE_ID = 500;

    @Test
    void testPrivateConstructor() {
        // get constructor
        Constructor<RouteUtils> constructor;
        try {
            constructor = RouteUtils.class.getDeclaredConstructor();
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
    void generateRouteMoonBoardTest() {
        // given: grade

        // when: generate route
        JSONArray result = generateRouteMoonBoard(AVERAGE_GRADE);

        // then: result is not null
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertFalse(result.toString().isBlank());
    }

    @Test
    void plotHoldsOnImage() {
        // given: holds, paths, route ID, working directory
        JSONArray holds = generateRouteMoonBoard(AVERAGE_GRADE);
        assertNotNull(holds);
        assertFalse(holds.toString().isBlank());
        String wallImageFilePath = "MoonBoard2016.jpg";
        String workingDirectory = System.getProperty("user.dir");

        // when: plotting holds on image
        String newFile = plotHoldsOnImagePython(
            EXAMPLE_ROUTE_ID, wallImageFilePath,
            workingDirectory, workingDirectory,
            holds
        );

        // then: file is successfully created
        assertNotNull(newFile);
    }
}

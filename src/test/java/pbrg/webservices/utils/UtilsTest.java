package pbrg.webservices.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UtilsTest {

    /** The average bouldering grade worldwide is V5. */
    private static final int AVERAGE_GRADE = 5;

    /** The example route ID. */
    private static final int EXAMPLE_ROUTE_ID = 500;

    @BeforeAll
    static void setUp() {
        if (!TestUtils.python3Installed()) {
            throw new RuntimeException("Python 3 not installed.");
        }
    }

    @Test
    void getContentType() {
        assertEquals("image/jpeg", Utils.getContentType("jpg"));

        // ensure non keys are mapped to null
        String[] nonKeys = {null, "", "not-in-map"};
        for (String nonKey : nonKeys) {
            assertNull(Utils.getContentType(nonKey));
        }
    }

    @Test
    void generateRouteMoonBoard() {
        // given: grade

        // when: generate route
        JSONArray result = Utils.generateRouteMoonBoard(AVERAGE_GRADE);

        // then: result is not null
        assertNotNull(result);
        assert !result.isEmpty();
        assert !result.toString().isBlank();
    }

    @Test
    void plotHoldsOnImage() {
        // given: holds, paths, route ID, working directory
        JSONArray holds = Utils.generateRouteMoonBoard(AVERAGE_GRADE);
        assertNotNull(holds);
        assert !holds.toString().isBlank();
        String wallImageFilePath = "MoonBoard2016.jpg";
        String workingDirectory = System.getProperty("user.dir");

        // when: plotting holds on image
        String newFile = Utils.plotHoldsOnImagePython(
            EXAMPLE_ROUTE_ID, wallImageFilePath,
            workingDirectory, workingDirectory,
            holds
        );

        // then: file is successfully created
        assertNotNull(newFile);
    }
}

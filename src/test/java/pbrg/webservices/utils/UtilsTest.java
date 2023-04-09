package pbrg.webservices.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Objects;
import org.json.JSONArray;
// import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class UtilsTest {

    /** The average bouldering grade worldwide is V5. */
    private static final int AVERAGE_GRADE = 5;

    /** The example route ID. */
    private static final int EXAMPLE_ROUTE_ID = 500;

    @Test
    void getContentType() {
        assert Objects.equals(Utils.getContentType("jpg"), "image/jpeg");

        // ensure non keys are mapped to null
        String[] nonKeys = {null, "not-in-map"};
        for (String nonKey : nonKeys) {
            assert Objects.equals(Utils.getContentType(nonKey), null);
        }
    }

    @Test
    void generateRouteMoonBoard() {
        // given: grade

        // when: generate route
        JSONArray result = Utils.generateRouteMoonboard(AVERAGE_GRADE);

        // then: result is not null
        assertNotNull(result);
        assert !result.isEmpty();
    }

    @Test
    void plotHoldsOnImage() {
        // given: holds, paths, route ID, working directory
        JSONArray holds = Utils.generateRouteMoonboard(AVERAGE_GRADE);
        assertNotNull(holds);
        String wallImageFilePath = "MoonBoard2016.jpg";
        String workingDirectory = System.getProperty("user.dir");

        // when: plotting holds on image
        String newFile = Utils.plotHoldsOnImagePython(
            EXAMPLE_ROUTE_ID, wallImageFilePath,
            workingDirectory, workingDirectory,
            holds
        );

        // then: ensure file is created
        assertNotNull(newFile);
    }
}

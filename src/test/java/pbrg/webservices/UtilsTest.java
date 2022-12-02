package pbrg.webservices;

import org.junit.jupiter.api.Test;
import pbrg.webservices.utils.Utils;

//@Disabled
public class UtilsTest {

    @Test
    void testFilePath() {
        String wallPath = Utils.WALL_IMAGE_PATH;
        System.out.printf("Wall image path: {%s}%n", wallPath);
    }
}

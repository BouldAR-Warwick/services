package pbrg.webservices.utils;

import org.junit.jupiter.api.Test;

//@Disabled
public class UtilsTest {

    @Test
    void testFilePath() {
        String wallPath = Utils.WALL_IMAGE_PATH;
        System.out.printf("Wall image path: {%s}%n", wallPath);
    }
}

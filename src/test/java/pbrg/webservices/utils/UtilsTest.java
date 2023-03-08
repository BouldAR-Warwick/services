package pbrg.webservices.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Objects;
import org.json.JSONArray;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class UtilsTest {

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
    void generateRouteMoonboard() {
        int grade = 5;

        JSONArray result = Utils.generateRouteMoonboard(grade);

        assert(!result.isEmpty());
    }

    @Test
    @Disabled
    void plotHoldsOnImageOpenCV() {
        int grade = 5;
        JSONArray holds = Utils.generateRouteMoonboard(grade);

        String wallImageFilePath = "moonboard2016.jpg";

        int name = 500;

        String newFile = Utils.plotHoldsOnImageOpenCV(name, wallImageFilePath, holds);

        assertNotNull(newFile);
    }

    @Test
    void plotHoldsOnImage() {
        int grade = 5;
        JSONArray holds = Utils.generateRouteMoonboard(grade);

        String wallImageFilePath = "moonboard2016.jpg";

        int name = 500;

        String newFile = Utils.plotHoldsOnImagePython(name, wallImageFilePath, "./", "./", holds);

        assertNotNull(newFile);
    }
}
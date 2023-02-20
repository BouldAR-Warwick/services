package pbrg.webservices.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Objects;
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
}
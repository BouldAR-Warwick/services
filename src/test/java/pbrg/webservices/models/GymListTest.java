package pbrg.webservices.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class GymListTest {
    @Test
    void testGetGyms() {
        String[] expectedGyms = {"Gym1", "Gym2", "Gym3"};
        GymList gymList = new GymList(expectedGyms);
        assertArrayEquals(expectedGyms, gymList.getGyms());
    }
}
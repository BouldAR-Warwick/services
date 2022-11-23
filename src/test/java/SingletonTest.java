import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import pbrg.webservices.Singleton;


@Disabled
public class SingletonTest {

    static {
        Singleton.getInstance();
    }

    @Test
    void testFilePath() {
        System.out.println(String.format("Wall image path: {0}", Singleton.wallImagePath));
    }
}

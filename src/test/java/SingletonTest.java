import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pbrg.webservices.Singleton;

@Disabled
public class SingletonTest {

    @Test
    void testFilePath() {
        String wallPath = Singleton.wallImagePath;
        System.out.printf("Wall image path: {%s}%n", wallPath);
    }
}

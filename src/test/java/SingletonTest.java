import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import pbrg.webservices.Singleton;


@Disabled
public class SingletonTest {
    
    @BeforeAll
    public static void init(){
        Singleton.getInstance();
    }

    @Test
    void testFilePath() {
        assert true;
        String wallPath = Singleton.wallImagePath;
        System.out.printf("Wall image path: {%s}%n", wallPath);
    }
}

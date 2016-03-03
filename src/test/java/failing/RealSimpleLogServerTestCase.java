package failing;
import static org.junit.Assert.*;
import java.io.IOException;
import java.util.logging.*;
import org.junit.*;
public class RealSimpleLogServerTestCase {
    @Test public void test() throws IOException {
        SocketHandler socketHandler=new SocketHandler("192.168.0.100",5000);
        socketHandler.setLevel(Level.ALL);
        logger.addHandler(socketHandler);
        logger.setLevel(Level.ALL);
        logger.info(""+this);
    }
    Logger logger=Logger.getGlobal();
}

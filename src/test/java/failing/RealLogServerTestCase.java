package failing;
import static org.junit.Assert.*;
import java.util.logging.*;
import org.junit.*;
import com.tayek.tablet.Main;
import com.tayek.tablet.io.*;
public class RealLogServerTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        // LogManager.getLogManager().reset();
        LoggingHandler.once=false;
        LoggingHandler.init();
        LoggingHandler.socketHandler=null; // static, was causing tests to fail!
        LoggingHandler.startSocketHandler(Main.testingHost,LogServer.defaultService);
        LoggingHandler.setLevel(Level.ALL);
        LoggingHandler.addSocketHandler(LoggingHandler.socketHandler);
    }
    @After public void tearDown() throws Exception {
        LoggingHandler.stopSocketHandler();
    }
    @Test public void test() { // just testing that we can log.
        IO.staticLogger.info("sample log message");
    }
}

package failing;
import static org.junit.Assert.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.Info.Histories;
import com.tayek.tablet.io.LoggingHandler;
import static com.tayek.tablet.io.IO.*;
public class OneTabletTestCase extends AbstractTabletTestCase {
    @Before public void setUp() throws Exception {
        super.setUp();
        tablets=createForTest(1,serviceOffset);
        printThreads=true;
    }
    @After public void tearDown() throws Exception {
        super.tearDown();
    }
    @Test public void testSetUpAndTearDown() {}
    @Test public void startupAndShutdown() {
        startListening();
        shutdown();
    }
    @Test(timeout=200) public void testSendOneMessage() throws InterruptedException {
        startListening();
        sendOneDummyMessageFromEachTabletAndWait(false);
        shutdown();
    }
}

package failing;
import static org.junit.Assert.*;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.Info;
import com.tayek.tablet.Group.Info.Histories;
import com.tayek.tablet.io.LoggingHandler;
import static com.tayek.tablet.io.IO.*;
public class TwoTabletsTestCase extends AbstractTabletTestCase {
    @Before public void setUp() throws Exception {
        super.setUp();
        tablets=createForTest(2,serviceOffset);
        startListening();
    }
    @After public void tearDown() throws Exception {
        shutdown();
        super.tearDown();
    }
    @Test(timeout=200) public void testDummy2() throws InterruptedException,UnknownHostException,ExecutionException {
        sendOneDummyMessageFromEachTabletAndWaitAndShutdown(false);
        for(Tablet tablet:tablets) {
            Histories history=tablet.group.info(tablet.tabletId()).history;
            assertEquals(new Integer(0),history.client.client.failures());
        }
    }
    @Test() public void testDummy2Brokem() throws InterruptedException,UnknownHostException,ExecutionException {
        LoggingHandler.setLevel(Level.SEVERE); // so timeout warnings do not print out.
        for(Tablet tablet:tablets)
            tablet.stopListening(); // so send will fail
        sendOneDummyMessageFromEachTablet();
        Thread.sleep(500);
        for(Tablet tablet:tablets) {
            Histories history=tablet.group.info(tablet.tabletId()).history;
            assertEquals(new Integer(2),history.client.client.failures());
        }
    }
    Level oldLevel;
}

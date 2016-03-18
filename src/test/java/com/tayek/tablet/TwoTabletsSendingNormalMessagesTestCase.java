package com.tayek.tablet;
import static org.junit.Assert.*;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.tablet.io.LoggingHandler;
import static com.tayek.tablet.io.IO.*;
import static com.tayek.utilities.Utility.*;
public class TwoTabletsSendingNormalMessagesTestCase extends AbstractTabletTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        super.setUp();
        tablets=createForTest(2,serviceOffset);
        startListening();
    }
    @After public void tearDown() throws Exception {
        shutdown();
        super.tearDown();
    }
    @Test() public void testOneNormalMessage() throws InterruptedException,UnknownHostException,ExecutionException {
        LoggingHandler.setLevel(Level.WARNING);
        for(Tablet tablet:tablets)
            tablet.click(1);
        Thread.sleep(100);
        for(Tablet tablet:tablets) {
            Histories history=tablet.group.info(tablet.tabletId()).history;
            assertEquals(new Integer(0),history.client.client.failures());
            assertEquals(new Integer(0),history.server.server.failures());
            assertEquals(new Integer(0),history.server.missing.failures());
        }
        for(Tablet tablet:tablets) 
            p("history for tablet: "+tablet+" "+tablet.group.info(tablet.tabletId()).history);
    }
}

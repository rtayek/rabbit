package com.tayek.sablet;
import static com.tayek.io.IO.*;
import static org.junit.Assert.*;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import org.junit.*;
import com.tayek.tablet.*;
public class TwoTabletsTestCase extends AbstractTabletTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        AbstractTabletTestCase.setUpBeforeClass();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {
        AbstractTabletTestCase.tearDownAfterClass();
    }
   @Before public void setUp() throws Exception {
        super.setUp();
    }
    @After public void tearDown() throws Exception {
        super.tearDown();
    }
    @Test(timeout=200) public void testDummy2() throws InterruptedException,UnknownHostException,ExecutionException {
        tablets=Tablet.createForTest(2,serviceOffset);
        startListening();
        sendOneDummyMessageFromEachTabletAndWaitAndShutdown(false);
        for(Tablet tablet:tablets) {
            Histories history=tablet.histories();
            assertEquals(new Integer(0),history.client.client.failures());
        }
    }
    @Test(timeout=500) public void testDummyWithEmptyMessage() throws InterruptedException,UnknownHostException,ExecutionException {
        tablets=Tablet.createForTest(2,serviceOffset);
        startListening();
        Tablet first=tablets.iterator().next();
        int n=1;
        for(int i=1;i<=n;i++) {
            first.broadcast(first.stuff.messages.empty(),first.stuff);
            Thread.sleep(10); // fails if no sleep!
            // not when waiting for send called!
        }
        Thread.sleep(100);
    }
    @Test() public void testDummy2Brokem() throws InterruptedException,UnknownHostException,ExecutionException {
        tablets=Tablet.createForTest(2,serviceOffset);
        //startListening(); // so send will fail
        sendOneDummyMessageFromEachTablet();
        Thread.sleep(500);
        shutdown();
        for(Tablet tablet:tablets) {
            Histories history=tablet.histories();
            assertEquals(new Integer(2),history.client.client.failures());
        }
    }
}

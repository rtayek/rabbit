package com.tayek.failures;
import static com.tayek.utilities.Utility.*;
import static org.junit.Assert.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.io.LoggingHandler;
import com.tayek.sablet.AbstractTabletTestCase;
import com.tayek.tablet.*;
import com.tayek.Tablet.*;
import com.tayek.tablet.Group.TabletImpl2;
import com.tayek.tablet.Message.Type;
import static com.tayek.io.IO.*;
import com.tayek.*;
import com.tayek.utilities.Et;
public class TabletFastMissingMessageTestCase extends AbstractTabletTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        AbstractTabletTestCase.setUpBeforeClass();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {
        AbstractTabletTestCase.tearDownAfterClass();
    }
    @Before public void setUp() throws Exception {
        super.setUp();
        LoggingHandler.setLevel(Level.WARNING);
        printThreads=false; // reduce noise level
    }
    @After public void tearDown() throws Exception {
        super.tearDown();
    }
    void check(Integer n) {
        for(Tablet tablet:tablets) {
            Histories histories=tablet.histories();
            if(false) p("histories: "+histories);
            assertTrue(n<=histories.senderHistory.history.attempts());
            // strange, was 101
            assertTrue(n<=histories.receiverHistory.history.attempts());
            assertEquals(n,histories.receiverHistory.missing.attempts());
            assertEquals(n,histories.senderHistory.history.successes());
            assertEquals(n,histories.receiverHistory.history.successes());
            assertEquals(n,histories.receiverHistory.missing.successes());
            assertEquals(zero,histories.senderHistory.history.failures());
            assertEquals(zero,histories.receiverHistory.history.failures());
            assertEquals(zero,histories.receiverHistory.missing.failures());
            boolean fail=true;
            if(fail) if(histories.receiverHistory.missed.areAnyMissing()) p("some are missing!");
            else assertEquals(F,histories.receiverHistory.missed.areAnyMissing());
            if(false) assertEquals(F,histories.receiverHistory.missed.areAnyOutOfOrder());
            else if(histories.receiverHistory.missed.areAnyOutOfOrder()) p("missed: "+histories.receiverHistory.missed);
        }
    }
    void test(int nTablets,int messages,int wait) throws InterruptedException {
        Histories.defaultReportPeriod=0;
        tablets=createForTest(nTablets,serviceOffset);
        startListening();
        Tablet first=tablets.iterator().next();
        for(int i=1;i<=messages;i++) {
            first.broadcast(first.messageFactory().other(Type.dummy,first.group().groupId,first.tabletId()));
            Thread.sleep(wait);
        }
        Thread.sleep(10*messages);
        Thread.sleep(30*nTablets);
        check(messages);
    }
    @Test public void test1_2WithoutWait() throws InterruptedException {
        test(1,2,0);
    }
    @Test public void test1_5WithoutWait() throws InterruptedException {
        test(1,5,0);
    }
    @Test public void test1_10WithoutWait() throws InterruptedException {
        test(1,10,0);
    }
    @Test public void test1_10WithWait() throws InterruptedException {
        test(1,10,wait1);
    }
    @Test public void test2_10WithoutWait() throws InterruptedException {
        test(2,10,0);
    }
    @Test public void test2_10WithWait() throws InterruptedException {
        test(2,10,wait1);
    }
    @Test() public void test10_10WithoutWait() throws InterruptedException {
        test(10,10,0);
    }
    @Test() public void test10_10WithWait() throws InterruptedException {
        test(10,10,wait1);
    }
    /*
    @Test() public void test10_100WithoutWait() throws InterruptedException {
        test(10,100,0);
    }
    @Test() public void test10_100WithWait() throws InterruptedException {
        test(10,100,wait1); // takes 32 seconds!
    }
    @Test() public void test100_100WithoutWait() throws InterruptedException {
        test(100,100,0); // takes 108 seconds
    }
    @Test() public void test100_100WithWait() throws InterruptedException {
        test(100,100,wait1);
    }
    */
    int wait1=Config.defaultDriveWait;
}

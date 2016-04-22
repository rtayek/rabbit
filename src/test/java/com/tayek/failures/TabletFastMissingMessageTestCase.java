package com.tayek.failures;
import static com.tayek.utilities.Utility.*;
import static org.junit.Assert.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.io.LoggingHandler;
import com.tayek.sablet.AbstractTabletTestCase;
import com.tayek.tablet.*;
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
    }
    @After public void tearDown() throws Exception {
        super.tearDown();
    }
    void check(Integer n) {
        for(TabletImpl2 tablet:tablets) {
            Histories histories=tablet.histories();
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
            assertEquals(F,histories.receiverHistory.missed.areAnyMissing());
            if(false) assertEquals(F,histories.receiverHistory.missed.areAnyOutOfOrder());
            else if(histories.receiverHistory.missed.areAnyOutOfOrder()) p("missed: "+histories.receiverHistory.missed);
        }
    }
    void test(int nTablets,int messages,int wait) throws InterruptedException {
        Histories.defaultReportPeriod=0;
        tablets=createForTest(nTablets,serviceOffset);
        startListening();
        TabletImpl2 first=tablets.iterator().next();
        for(int i=1;i<=messages;i++) {
            first.broadcast(first.messageFactory().other(Type.dummy,first.groupId(),first.tabletId()));
            Thread.sleep(wait);
        }
        Thread.sleep(10*messages);
        Thread.sleep(30*nTablets);
        check(messages);
        for(TabletImpl2 tablet:tablets)
            p(tablet.report(method()));
    }
    @Test public void test1WithoutWait() throws InterruptedException {
        test(1,10,0);
    }
    @Test public void test1WithWait() throws InterruptedException {
        test(1,10,wait1);
    }
    @Test public void test2WithoutWait() throws InterruptedException {
        test(2,10,0);
    }
    @Test public void test2WithWait() throws InterruptedException {
        test(2,10,wait1);
    }
    @Test() public void testMoreWithoutWait() throws InterruptedException {
        test(10,10,0);
    }
    @Test() public void testMoreWithWait() throws InterruptedException {
        test(10,10,wait1);
    }
    @Test() public void testEvenMoreWithoutWait() throws InterruptedException {
        test(10,100,0);
    }
    @Test() public void testEvenMoreWithWait() throws InterruptedException {
        test(10,100,wait1);
    }
    @Test() public void test100WithoutWait() throws InterruptedException {
        test(100,100,0);
    }
    @Test() public void test100WithWait() throws InterruptedException {
        test(100,100,wait1);
    }
    int wait1=Group.Config.defaultDriveWait;
}

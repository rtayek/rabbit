package com.tayek.failures;
import static com.tayek.utilities.Utility.*;
import static org.junit.Assert.*;
import org.junit.*;
import com.tayek.sablet.AbstractTabletTestCase;
import com.tayek.tablet.*;
import com.tayek.tablet.Messages.*;
import static com.tayek.io.IO.*;
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
    }
    @After public void tearDown() throws Exception {
        super.tearDown();
    }
    void check(Integer n) {
        for(Tablet tablet:tablets) {
            Histories histories=tablet.histories();
            assertEquals(n,histories.client.client.attempts());
            assertEquals(n,histories.server.server.attempts());
            assertEquals(n,histories.server.missing.attempts());
            assertEquals(n,histories.client.client.successes());
            assertEquals(n,histories.server.server.successes());
            assertEquals(n,histories.server.missing.successes());
            assertEquals(zero,histories.client.client.failures());
            assertEquals(zero,histories.server.server.failures());
            assertEquals(zero,histories.server.missing.failures());
        }
    }
    @Test public void test() throws InterruptedException {
        Histories.defaultReportPeriod=0;
        tablets=Tablet.createForTest(2,serviceOffset);
        startListening();
        Tablet first=tablets.iterator().next();
        int n=10;
        for(int i=1;i<=n;i++) {
            first.broadcast(first.stuff.messages.other(Type.dummy,first.groupId,first.tabletId()),first.stuff);
            Thread.sleep(10); // fails if no sleep!
            // not when waiting for send called!
        }
        Thread.sleep(100);
        check(n);
    }
    @Test() public void testSendManyMessageFromFirstTabletAndWait() throws InterruptedException {
        Histories.defaultReportPeriod=0;
        tablets=Tablet.createForTest(32,serviceOffset);
        startListening();
        Tablet first=tablets.iterator().next();
        int n=50;
        for(int i=1;i<=n;i++) {
            first.broadcast(first.stuff.messages.other(Type.dummy,first.groupId,first.tabletId()),first.stuff);
            Thread.sleep(50);
        }
        Thread.sleep(2_000);
        check(n);
    }
    @Test() public void test50ForSpeed() throws InterruptedException {
        Histories.defaultReportPeriod=0;
        tablets=Tablet.createForTest(50,serviceOffset);
        startListening();
        Tablet first=tablets.iterator().next();
        Tablet last=(Tablet)tablets.toArray()[tablets.size()-1];
        int n=100;
        Et et=new Et();
        for(int i=1;i<=n;i++) {
            first.broadcast(first.stuff.messages.other(Type.dummy,first.groupId,first.tabletId()),first.stuff);
            Thread.sleep(50);
        }
        p(n+" messages sent in: "+et);
        while(last.histories().server.server.attempts()<n)
            Thread.sleep(10);
        p(n+" messages received in: "+et);
        Thread.sleep(100);
        check(n);
    }
}

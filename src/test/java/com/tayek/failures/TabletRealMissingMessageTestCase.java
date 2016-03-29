package com.tayek.failures;
import static com.tayek.utilities.Utility.*;
import static org.junit.Assert.*;
import org.junit.*;
import com.tayek.sablet.AbstractTabletTestCase;
import com.tayek.tablet.*;
import com.tayek.tablet.Messages.*;
import static com.tayek.io.IO.*;
public class TabletRealMissingMessageTestCase extends AbstractTabletTestCase {
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
    @Test(timeout=900) public void testNoneMissing() throws InterruptedException {
        Histories.defaultReportPeriod=0;
        tablets=Tablet.createForTest(2,serviceOffset);
        startListening();
        Tablet first=tablets.iterator().next();
        Message message1=first.stuff.messages.other(Type.dummy,first.groupId,first.tabletId());
        Message message2=first.stuff.messages.other(Type.dummy,first.groupId,first.tabletId());
        Message message3=first.stuff.messages.other(Type.dummy,first.groupId,first.tabletId());
        first.broadcast(message1,first.stuff);
        Thread.sleep(100);
        first.broadcast(message2,first.stuff);
        Thread.sleep(100);
        first.broadcast(message3,first.stuff);
        while(first.histories().client.client.attempts()<2)
            Thread.sleep(10);
        for(Tablet tablet:tablets)
        p(tablet.stuff.report(tablet.tabletId()));
    }
    @Test(timeout=900) public void testOneMissing() throws InterruptedException {
        Histories.defaultReportPeriod=0;
        tablets=Tablet.createForTest(2,serviceOffset);
        startListening();
        Tablet first=tablets.iterator().next();
        Message message1=first.stuff.messages.other(Type.dummy,first.groupId,first.tabletId());
        Message message2=first.stuff.messages.other(Type.dummy,first.groupId,first.tabletId());
        Message message3=first.stuff.messages.other(Type.dummy,first.groupId,first.tabletId());
        first.broadcast(message1,first.stuff);
        Thread.sleep(100);
        //first.broadcast(message2,first.stuff);
        //Thread.sleep(100);
        first.broadcast(message3,first.stuff);
        while(first.histories().client.client.attempts()<2)
            Thread.sleep(10);
        for(Tablet tablet:tablets)
            p(tablet.stuff.report(tablet.tabletId()));
    }
    @Test(timeout=900) public void testOneOutOfOrder() throws InterruptedException {
        Histories.defaultReportPeriod=0;
        tablets=Tablet.createForTest(2,serviceOffset);
        startListening();
        Tablet first=tablets.iterator().next();
        Message message1=first.stuff.messages.other(Type.dummy,first.groupId,first.tabletId());
        Message message2=first.stuff.messages.other(Type.dummy,first.groupId,first.tabletId());
        Message message3=first.stuff.messages.other(Type.dummy,first.groupId,first.tabletId());
        first.broadcast(message1,first.stuff);
        Thread.sleep(100);
        first.broadcast(message3,first.stuff);
        Thread.sleep(100);
        first.broadcast(message2,first.stuff);
        while(first.histories().client.client.attempts()<2)
            Thread.sleep(10);
        for(Tablet tablet:tablets)
            p(tablet.stuff.report(tablet.tabletId()));
    }
}

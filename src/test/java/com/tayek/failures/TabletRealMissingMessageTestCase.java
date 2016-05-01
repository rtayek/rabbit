package com.tayek.failures;
import static com.tayek.utilities.Utility.*;
import static org.junit.Assert.*;
import org.junit.*;
import com.tayek.sablet.AbstractTabletTestCase;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.TabletImpl2;
import com.tayek.tablet.Message.Type;
import com.tayek.*;
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
            assertEquals(n,histories.senderHistory.history.attempts());
            assertEquals(n,histories.receiverHistory.history.attempts());
            assertEquals(n,histories.receiverHistory.missing.attempts());
            assertEquals(n,histories.senderHistory.history.successes());
            assertEquals(n,histories.receiverHistory.history.successes());
            assertEquals(n,histories.receiverHistory.missing.successes());
            assertEquals(zero,histories.senderHistory.history.failures());
            assertEquals(zero,histories.receiverHistory.history.failures());
            assertEquals(zero,histories.receiverHistory.missing.failures());
        }
    }
    @Test(timeout=900) public void testNoneMissing() throws InterruptedException {
        Histories.defaultReportPeriod=0;
        tablets=createForTest(2,serviceOffset);
        startListening();
        Tablet first=tablets.iterator().next();
        Message message1=first.messageFactory().other(Type.dummy,first.groupId(),first.tabletId());
        Message message2=first.messageFactory().other(Type.dummy,first.groupId(),first.tabletId());
        Message message3=first.messageFactory().other(Type.dummy,first.groupId(),first.tabletId());
        first.broadcast(message1);
        Thread.sleep(100);
        first.broadcast(message2);
        Thread.sleep(100);
        first.broadcast(message3);
        while(first.histories().senderHistory.history.attempts()<2)
            Thread.sleep(10);
        for(Tablet tablet:tablets)
        p(tablet.report(tablet.tabletId()));
    }
    @Test(timeout=900) public void testOneMissing() throws InterruptedException {
        Histories.defaultReportPeriod=0;
        tablets=createForTest(2,serviceOffset);
        startListening();
        Tablet first=tablets.iterator().next();
        Message message1=first.messageFactory().other(Type.dummy,first.groupId(),first.tabletId());
        Message message2=first.messageFactory().other(Type.dummy,first.groupId(),first.tabletId());
        Message message3=first.messageFactory().other(Type.dummy,first.groupId(),first.tabletId());
        first.broadcast(message1);
        Thread.sleep(100);
        //first.broadcast(message2,first.stuff);
        //Thread.sleep(100);
        first.broadcast(message3);
        while(first.histories().senderHistory.history.attempts()<2)
            Thread.sleep(10);
        for(Tablet tablet:tablets)
            p(tablet.report(tablet.tabletId()));
    }
    @Test(timeout=900) public void testOneOutOfOrder() throws InterruptedException {
        Histories.defaultReportPeriod=0;
        tablets=createForTest(2,serviceOffset);
        startListening();
        Tablet first=tablets.iterator().next();
        Message message1=first.messageFactory().other(Type.dummy,first.groupId(),first.tabletId());
        Message message2=first.messageFactory().other(Type.dummy,first.groupId(),first.tabletId());
        Message message3=first.messageFactory().other(Type.dummy,first.groupId(),first.tabletId());
        first.broadcast(message1);
        Thread.sleep(100);
        first.broadcast(message3);
        Thread.sleep(100);
        first.broadcast(message2);
        while(first.histories().senderHistory.history.attempts()<2)
            Thread.sleep(10);
        for(Tablet tablet:tablets)
            p(tablet.report(tablet.tabletId()));
    }
}

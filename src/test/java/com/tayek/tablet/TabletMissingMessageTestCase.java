package com.tayek.tablet;
import static org.junit.Assert.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.tablet.io.LoggingHandler;
import static com.tayek.tablet.io.IO.*;
public class TabletMissingMessageTestCase extends AbstractTabletTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        AbstractTabletTestCase.setUpBeforeClass();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {
        AbstractTabletTestCase.tearDownAfterClass();
    }
    @Before public void setUp() throws Exception {
        super.setUp();
        printThreads=true;
        // LoggingHandler.setLevel(Level.INFO);
    }
    @After public void tearDown() throws Exception {
        super.tearDown();
    }
    @Test public void test() throws InterruptedException {
        tablets=createForTest(2,serviceOffset);
        startListening();
        Tablet first=tablets.iterator().next();
        int n=10;
        for(int i=1;i<=n;i++) {
            first.broadcast(first.group.messages.dummy(first.group.groupId,first.tabletId()),0);
            Thread.sleep(50); // fails if no sleep!
        }
        Thread.sleep(100);
        for(Tablet tablet:tablets)
            ;//p("histories: "+tablet.group.histories(tablet));
        for(Tablet tablet:tablets) {
            assertEquals(Integer.valueOf(n),tablet.group.info(tablet.tabletId()).history.client.client.attempts());
            assertEquals(Integer.valueOf(n),tablet.group.info(tablet.tabletId()).history.server.server.attempts());
            assertEquals(Integer.valueOf(n),tablet.group.info(tablet.tabletId()).history.server.missing.attempts());
            assertEquals(Integer.valueOf(n),tablet.group.info(tablet.tabletId()).history.client.client.successes());
            assertEquals(Integer.valueOf(n),tablet.group.info(tablet.tabletId()).history.server.server.successes());
            assertEquals(Integer.valueOf(n),tablet.group.info(tablet.tabletId()).history.server.missing.successes());
            assertEquals(zero,tablet.group.info(tablet.tabletId()).history.client.client.failures());
            assertEquals(zero,tablet.group.info(tablet.tabletId()).history.server.server.failures());
            assertEquals(zero,tablet.group.info(tablet.tabletId()).history.server.missing.failures());
        }
    }
    @Test() public void testSendManyMessageFromFirstTabletAndWait() throws InterruptedException {
        tablets=createForTest(32,serviceOffset);
        startListening();
        Tablet first=tablets.iterator().next();
        first.group.reportPeriod=0; // bug?
        int n=10;
        for(int i=1;i<=n;i++) {
            first.broadcast(first.group.messages.dummy(first.group.groupId,first.tabletId()),0);
            Thread.sleep(50);
        }
        Thread.sleep(600);
        for(Tablet tablet:tablets) {
            assertEquals(Integer.valueOf(n),tablet.group.info(tablet.tabletId()).history.client.client.attempts());
            assertEquals(Integer.valueOf(n),tablet.group.info(tablet.tabletId()).history.server.server.attempts());
            assertEquals(Integer.valueOf(n),tablet.group.info(tablet.tabletId()).history.server.missing.attempts());
            assertEquals(Integer.valueOf(n),tablet.group.info(tablet.tabletId()).history.client.client.successes());
            assertEquals(Integer.valueOf(n),tablet.group.info(tablet.tabletId()).history.server.server.successes());
            assertEquals(Integer.valueOf(n),tablet.group.info(tablet.tabletId()).history.server.missing.successes());
            assertEquals(zero,tablet.group.info(tablet.tabletId()).history.client.client.failures());
            assertEquals(zero,tablet.group.info(tablet.tabletId()).history.server.server.failures());
            assertEquals(zero,tablet.group.info(tablet.tabletId()).history.server.missing.failures());
        }
    }

}

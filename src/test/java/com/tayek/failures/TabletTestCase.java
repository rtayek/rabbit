package com.tayek.failures;
import static com.tayek.io.IO.*;
import static org.junit.Assert.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import org.junit.*;
import com.tayek.*;
import com.tayek.sablet.AbstractTabletTestCase;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.TabletImpl2;
import com.tayek.tablet.Message.Type;
public class TabletTestCase extends AbstractTabletTestCase {
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
    void test(int n) throws InterruptedException {
        tablets=createForTest(n,serviceOffset);
        startListening();
        sendOneDummyMessageFromEachTabletAndWaitAndShutdown(false);
        for(Tablet tablet:tablets)
            if(tablet instanceof TabletImpl2) {
                TabletImpl2 t2=(TabletImpl2)tablet;
                checkHistory(t2,tablets.size(),false);
            } else {
                p("how do i check history?");
            }
        
    }
    @Test(timeout=200) public void testDummy2() throws InterruptedException,UnknownHostException,ExecutionException {
        test(2);
    }
    @Test(timeout=400) public void testDummy4() throws InterruptedException,UnknownHostException,ExecutionException {
        test(4);
    }
    @Test(timeout=600) public void testDummy6() throws InterruptedException,UnknownHostException,ExecutionException {
        test(6);
    }
    @Test(timeout=800) public void testDummy8() throws InterruptedException,UnknownHostException,ExecutionException {
        test(8);
    }
    @Test(timeout=1_200) public void testDummy10() throws InterruptedException,UnknownHostException,ExecutionException {
        test(10);
    }
    @Test(timeout=1_600) public void testDummy12() throws InterruptedException,UnknownHostException,ExecutionException {
        test(12);
    }
    @Test(timeout=2_500) public void testDummy14() throws InterruptedException,UnknownHostException,ExecutionException {
        test(14);
    }
    @Test(timeout=3_000) public void testDummy16() throws InterruptedException,UnknownHostException,ExecutionException {
        test(16);
    }
    @Test(timeout=10_000) public void testDummy32() throws InterruptedException,UnknownHostException,ExecutionException {
        test(32);
    }
    @Test(timeout=12_000) public void testDummy64() throws InterruptedException,UnknownHostException,ExecutionException {
        test(32);
    }
    @Test public void test2RealSimple() throws InterruptedException,UnknownHostException,ExecutionException {
        tablets=createForTest(2,serviceOffset);
        p("back in test");
        for(Tablet tablet:tablets) { // don't forget to do consistency check on histories for tabletimpl1!
            //p("group histories id for "+tablet.tabletId()+" is "+tablet.group().required(tablet.tabletId()).histories().serialNumber+" "+tablet.histories().serialNumber);
            p("histories id for "+tablet.tabletId()+" is "+tablet.histories().serialNumber+" "+tablet.histories().serialNumber);
        }
        startListening();
        for(Tablet tablet:tablets) {
            tablet.broadcast(tablet.messageFactory().other(Type.dummy,tablet.group().groupId,tablet.tabletId()));
        }
        Thread.sleep(200);
        Integer expected=2; // sending to self now
        Iterator<Tablet> i=tablets.iterator();
        Tablet t=i.next();
        Histories histories=t.histories(); // get history from tablet
        assertEquals(expected,histories.receiverHistory.history.successes());
        t=i.next();
        histories=t.histories();
        assertEquals(expected,histories.receiverHistory.history.successes());
        Tablet first=tablets.iterator().next();
        for(int buttoneId=1;buttoneId<=first.model().buttons;buttoneId++) {
            first.model().setState(buttoneId,true);
            Message message=first.messageFactory().normal(first.group().groupId,first.tabletId(),buttoneId,first.model().toCharacters());
            first.broadcast(message);
            Thread.sleep(100);
            for(Tablet tablet:tablets) {
                //assert missing attenpts should be equal to messages
                if(tablet.histories().receiverHistory.missing.failures()>0) fail("badness");
            }
        }
        for(int buttoneId=1;buttoneId<=first.model().buttons;buttoneId++)
            assertTrue(first.model().state(buttoneId));
        for(Tablet tablet:tablets)
            for(int buttoneId=1;buttoneId<=first.model().buttons;buttoneId++)
                assertTrue(tablet.model().state(buttoneId));
        first.model().reset();
        for(int buttoneId=1;buttoneId<=first.model().buttons;buttoneId++)
            assertFalse(first.model().state(buttoneId));
        Message message=first.messageFactory().other(Type.reset,first.group().groupId,first.tabletId());
        first.broadcast(message);
        Thread.sleep(100);
        for(Tablet tablet:tablets)
            for(int buttoneId=1;buttoneId<first.model().buttons;buttoneId++)
                assertFalse(tablet.model().state(buttoneId));
        for(Tablet tablet:tablets) // fails because each tablet needs a messages class!
            if(tablet.histories().receiverHistory.missing.failures()>0) fail("badness");
        shutdown();
        //printStats();
    }
    @Test(timeout=2_000) public void test2Real() throws InterruptedException,UnknownHostException,ExecutionException {
        tablets=createForTest(2,serviceOffset);
        startListening();
        for(Tablet tablet:tablets)
            tablet.broadcast(tablet.messageFactory().other(Type.dummy,tablet.group().groupId,tablet.tabletId()));
        if(true) {
            Thread.sleep(200);
            Histories histories;
            for(Tablet tablet:tablets)
                histories=tablet.histories();
            //printStats(""+getClass().getSimpleName());
        }
        waitForEachTabletToReceiveAtLeastOneMessageFromEachTablet(false);
        Tablet first=tablets.iterator().next();
        for(int buttoneId=1;buttoneId<=first.model().buttons;buttoneId++) {
            first.model().setState(buttoneId,true);
            Message message=first.messageFactory().normal(first.group().groupId,first.tabletId(),buttoneId,first.model().toCharacters());
            first.broadcast(message);
            Thread.sleep(100);
        }
        for(int buttoneId=1;buttoneId<=first.model().buttons;buttoneId++)
            assertTrue(first.model().state(buttoneId));
        for(Tablet tablet:tablets)
            for(int buttoneId=1;buttoneId<=tablet.model().buttons;buttoneId++)
                assertTrue(tablet.model().state(buttoneId));
        first.model().reset();
        for(int buttoneId=1;buttoneId<=first.model().buttons;buttoneId++)
            assertFalse(first.model().state(buttoneId));
        Message message=first.messageFactory().other(Type.reset,first.group().groupId,first.tabletId());
        first.broadcast(message);
        Thread.sleep(100);
        for(Tablet tablet:tablets)
            for(int buttoneId=1;buttoneId<tablet.model().buttons;buttoneId++)
                assertFalse(tablet.model().state(buttoneId));
        shutdown();
    }
}

package com.tayek.tablet;
import static com.tayek.tablet.io.IO.*;
import static org.junit.Assert.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.Info;
import com.tayek.tablet.Messages.Message;
import com.tayek.tablet.io.*;
import com.tayek.utilities.Et;
public class TabletTestCase extends AbstractTabletTestCase {
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
            checkHistory(tablet,tablets.size(),false);
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
    @Test(timeout=1_000) public void testDummy10() throws InterruptedException,UnknownHostException,ExecutionException {
        test(10);
    }
    @Test(timeout=1_200) public void testDummy12() throws InterruptedException,UnknownHostException,ExecutionException {
        test(12);
    }
    @Test(timeout=1_400) public void testDummy14() throws InterruptedException,UnknownHostException,ExecutionException {
        test(14);
    }
    @Test(timeout=1_600) public void testDummy16() throws InterruptedException,UnknownHostException,ExecutionException {
        test(16);
    }
    @Test(timeout=3_200) public void testDummy32() throws InterruptedException,UnknownHostException,ExecutionException {
        test(32);
    }
    @Test(timeout=6_400) public void testDummy64() throws InterruptedException,UnknownHostException,ExecutionException {
        test(32);
    }
    @Test public void test2RealSimple() throws InterruptedException,UnknownHostException,ExecutionException {
        tablets=createForTest(2,serviceOffset);
        startListening();
        LoggingHandler.setLevel(Level.WARNING);
        for(Tablet tablet:tablets) {
            tablet.broadcast(tablet.group.messages.dummy(tablet.group.groupId,tablet.tabletId()),0);
        }
        Thread.sleep(200);
        Integer expected=2; // sending to self now
        Iterator<Tablet> i=tablets.iterator();
        Tablet t=i.next();
        Histories history=t.group.info(t.tabletId()).history;
        assertEquals(expected,history.server.server.successes());
        t=i.next();
        history=t.group.info(t.tabletId()).history;
        assertEquals(expected,history.server.server.successes());
        Tablet first=tablets.iterator().next();
        for(int buttoneId=1;buttoneId<=first.model.buttons;buttoneId++) {
            first.model.setState(buttoneId,true);
            Message message=first.group.messages.normal(first.group.groupId,first.tabletId(),buttoneId,first.model);
            first.broadcast(message,0);
            Thread.sleep(200);
            for(Tablet tablet:tablets)
                if(tablet.group.info(tablet.tabletId()).history.server.missing.failures()>0) fail("badness");
        }
        for(int buttoneId=1;buttoneId<=first.model.buttons;buttoneId++)
            assertTrue(first.model.state(buttoneId));
        for(Tablet tablet:tablets)
            for(int buttoneId=1;buttoneId<=first.model.buttons;buttoneId++)
                assertTrue(tablet.model.state(buttoneId));
        first.model.reset();
        for(int buttoneId=1;buttoneId<=first.model.buttons;buttoneId++)
            assertFalse(first.model.state(buttoneId));
        Message message=first.group.messages.reset(first.group.groupId,first.tabletId(),first.model.buttons);
        first.broadcast(message,0);
        Thread.sleep(100);
        for(Tablet tablet:tablets)
            for(int buttoneId=1;buttoneId<first.model.buttons;buttoneId++)
                assertFalse(tablet.model.state(buttoneId));
        for(Tablet tablet:tablets) // fails because each tablet needs a messages class!
            if(tablet.group.info(tablet.tabletId()).history.server.missing.failures()>0) fail("badness");
        shutdown();
        //printStats();
    }
    @Test(timeout=2_000) public void test2Real() throws InterruptedException,UnknownHostException,ExecutionException {
        tablets=createForTest(2,serviceOffset);
        startListening();
        for(Tablet tablet:tablets)
            tablet.broadcast(tablet.group.messages.dummy(tablet.group.groupId,tablet.tabletId()),0);
        waitForEachTabletToReceiveAtLeastOneMessageFromEachTablet(false);
        Tablet first=tablets.iterator().next();
        for(int buttoneId=1;buttoneId<=first.model.buttons;buttoneId++) {
            first.model.setState(buttoneId,true);
            Message message=first.group.messages.normal(first.group.groupId,first.tabletId(),buttoneId,first.model);
            first.broadcast(message,0);
            Thread.sleep(100);
        }
        for(int buttoneId=1;buttoneId<=first.model.buttons;buttoneId++)
            assertTrue(first.model.state(buttoneId));
        for(Tablet tablet:tablets)
            for(int buttoneId=1;buttoneId<=tablet.model.buttons;buttoneId++)
                assertTrue(tablet.model.state(buttoneId));
        first.model.reset();
        for(int buttoneId=1;buttoneId<=first.model.buttons;buttoneId++)
            assertFalse(first.model.state(buttoneId));
        Message message=first.group.messages.reset(first.group.groupId,first.tabletId(),first.model.buttons);
        first.broadcast(message,0);
        Thread.sleep(100);
        for(Tablet tablet:tablets)
            for(int buttoneId=1;buttoneId<tablet.model.buttons;buttoneId++)
                assertFalse(tablet.model.state(buttoneId));
        shutdown();
    }
}

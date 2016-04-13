package com.tayek.speed;
import static com.tayek.io.IO.*;
import static com.tayek.speed.Server.factory;
import static com.tayek.utilities.Utility.connect;
import static org.junit.Assert.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.Histories;
import com.tayek.io.LoggingHandler;
import com.tayek.utilities.Et;
public class ServerTestCase extends AbstractServerTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        super.setUp();
    }
    @After public void tearDown() throws Exception {
        super.tearDown();
    }
    @Test public void testCreateAndStopServers1() throws InterruptedException {
        create(1);
        stopServers();
        Thread.sleep(100);
        assertTrue(Thread.activeCount()<=threads);
    }
    void createStartAndStop(int n) throws InterruptedException {
        create(n);
        startServers();
        Thread.sleep(100);
        stopServers();
        Thread.sleep(200);
        assertTrue(Thread.activeCount()<=threads);
    }
    @Test public void testCreateStartServersAndStopServers1() throws InterruptedException {
        createStartAndStop(1);
    }
    @Test public void testCreateStartServersAndStopServers2() throws InterruptedException {
        createStartAndStop(2);
    }
    @Test public void testCreateStartServersAndStopServers3() throws InterruptedException {
        createStartAndStop(3);
    }
    @Test public void testCreateStartServersAndStopServers10() throws InterruptedException {
        createStartAndStop(10);
    }
    // need a test to use discover and pass the addresses to the tablets
    void run(int n,Integer messages) throws InterruptedException {
        create(n);
        startServers();
        //Thread.sleep(100);
        addSenders(n);
        //Thread.sleep(200);
        p("broadcasting -------------------------------------");
        Et et=new Et();
        for(int i=0;i<messages;i++) {
            for(Server server:servers) {
                server.broadcast(server.create("foo"));
            }
            //Thread.sleep(10);
        }
        p((messages*servers.size()*servers.size())+" messages sent in: "+et+"="+(1000.*messages*messages*servers.size()/et.etms())+" messages/second");
        Thread.sleep(20);
        p((messages*servers.size()*servers.size())+" messages sent in: "+et+"="+(1000.*messages*messages*servers.size()/et.etms())+" messages/second");
        if(true) {
            p("reporting -----------------------------------------------------");
            for(Server server:servers) {
                p("---------\n"+server.report());
            }
        }
        //new Thread(new Joiner(threads)).start();
        //Thread.sleep(200);
        stopServers();
        for(Server server:servers) {
            //p(""+server);
            Histories histories=server.histories();
            //p("messages: "+messages+", sent: "+histories.senderHistory.history.attempts()+", received: "+histories.receiverHistory.history.attempts());
            //p(""+histories.anyAttempts());
            //p(""+histories.anyFailures());
            //p(""+histories);
            if(histories.senderHistory.history.attempts()<messages) {
                p(""+server);
                p("not all were sent!");
            }
            if(histories.receiverHistory.history.attempts()<messages) {
                p(""+server);
                p("not all were received!");
            }
            assertTrue(histories.anyAttempts());
            assertFalse(histories.anyFailures());
            assertEquals(Integer.valueOf(messages),histories.senderHistory.history.attempts());
            assertEquals(Integer.valueOf(messages),histories.receiverHistory.history.attempts());
        }
    }
    @Test public void test1_1() throws InterruptedException {
        run(1,1);
        assertTrue(Thread.activeCount()<=threads);
    }
    @Test public void test2_1() throws InterruptedException {
        run(2,1);
        assertTrue(Thread.activeCount()<=threads);
    }
    @Test public void test10_1() throws InterruptedException {
        run(10,1);
        assertTrue(Thread.activeCount()<=threads);
    }
    @Test public void test1_10() throws InterruptedException {
        run(1,10);
        assertTrue(Thread.activeCount()<=threads);
    }
    @Test public void test2_10() throws InterruptedException {
        run(2,10);
        assertTrue(Thread.activeCount()<=threads);
    }
    @Test public void test10_10() throws InterruptedException {
        run(10,10);
        assertTrue(Thread.activeCount()<=threads);
    }
}

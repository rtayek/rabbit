package com.tayek.speed;
import static com.tayek.io.IO.p;
import static org.junit.Assert.*;
import java.net.*;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.*;
import com.tayek.io.LoggingHandler;
import com.tayek.tablet.Message.Type;
import com.tayek.utilities.*;
public class OneKnowsTestCase extends AbstractServerTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        super.setUp();
        LoggingHandler.setLevel(Level.WARNING);
    }
    @After public void tearDown() throws Exception {}
    void addSenders2() { // just add senders to the first tablet
        p("adding sender(s): "+servers+" -------------------------------------");
        Iterator<Server> i=servers.iterator();
        Server first=i.next(),next;
        while(i.hasNext()) {
            next=i.next();
            //p(first.id()+" is adding sender for: "+next.id());
            Required required=new Required(next.id(),next.host(),next.service());
            first.createAndAddSender(next.id(),required);
        }
    }
    void run2(int n,Integer messages) throws InterruptedException {
        create(n);
        startServers();
        Thread.sleep(1_000);
        addSenders2();
        Thread.sleep(1_000);
        p("servers: "+servers);
        // maybe do the brofcasting one tablet at a time?
        p("broadcasting -------------------------------------");
        for(int i=0;i<messages;i++) {
            for(Server server:servers) {
                server.broadcast(server.messageFactory().other(Type.dummy,"1","T1"));
            }
        }
        p("sleeping -------------------------------------");
        Thread.sleep(1_000);
        Server first=servers.iterator().next();
        for(Server server:servers) {
            p("for server: "+server);
            for(Entry<String,Pair<Sender,Reader>> entry:server.idToPair().entrySet()) {
                Pair<Sender,Reader> pair=entry.getValue();
                p("pair: "+pair);
                if(server.equals(first)) {
                    // will have more sends and no more receives
                    if(entry.getKey().equals(server.id())) {
                        assertEquals(messages,pair.first.histories.senderHistory.history.successes());
                        assertEquals(messages,pair.second.histories.receiverHistory.history.successes());
                    } else {
                        p("pair.first: "+pair.first);
                        assertEquals(messages,pair.first.histories.senderHistory.history.successes());
                        if(pair.second!=null) {
                            p("pair.second in not null: "+pair.second);
                            if(true) assertEquals(zero,pair.second.histories.receiverHistory.history.successes());
                        } else assertNull(pair.second); // others have not sent us anything yet
                    }
                } else {
                    // will have no more sends and more receives
                    if(entry.getKey().equals(first.id())) {
                        // looks like some of the latter tablets get the broadcast from
                        // the first soon enough to have to have a receiver
                        // and the broadcast is done late enough so tha h emakes a sender!
                        // fix this with sync
                        // hmmm, broadcast and addConnection are sync'ed on this
                        // so maybe we need to sync on something else?
                        // seems strange, the caller of addConnection should be on a different thread?
                        if(pair.first!=null) {
                            p("sender successes: "+pair.first.histories.senderHistory.history.successes());
                            if(true) assertEquals(zero,pair.first.histories.senderHistory.history.successes());
                        }
                        assertEquals(messages,pair.second.histories.receiverHistory.history.successes());
                    } else if(entry.getKey().equals(server.id())) {
                        assertEquals(messages,pair.first.histories.senderHistory.history.successes());
                        if(pair.second!=null) assertEquals(messages,pair.second.histories.receiverHistory.history.successes());
                    } else {
                        assertEquals(messages,pair.first.histories.senderHistory.history.successes());// others have not sent us anything yet
                        assertEquals(zero,pair.second.histories.receiverHistory.history.successes());
                    }
                }
            }
        }
        p("second broadcast -------------------------------------");
        for(int i=0;i<messages;i++) {
            for(Server server:servers) {
                server.broadcast(server.messageFactory().other(Type.dummy,"1","T1"));
            }
        }
        p("sleeping -------------------------------------");
        Thread.sleep(5_000);
        for(Server server:servers) {
            p("report for server: "+server+", "+server.report());
            for(Entry<String,Pair<Sender,Reader>> entry:server.idToPair().entrySet()) {
                Pair<Sender,Reader> pair=entry.getValue();
                p("pair: "+pair);
                if(server.equals(first)) {
                    // will have more sends and no more receives
                    if(entry.getKey().equals(server.id())) {
                        assertEquals(Integer.valueOf(2*messages),pair.first.histories.senderHistory.history.successes());
                        assertEquals(Integer.valueOf(2*messages),pair.second.histories.receiverHistory.history.successes());
                    } else {
                        p("pair.first: "+pair.first);
                        assertEquals(Integer.valueOf(2*messages),pair.first.histories.senderHistory.history.successes());
                        if(pair.second!=null) {
                            if(entry.getKey().equals(first.id())) assertEquals(Integer.valueOf(2*messages),pair.second.histories.receiverHistory.history.successes());
                            else {
                                p("first, not server, not first, successes: "+pair.second.histories.receiverHistory.history.successes());
                                if(true) assertEquals(Integer.valueOf(messages),pair.second.histories.receiverHistory.history.successes());
                            }
                        } else p("pair.second is null");
                    }
                } else {
                    // will have no more sends and more receives
                    if(entry.getKey().equals(server.id())) assertEquals(Integer.valueOf(2*messages),pair.first.histories.senderHistory.history.successes());
                    else {
                        if(entry.getKey().equals(first.id())) {
                            // receive is adding a sender
                            if(true) assertEquals(Integer.valueOf(messages),pair.first.histories.senderHistory.history.successes());
                        } else assertEquals(messages,pair.first.histories.senderHistory.history.successes());
                    }
                    assertEquals(Integer.valueOf(2*messages),pair.second.histories.receiverHistory.history.successes());
                }
            }
        }
        p("stopping -------------------------------------");
        stopServers();
        Thread.sleep(1_000);
        for(Server server:servers)
            p("server: "+server);
    }
    @Test public void test2_1() throws Exception {
        run2(2,1);
    }
    @Test public void test3_1() throws Exception {
        run2(3,1);
    }
    @Test public void test4_1() throws Exception {
        run2(4,1);
    }
    @Test public void test5_1() throws Exception {
        run2(5,1);
    }
    @Test public void test6_1() throws Exception {
        run2(6,1);
    }
    @Test public void test10_1() throws Exception {
        run2(10,1);
    }
    static Integer zero=Integer.valueOf(0);
}

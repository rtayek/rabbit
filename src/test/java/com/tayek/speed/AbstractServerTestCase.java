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
import com.tayek.*;
import com.tayek.io.LoggingHandler;
import com.tayek.tablet.Message.Type;
import com.tayek.utilities.Et;
public abstract class AbstractServerTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.WARNING);
        service+=1_000;
    }
    @After public void tearDown() throws Exception {}
    void createStartAndStop(int n) throws InterruptedException {
        create(n);
        startServers();
        Thread.sleep(100);
        stopServers();
        Thread.sleep(200);
        assertTrue(Thread.activeCount()<=threads);
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
                server.broadcast(server.messageFactory().other(Type.dummy,"1","T1"));
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
        Thread.sleep(1_000);
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
        stopServers();
    }
    void create(int n) {
        for(Integer i=1;i<=n;i++)
            if(i==1) servers.add(factory.create(new Required(aTabletId(i),testingHost,service+i)));
            else servers.add(factory.create(new Required(aTabletId(i),defaultHost,service+i)));
    }
    void stopServers() throws InterruptedException {
        for(Server server:servers)
            server.stopServer();
    }
    void startServers() {
        for(Server server:servers)
            server.startServer();
    }
    void addSenders(int n) {
        p("adding sender(s) -------------------------------------");
        if(true) {
            for(Server server:servers)
                for(Server server2:servers)
                    if(server!=server2) {
                        SocketAddress socketAddress=new InetSocketAddress(server2.host(),server2.service());
                        Required required=new Required(server2.id(),server2.host(),server2.service());
                        server.createAndAddSender(server2.id(),required);
                    }
        } else {
            Iterator<Server> i=servers.iterator();
            Server first=i.next(),next;
            for(int j=1;j<=servers.size();j++)
                if(n>j) {
                    next=i.next();
                    p(first.id()+" is adding sender for: "+next.id());
                    Required required=new Required(next.id(),next.host(),next.service());

                    first.createAndAddSender(next.id(),required);
                }
        }
    }
    int threads=Thread.activeCount();
    Set<Server> servers=new LinkedHashSet<>();
    static int service=1_000;
}

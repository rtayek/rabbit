package com.tayek.speed;
import static com.tayek.io.IO.*;
import static com.tayek.speed.Server.factory;
import static org.junit.Assert.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.Description;
import com.tayek.*;
import com.tayek.io.LoggingHandler;
import com.tayek.tablet.Message;
import com.tayek.tablet.Message.Type;
import com.tayek.utilities.Et;
public abstract class AbstractServerTestCase {
    @Rule public TestRule watcher=new MyTestWatcher();
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.WARNING);
        service+=1_000;
    }
    @After public void tearDown() throws Exception {}
    void createStartAndStop(int n) throws InterruptedException {
        createTestTablets(n);
        startServers();
        Thread.sleep(100);
        stopServers();
        Thread.sleep(200);
        printThreads();
        //assertTrue(Thread.activeCount()<=threads);
    }
    void checkThreads(boolean fail) {
        if(Thread.activeCount()<=threads) {
            printThreads();
            if(fail) fail("extra threads!");
            else {
                p("extra threads:");
                printThreads();
            }
        }
    }
    // need a test to use discover and pass the addresses to the tablets
    // no, we have the group addresses, we should not need to do this kinf of thing anymore
    void run(int n,Integer messages) throws InterruptedException {
        createTestTablets(n);
        for(Server server:servers)
            p("server: "+server);
        startServers();
        Thread.sleep(100);
        addSenders(n);
        //Thread.sleep(200);
        p("broadcasting -------------------------------------");
        Et et=new Et();
        for(int i=0;i<messages;i++) {
            for(Server server:servers) {
                Message message=server.messageFactory().other(Type.dummy,"1",server.id());
                p(i+": sending: "+message);
                server.broadcast(message);
            }
            //Thread.sleep(10);
        }
        p((messages*servers.size()*servers.size())+" messages sent in: "+et+"="+(1000.*messages*messages*servers.size()/et.etms())+" messages/second");
        Thread.sleep(500);
        p((messages*servers.size()*servers.size())+" messages sent in: "+et+"="+(1000.*messages*messages*servers.size()/et.etms())+" messages/second");
        l.warning("just to see et");
        for(Server server:servers) {
            p(server+" "+server.report()+"\nend of report.");
        }
        for(Server server:servers) {
            Histories histories=server.histories();
            p("pairs: "+server.idToPair().entrySet());
            p(histories.toString(server.toString()));
            if(histories.senderHistory.history.attempts()<messages) {
                p(""+server);
                p("not all were sent!");
            }
            if(histories.receiverHistory.history.attempts()<messages) {
                p(""+server);
                p("not all were received!");
            }
        }
        for(Server server:servers) {
            Histories histories=server.histories();
            assertTrue(histories.anyAttempts());
            assertFalse(histories.anyFailures());
            Integer expectedSent=messages;
            Integer expectedReceived=messages;
            assertEquals(expectedSent,histories.senderHistory.history.attempts());
            assertEquals(expectedReceived,histories.receiverHistory.history.attempts());
        }
        if(false) {
            p("stopping servers");
            stopServers();
        }
    }
    void createTestTablets(int n) {
        for(Integer i=1;i<=n;i++)
            servers.add(factory.create(new Required(testingHost,service+i)));
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
                        p("adding sender to: "+required);
                        server.createAndAddWriter(server2.id(),required);
                    }
        } else {
            Iterator<Server> i=servers.iterator();
            Server first=i.next(),next;
            for(int j=1;j<=servers.size();j++)
                if(n>j) {
                    next=i.next();
                    p(first.id()+" is adding sender for: "+next.id());
                    Required required=new Required(next.id(),next.host(),next.service());
                    first.createAndAddWriter(next.id(),required);
                }
        }
    }
    int threads=Thread.activeCount();
    Set<Server> servers=new LinkedHashSet<>();
    static int service=1_000;
}

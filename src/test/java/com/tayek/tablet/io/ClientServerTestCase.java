package com.tayek.tablet.io;
import static org.junit.Assert.*;
import java.io.IOException;
import java.net.*;
import org.junit.*;
import org.junit.rules.TestRule;
import com.tayek.*;
import com.tayek.Sender.Client;
import com.tayek.io.LoggingHandler;
import com.tayek.tablet.*;
import com.tayek.Tablet.*;
import com.tayek.tablet.Message.*;
import com.tayek.utilities.Single;
public class ClientServerTestCase {
    @Rule public TestRule watcher=new MyTestWatcher();

    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LoggingHandler.init();
    }
    @After public void tearDown() throws Exception {}
    @Test(timeout=500) public void test() throws IOException,InterruptedException {
        socketAddress=new InetSocketAddress("localhost",++service);
        Required required=new Required("T0","localhost",service);
        factory=Message.instance.create(required.host,required.service,new Single<Integer>(0));
        Group group=new Group("1");
        Config config=new Config();
        server=new Server(null,socketAddress,null,config,histories);
        server.startServer();
        client=new Client(socketAddress,config,histories);
        Integer n=1;
        for(int i=1;i<=n;i++) {
            Message message=factory.other(Message.Type.dummy,"1","1");
            client.send(message);
        }
        while(histories.senderHistory.history.attempts()<n)
            Thread.yield();
        while(histories.receiverHistory.history.attempts()<n)
            Thread.yield();
        Thread.sleep(200);
        assertEquals(n,histories.senderHistory.history.successes());
        assertEquals(n,histories.receiverHistory.history.successes());
        server.stopServer();
    }
    @Test(timeout=500) public void testWithReply() throws IOException {
        Required required=new Required("T0","localhost",++service);
        socketAddress=new InetSocketAddress(required.host,required.service);
        factory=Message.instance.create(required.host,required.service,new Single<Integer>(0));
        Group group=new Group("1");
        Config config=new Config();
        config.replying=true;
        server=new Server(null,socketAddress,null,config,histories);
        server.startServer();
        client=new Client(socketAddress,config,histories);
        Integer n=10;
        for(int i=1;i<=n;i++) {
            Message message=factory.other(Message.Type.dummy,"1","1");
            client.send(message);
        }
        while(histories.senderHistory.history.attempts()<n)
            Thread.yield();
        while(histories.receiverHistory.history.attempts()<n)
            Thread.yield();
        assertEquals(n,histories.senderHistory.history.successes());
        assertEquals(n,histories.senderHistory.replies.successes());
        assertEquals(n,histories.receiverHistory.history.successes());
        assertEquals(n,histories.receiverHistory.replies.successes());
        server.stopServer();
    }
    @Test(timeout=200) public void testMissing() throws IOException,InterruptedException {
        Required required=new Required("T0","localhost",++service);
        socketAddress=new InetSocketAddress(required.host,required.service);
        factory=Message.instance.create(required.host,required.service,new Single<Integer>(0));
        Group stuff=new Group("1");
        Config config=new Config();
        server=new Server(null,socketAddress,null,config,histories);
        server.startServer();
        client=new Client(socketAddress,config,histories);
        Integer n=10;
        for(int i=1;i<=n;i++) {
            Message message=factory.other(Message.Type.dummy,"1","1");
            if(i!=5) client.send(message);
        }
        while(histories.senderHistory.history.attempts()<n-1)
            Thread.yield();
        while(histories.receiverHistory.history.attempts()<n-1)
            Thread.yield();
        assertEquals(Integer.valueOf(n-1),histories.senderHistory.history.successes());
        assertEquals(Integer.valueOf(n-1),histories.receiverHistory.history.successes());
        server.stopServer();
    }
    SocketAddress socketAddress;
    Client client;
    Server server;
    Histories histories=new Histories();
    Message.Factory factory;
    static int service=55555;
}

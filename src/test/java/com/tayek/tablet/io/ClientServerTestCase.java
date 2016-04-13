package com.tayek.tablet.io;
import static org.junit.Assert.*;
import java.io.IOException;
import java.net.*;
import org.junit.*;
import com.tayek.*;
import com.tayek.io.LoggingHandler;
import com.tayek.tablet.*;
import com.tayek.tablet.Message.*;
import com.tayek.tablet.Main.Stuff;
import com.tayek.tablet.io.Sender.Client;
import com.tayek.utilities.Single;
public class ClientServerTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LoggingHandler.init();
    }
    @After public void tearDown() throws Exception {}
    @Test(timeout=500) public void test() throws IOException,InterruptedException {
        socketAddress=new InetSocketAddress("localhost",++service);
        Stuff stuff=new Stuff();
        server=new Server(null,socketAddress,null,stuff,histories);
        server.startServer();
        client=new Client(socketAddress,stuff,histories);
        Integer n=1;
        for(int i=1;i<=n;i++) {
            Message message=messages.other(Type.dummy,"1","1");
            client.send(message,stuff);
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
        socketAddress=new InetSocketAddress("localhost",++service);
        Stuff stuff=new Stuff();
        stuff.replying=true;
        server=new Server(null,socketAddress,null,stuff,histories);
        server.startServer();
        client=new Client(socketAddress,stuff,histories);
        Integer n=10;
        for(int i=1;i<=n;i++) {
            Message message=messages.other(Type.dummy,"1","1");
            client.send(message,stuff);
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
        socketAddress=new InetSocketAddress("localhost",++service);
        Stuff stuff=new Stuff();
        server=new Server(null,socketAddress,null,stuff,histories);
        server.startServer();
        client=new Client(socketAddress,stuff,histories);
        Integer n=10;
        for(int i=1;i<=n;i++) {
            Message message=messages.other(Type.dummy,"1","1");
            if(i!=5) client.send(message,stuff);
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
    Factory messages=Message.instance.create(new Single<Integer>(0));
    static int service=55555;
}

package com.tayek.tablet.io;
import static org.junit.Assert.*;
import java.io.IOException;
import java.net.*;
import org.junit.*;
import com.tayek.tablet.*;
import com.tayek.tablet.Main.Stuff;
import com.tayek.tablet.Messages.*;
import com.tayek.tablet.io.Sender.Client;
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
        while(histories.client.client.attempts()<n)
            Thread.yield();
        while(histories.server.server.attempts()<n)
            Thread.yield();
        Thread.sleep(200);
        assertEquals(n,histories.client.client.successes());
        assertEquals(n,histories.server.server.successes());
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
        while(histories.client.client.attempts()<n)
            Thread.yield();
        while(histories.server.server.attempts()<n)
            Thread.yield();
        assertEquals(n,histories.client.client.successes());
        assertEquals(n,histories.client.replies.successes());
        assertEquals(n,histories.server.server.successes());
        assertEquals(n,histories.server.replies.successes());
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
        while(histories.client.client.attempts()<n-1)
            Thread.yield();
        while(histories.server.server.attempts()<n-1)
            Thread.yield();
        assertEquals(Integer.valueOf(n-1),histories.client.client.successes());
        assertEquals(Integer.valueOf(n-1),histories.server.server.successes());
        server.stopServer();
    }
    SocketAddress socketAddress;
    Client client;
    Server server;
    Histories histories=new Histories();
    Messages messages=new Messages();
    static int service=55555;
}

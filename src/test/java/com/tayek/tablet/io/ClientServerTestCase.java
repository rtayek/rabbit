package com.tayek.tablet.io;
import static org.junit.Assert.*;
import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.tablet.*;
import com.tayek.tablet.Messages.Message;
import com.tayek.tablet.io.*;
import com.tayek.tablet.io.Sender.Client;
import static com.tayek.tablet.io.IO.*;
public class ClientServerTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.WARNING);
    }
    @After public void tearDown() throws Exception {}
    @Test(timeout=500) public void test() throws IOException, InterruptedException {
        socketAddress=new InetSocketAddress("localhost",++service);
        server=new Server(null,socketAddress,null,false,histories.server);
        server.startServer();
        client=new Client(socketAddress,false,100);
        Integer n=10;
        for(int i=1;i<=n;i++) {
            Message message=messages.dummy(1,1);
            client.send(message,histories.client);
        }
        while(histories.client.client.attempts()<n)
            Thread.yield();
        while(histories.server.server.attempts()<n)
            Thread.yield();
        while(histories.server.missing.attempts()<n)
            Thread.yield();
        assertEquals(Integer.valueOf(0),histories.server.missing.failures());
        Thread.sleep(200);
        assertEquals(n,histories.client.client.successes());
        assertEquals(n,histories.server.server.successes());
        assertEquals(n,histories.server.missing.successes());
        server.stopServer();
    }
    @Test(timeout=500) public void testWithReply() throws IOException {
        socketAddress=new InetSocketAddress("localhost",++service);
        server=new Server(null,socketAddress,null,true,histories.server);
        server.startServer();
        client=new Client(socketAddress,true,100);
        Integer n=10;
        for(int i=1;i<=n;i++) {
            Message message=messages.dummy(1,1);
            client.send(message,histories.client);
        }
        while(histories.client.client.attempts()<n)
            Thread.yield();
        while(histories.server.server.attempts()<n)
            Thread.yield();
        while(histories.server.missing.attempts()<n)
            Thread.yield();
        assertEquals(n,histories.client.client.successes());
        assertEquals(n,histories.client.replies.successes());
        assertEquals(n,histories.server.server.successes());
        assertEquals(n,histories.server.replies.successes());
        assertEquals(n,histories.server.missing.successes());
        server.stopServer();
    }
    @Test(timeout=200) public void testMissing() throws IOException, InterruptedException {
        socketAddress=new InetSocketAddress("localhost",++service);
        server=new Server(null,socketAddress,null,false,histories.server);
        server.startServer();
        client=new Client(socketAddress,false,100);
        Integer n=10;
        for(int i=1;i<=n;i++) {
            Message message=messages.dummy(1,1);
            if(i!=5)
            client.send(message,histories.client);
        }
        while(histories.client.client.attempts()<n-1)
            Thread.yield();
        while(histories.server.server.attempts()<n-1)
            Thread.yield();
        while(histories.server.missing.attempts()<n-1)
            Thread.yield();
        assertEquals(Integer.valueOf(n-1),histories.client.client.successes());
        assertEquals(Integer.valueOf(n-1),histories.server.server.successes());
        assertEquals(Integer.valueOf(n-1),histories.server.missing.attempts());
        assertEquals(Integer.valueOf(n-2),histories.server.missing.successes());
        assertEquals(Integer.valueOf(1),histories.server.missing.failures());
        server.stopServer();
    }

    SocketAddress socketAddress;
    Client client;
    Server server;
    Histories histories=new Histories();
    Messages messages=new Messages();
    static int service=12345;
}

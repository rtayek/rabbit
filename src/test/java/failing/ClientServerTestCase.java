package failing;
import static org.junit.Assert.*;
import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.tablet.Group.Info.Histories;
import com.tayek.tablet.Message;
import com.tayek.tablet.io.*;
import static com.tayek.tablet.io.IO.*;
public class ClientServerTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.WARNING);
    }
    @After public void tearDown() throws Exception {}
    @Test public void test() throws IOException {
        socketAddress=new InetSocketAddress("localhost",12345);
        server=new Server(null,socketAddress,null,false,both);
        server.startServer();
        client=new Client(socketAddress,false,100);
        Message message=Message.dummy(1,1);
        Integer n=10;
        for(int i=0;i<n;i++)
            client.send(message,both.client);
        while(both.client.client.attempts()<n)
            Thread.yield();
        assertEquals(n,both.client.client.successes());
        server.stopServer();
        //p("history: "+both);
    }
    @Test public void testWithReply() throws IOException {
        socketAddress=new InetSocketAddress("localhost",12346);
        server=new Server(null,socketAddress,null,true,both);
        server.startServer();
        client=new Client(socketAddress,true,100);
        Message message=Message.dummy(1,1);
        Integer n=10;
        for(int i=0;i<n;i++)
            client.send(message,both.client);
        while(both.client.client.attempts()<n)
            Thread.yield();
        server.stopServer();
        p("history: "+both);
        assertEquals(n,both.client.client.successes());
        assertEquals(n,both.client.replies.successes());
        assertEquals(n,both.server.server.successes());
        assertEquals(n,both.server.replies.successes());
    }
    SocketAddress socketAddress;
    Client client;
    Server server;
    Histories both=new Histories();
}

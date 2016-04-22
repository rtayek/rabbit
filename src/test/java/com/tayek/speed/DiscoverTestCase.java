package com.tayek.speed;
import static com.tayek.io.IO.*;
import static org.junit.Assert.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.Required;
import com.tayek.io.LoggingHandler;
import com.tayek.utilities.Pair;
public class DiscoverTestCase extends AbstractServerTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.SEVERE);
    }
    @After public void tearDown() throws Exception {}
    @Test public void testDiscoverRealTablets() throws InterruptedException {
        Level level=l.getLevel();
        //LoggingHandler.setLevel(Level.SEVERE);
        Set<Pair<Integer,SocketAddress>> discovered=discoverRealTablets(6);
        LoggingHandler.setLevel(level);
        p("discovered: "+discovered);
        assertEquals(6,discovered.size());
        // ok, let's try to send them a message
        // we can not since the are not doing tcp yet :(
    }
    @Test public void testDiscoverTestTablets() throws InterruptedException {
        int n=6;
        create(n);
        startServers();
        Thread.sleep(2_000);
        Level level=l.getLevel();
        Set<Pair<Integer,SocketAddress>> discovered=discoverTestTablets(n,service);
        LoggingHandler.setLevel(level);
        p("discovered: "+discovered);
        assertEquals(n,discovered.size());
        for(Server server:servers) {
            for(Pair<Integer,SocketAddress> pair:discovered) {
                String id=aTabletId(pair.first);
                InetSocketAddress inetSocketAddress=(InetSocketAddress)pair.second;
                Required required=new Required(id,inetSocketAddress.getHostName(),inetSocketAddress.getPort());
                server.createAndAddSender(id,required);
            }
        }
        Thread.sleep(1_000);
        for(Server server:servers)
            p("server: "+server);
        stopServers();
        Thread.sleep(100);
        assertTrue(Thread.activeCount()<=threads);
    }
}

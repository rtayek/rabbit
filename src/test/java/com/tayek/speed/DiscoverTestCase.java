package com.tayek.speed;
import static com.tayek.io.IO.*;
import static org.junit.Assert.*;
import java.net.SocketAddress;
import java.util.Set;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.io.LoggingHandler;
public class DiscoverTestCase extends AbstractServerTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void testDiscoverRealTablets() throws InterruptedException {
        Level level=l.getLevel();
        //LoggingHandler.setLevel(Level.SEVERE);
        Set<SocketAddress> discovered=discoverReal(6);
        LoggingHandler.setLevel(level);
        p("discovered: "+discovered);
        assertEquals(6,discovered.size());
    }
    @Test public void testDiscoverTestTablets() throws InterruptedException {
        create(6);
        startServers();
        Thread.sleep(200);
        Level level=l.getLevel();
        LoggingHandler.setLevel(Level.SEVERE);
        Set<SocketAddress> discovered=discoverTestTablets();
        LoggingHandler.setLevel(level);
        p("discovered: "+discovered);
        stopServers();
        Thread.sleep(100);
        assertTrue(Thread.activeCount()<=threads);
    }
}

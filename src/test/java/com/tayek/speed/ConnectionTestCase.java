package com.tayek.speed;
import static org.junit.Assert.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.Required;
import com.tayek.io.LoggingHandler;
import com.tayek.tablet.Message.Type;
import static com.tayek.io.IO.*;
public class ConnectionTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.WARNING);
    }
    @After public void tearDown() throws Exception {}
    @Test public void testCreate() throws InterruptedException {
        printThreads();
        Thread.sleep(100);
        server.stopServer();
        Thread.sleep(200);
        if(Thread.activeCount()>threads) printThreads();
        assertTrue(Thread.activeCount()<=threads);
    }
    @Test public void testCreateAndStartup() throws InterruptedException {
        printThreads();
        server.startServer();
        Thread.sleep(100);
        server.stopServer();
        Thread.sleep(200);
        if(Thread.activeCount()>threads) printThreads();
        assertTrue(Thread.activeCount()<=threads);
    }
    @Test public void testBroadcast() throws InterruptedException {
        printThreads();
        server.startServer();
        p(""+server);
        p(""+server.messageFactory());
        Thread.sleep(100);
        server.broadcast(server.messageFactory().other(Type.dummy,"1","T1"));
        Thread.sleep(200);
        server.stopServer();
        Thread.sleep(200);
        if(Thread.activeCount()>threads) printThreads();
        assertTrue(Thread.activeCount()<=threads);
    }
    int service=serviceBase++;
    int threads=Thread.activeCount();
    Server server=Server.factory.create(new Required("test","localhost",service));
    static int serviceBase=44444;
}

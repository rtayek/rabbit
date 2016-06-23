package com.tayek.speed;
import static org.junit.Assert.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.Required;
import com.tayek.io.LoggingHandler;
import com.tayek.tablet.Message.Type;
import com.tayek.tablet.MessageReceiver.Model;
import static com.tayek.io.IO.*;
public class ConnectionTestCase extends AbstractServerTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        super.setUp();
    }
    @After public void tearDown() throws Exception {
        super.tearDown();
    }
    @Test public void testCreate() throws InterruptedException {
        LoggingHandler.setLevel(Level.ALL);
        printThreads();
        Thread.sleep(100);
        server.stopServer();
        Thread.sleep(200);
        checkThreads(failOnExtraThreads);
    }
    @Test public void testCreateAndStartup() throws InterruptedException {
        printThreads();
        server.startServer();
        Thread.sleep(100);
        server.stopServer();
        Thread.sleep(200);
        checkThreads(failOnExtraThreads);
    }
    @Test public void testBroadcast() throws InterruptedException {
        printThreads();
        server.startServer();
        p(""+server);
        p(""+server.messageFactory());
        Thread.sleep(100);
        server.broadcast(server.messageFactory().other(Type.dummy,"1",server.id()));
        Thread.sleep(200);
        //server.stopServer();
        //Thread.sleep(200);
        checkThreads(failOnExtraThreads);
    }
    Required required=new Required("localhost",service);
    Server server=Server.factory.create(required,Model.mark1.clone());
    static boolean failOnExtraThreads=false;
}

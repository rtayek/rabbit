package com.tayek;
import static com.tayek.io.IO.*;
import static com.tayek.speed.Server.factory;
import static org.junit.Assert.*;
import java.net.ServerSocket;
import java.util.*;
import org.junit.*;
import org.junit.rules.TestRule;
import com.tayek.io.IO;
import com.tayek.speed.Server;
import com.tayek.tablet.Group;
import com.tayek.tablet.MessageReceiver.Model;
public class BasicTabletTestCase {
    @Rule public TestRule watcher=new MyTestWatcher();

    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void test1Close() throws InterruptedException {
        Server server=factory.create(required);
        assertTrue(server.startServer());
        ServerSocket serverSocket=server.serverSocket();
        p("before stop: "+IO.toString(serverSocket));
        server.stopServer();
        p("before sleep: "+IO.toString(serverSocket));
        Thread.sleep(1_000);
        p("after sleep: "+IO.toString(serverSocket));
    }
    @Test public void test1() {
        Server server=factory.create(required);
        assertTrue(server.startServer());
        server.stopServer();
        p("start 2");
        assertTrue(server.startServer());
        server.stopServer();
    }
    @Test public void test2() {
        Map<String,Required> requireds=new TreeMap<>();
        requireds.put(required.id,required);
        Group group=new Group("1",requireds,Model.mark1);
        Tablet tablet=Tablet.factory.create2(group,required.id,group.getModelClone());
        assertTrue(tablet.startServer());
        tablet.stopServer();
        assertTrue(tablet.startServer());
        tablet.stopServer();
    }
    final Required required=new Required("localhost",defaultReceivePort);
}

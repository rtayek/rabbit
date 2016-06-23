package com.tayek;
import static org.junit.Assert.*;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import org.junit.*;
import org.junit.rules.TestRule;
import com.tayek.io.LoggingHandler;
import static com.tayek.io.IO.*;
import com.tayek.speed.Server;
import com.tayek.tablet.MessageReceiver.Model;
public class ServerSocketTestCase {
    @Rule public TestRule watcher=new MyTestWatcher();

    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.WARNING);
        service=++staticService;
    }
    @After public void tearDown() throws Exception {}
    @Test public void mytest() throws Exception {
        Required required=new Required("localhost",service);
        Server server=Server.factory.create(required,Model.mark1.clone());
        boolean ok=server.startServer();
        assertTrue(ok);
        ((Server.Factory.FactoryImpl.ServerABC)server).isShuttingDown=true;
        server.serverSocket().close();
        System.out.println("after 1'st close: server socket: isBound: "+server.serverSocket().isBound()+", isClosed: "+server.serverSocket().isClosed());
        assertTrue(server.startServer());
        ((Server.Factory.FactoryImpl.ServerABC)server).isShuttingDown=true;
        server.serverSocket().close();
        System.out.println("at end: server socket: isBound: "+server.serverSocket().isBound()+", isClosed: "+server.serverSocket().isClosed());
    }
    @Test public void test1() throws Exception {
        for(int i=0;i<n;i++) {
            System.out.println("test1 i: "+i);
            final ServerSocket ss=new ServerSocket();
            ss.bind(new InetSocketAddress(service));
            Thread t=new Thread(new Runnable() {
                @Override public void run() {
                    try {
                        final Socket s=ss.accept();
                        ss.close();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
            Socket s=new Socket("localhost",service);
            t.join();
            Thread.sleep(200);
            System.out.println("at end: server socket: isBound: "+ss.isBound()+", isClosed: "+ss.isClosed());
        }
    }
    @Test public void test2() throws InterruptedException,UnknownHostException,IOException {
        // see: http://stackoverflow.com/questions/10516030/java-server-socket-doesnt-reuse-address
        for(int i=0;i<n;i++) {
            System.out.println("test2 i: "+i);
            final ServerSocket ss=new ServerSocket();
            Thread t=new Thread(new Runnable() {
                @Override public void run() {
                    try {
                        ss.setReuseAddress(false); // does not seem to matter
                        ss.bind(new InetSocketAddress(service));
                        Socket s=ss.accept();
                        BufferedReader br=new BufferedReader(new InputStreamReader(s.getInputStream()));
                        System.out.println(br.readLine());
                        ss.close();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
            Socket s=new Socket("localhost",service);
            BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            bw.write("foo");
            bw.close();
            t.join();
            Thread.sleep(200);
            System.out.println("at end: server socket: isBound: "+ss.isBound()+", isClosed: "+ss.isClosed());
        }
    }
    int n=5,service;
    static int staticService=defaultReceivePort+2000;
}

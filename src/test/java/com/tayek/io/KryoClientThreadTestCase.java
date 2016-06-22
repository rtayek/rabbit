package com.tayek.io;
import static org.junit.Assert.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import org.junit.*;
import com.esotericsoftware.kryonet.*;
import com.esotericsoftware.minlog.Log;
public class KryoClientThreadTestCase {
    static class MyListener extends Listener {
        public void received(Connection connection,Object object) {
            received++;
        }
        int received;
    }
    @Before public void setUp() throws Exception {
        Log.set(Log.LEVEL_ERROR);
        server=new Server();
    }
    @After public void tearDown() throws Exception {}
    private void run() throws InterruptedException {
        server.start();
        try {
            InetAddress inetAddress=InetAddress.getByName(host);
            InetSocketAddress tcp=new InetSocketAddress(inetAddress,service);
            InetSocketAddress udp=new InetSocketAddress(inetAddress,service+1_000);
            server.bind(tcp,udp);
            System.out.println("server is bound to: "+tcp+", "+udp);
            server.addListener(myListener);
        } catch(IOException e) {
            e.printStackTrace();
            System.out.println("caught: "+e);
        }
        for(int i=0;i<n;i++)
            clients.add(new Client());
        for(Client client:clients)
            client.start();
        for(Client client:clients)
            try {
                client.connect(5000,host,service,service+1000);
                client.addListener(new Listener() {
                    public void received(Connection connection,Object object) {
                        if(object instanceof String) System.out.println("received: "+object);
                        else System.out.println("received: "+object);
                    }
                });
            } catch(IOException e) {
                e.printStackTrace();
                System.out.println("caught: "+e);
            }
        for(Client client:clients) {
            client.sendTCP("tcp");
            client.sendUDP("udp");
        }
        while(myListener.received<clients.size()*2)
            Thread.sleep(100);
        server.stop();
        for(Client client:clients)
            client.stop();
    }
    @Test public void test1() throws Exception {
        int threads=Thread.activeCount();
        n=1;
        run();
        Thread.sleep(5_000);
        System.out.println("total: "+myListener.received);
        assertTrue(Thread.activeCount()==threads);
    }
    @Test public void test10() throws Exception {
        int threads=Thread.activeCount();
        n=10;
        run();
        Thread.sleep(5_000);
        System.out.println("total: "+myListener.received);
        assertTrue(Thread.activeCount()==threads);
    }
    @Test public void test100() throws Exception {
        int threads=Thread.activeCount();
        n=100;
        run();
        Thread.sleep(5_000);
        System.out.println("total: "+myListener.received);
        assertTrue(Thread.activeCount()==threads);
    }
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    final String host="localhost";
    final int service=12345;
    Server server;
    final MyListener myListener=new MyListener();
    final List<Client> clients=new ArrayList<>();
    int n;
    int threads;
}

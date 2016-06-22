package com.tayek.io;
import static com.tayek.io.IO.*;
import static org.junit.Assert.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import org.junit.*;
import com.esotericsoftware.kryonet.*;
public class KryoTimeoutTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    private void startAndConectClients() {
        for(int i=0;i<n;i++) {
            final Client client=new Client();
            client.start();
            p("update thread: "+client.getUpdateThread());
            try {
                Thread.sleep(500);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
            final String key=""+i;
            final int service=service0;
            new Thread(new Runnable() {
                @Override public void run() {
                    System.out.println("connect thread: "+Thread.currentThread());
                    try {
                        client.connect(5000,host,service,service+1000);
                        client.addListener(new Listener() {
                            public void received(Connection connection,Object object) {
                                if(object instanceof String) System.out.println("received: "+object);
                                else System.out.println("received: "+object);
                            }
                        });
                        idToClient.put(key,client);
                    } catch(IOException e) {
                        e.printStackTrace();
                        System.out.println("caught: "+e);
                        idToClient.put(key,null);
                    }
                }
            }).start();
        }
    }
    @Test public void test() throws InterruptedException {
        server=new Server();
        server.start();
        try {
            InetAddress inetAddress=InetAddress.getByName(host);
            InetSocketAddress tcp=new InetSocketAddress(inetAddress,service0);
            InetSocketAddress udp=new InetSocketAddress(inetAddress,service0+1_000);
            server.bind(tcp,udp);
            System.out.println("server is bound to: "+tcp+", "+udp);
            //server.addListener(myListener);
        } catch(IOException e) {
            e.printStackTrace();
            System.out.println("caught: "+e);
        }
        startAndConectClients();
        while(idToClient.size()<n)
            try {
                Thread.sleep(100);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        Client client;
        for(int i=0;i<n;i++) {
            String key=""+i;
            if((client=idToClient.get(key))!=null) {
                client.sendTCP("tcp");
                client.sendUDP("udp");
            }
        }
        server.stop();
        for(Client client2:idToClient.values())
            if(client2!=null)
            client2.stop();
        Thread.sleep(2_000);
        System.out.println("threads: "+Thread.activeCount());
    }
    final String host="192.168.0.101";
    final int service0=12345;
    Server server;
    final int n=10;
    private final Map<String,Client> idToClient=new TreeMap<>();
}

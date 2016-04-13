package com.tayek.speed;
import static com.tayek.io.IO.*;
import static com.tayek.speed.Server.factory;
import static com.tayek.utilities.Utility.connect;
import static org.junit.Assert.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.io.LoggingHandler;
public abstract class AbstractServerTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.WARNING);
    }
    @After public void tearDown() throws Exception {}
    static Set<SocketAddress> discoverTestTablets() {
        int max=6;
        Set<SocketAddress> socketAddresses=new LinkedHashSet<>();
        for(int i=11;i<=11+max;i++)
            socketAddresses.add(new InetSocketAddress(tabletNetworkPrefix+i,serviceBase));
        for(int i=1;i<=max;i++)
            socketAddresses.add(new InetSocketAddress(fakeNetworkPrefix,serviceBase+i));
        for(int i=1;i<=max;i++)
            socketAddresses.add(new InetSocketAddress("192.168.0.101",serviceBase+i));
        Set<SocketAddress> good=new LinkedHashSet<>();
        for(int i=1;i<3;i++)
            for(SocketAddress socketAddress:socketAddresses) {
                Socket socket=connect(socketAddress,200);
                if(socket!=null) {
                    try {
                        socket.close();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                    good.add(socketAddress);
                    p("added: "+socketAddress);
                }
            }
        return good;
    }
    void create(int n) {
        for(Integer i=1;i<=n;i++)
            if(i==1) servers.add(factory.create("T"+i,"192.168.0.101",service+i,null));
            else servers.add(factory.create("T"+i,defaultHost,service+i,null));
    }
    void stopServers() throws InterruptedException {
        for(Server server:servers)
            server.stopServer();
    }
    void startServers() {
        for(Server server:servers)
            server.startServer();
    }
    void addSenders(int n) {
        p("adding sender(s) -------------------------------------");
        if(true) {
            for(Server server:servers)
                for(Server server2:servers)
                    if(server!=server2) {
                        SocketAddress socketAddress=new InetSocketAddress(server2.host(),server2.service());
                        server.createAndAddSender(server2.id(),socketAddress);
                    }
        } else {
            Iterator<Server> i=servers.iterator();
            Server first=i.next(),next;
            for(int j=1;j<=servers.size();j++)
                if(n>j) {
                    next=i.next();
                    p(first.id()+" is adding sender for: "+next.id());
                    first.createAndAddSender(next.id(),new InetSocketAddress(next.host(),next.service()));
                }
        }
    }
    int service=serviceBase++;
    int threads=Thread.activeCount();
    Set<Server> servers=new LinkedHashSet<>();
    static int serviceBase=55555;
}

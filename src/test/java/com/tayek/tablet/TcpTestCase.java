package com.tayek.tablet;
import static com.tayek.tablet.io.IO.*;
import static com.tayek.utilities.Utility.*;
import static org.junit.Assert.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.tayek.tablet.*;
import com.tayek.tablet.Messages.Message;
import com.tayek.tablet.MessageReceiver.DummyReceiver;
import com.tayek.tablet.io.*;
import com.tayek.tablet.io.IO.GetNetworkInterfacesCallable;
import com.tayek.tablet.io.Sender.Client;
@RunWith(Parameterized.class) public class TcpTestCase extends AbstractTabletTestCase {
    public TcpTestCase(SocketAddress socketAddress,Boolean replying) {
        this.socketAddress=socketAddress;
        this.replying=replying;
    }
    @Before public void setUp() throws Exception {
        super.setUp();
        printThreads=true;
    }
    @After public void tearDown() throws Exception {
        super.tearDown();
    }
    @Parameters public static Collection<Object[]> data() throws UnknownHostException,InterruptedException,ExecutionException {
        Set<InetAddress> inetAddresses=IO.runAndWait(new GetNetworkInterfacesCallable(Main.networkStub));
        p("addresses: "+inetAddresses);
        List<InetSocketAddress> list=new ArrayList<>();
        list.add(new InetSocketAddress("localhost",Main.defaultReceivePort+serviceOffset));
        list.add(new InetSocketAddress(InetAddress.getByName("127.0.0.1"),Main.defaultReceivePort+serviceOffset));
        list.add(new InetSocketAddress(Main.networkHost,Main.defaultReceivePort+serviceOffset));
        list.add(new InetSocketAddress(Main.testingHost,Main.defaultReceivePort+serviceOffset));
        List<Object[]> parameters=new ArrayList<Object[]>();
        for(InetSocketAddress inetSocketAddress:list) {
            parameters.add(new Object[] {inetSocketAddress,Boolean.FALSE});
            serviceOffset+=100;
            parameters.add(new Object[] {inetSocketAddress,Boolean.TRUE});
            serviceOffset+=100;
        }
        return parameters;
    }
    boolean sendAndReceiveOneMessage(SocketAddress socketAddress,boolean replying) throws UnknownHostException,IOException,InterruptedException {
        Receiver.DummyReceiver receiver=new Receiver.DummyReceiver();
        Histories history=new Histories();
        Server server=new Server(null,socketAddress,receiver,replying,history.server);
        server.startServer();
        // where is client socket bound to?
        Client client=new Client(socketAddress,replying,Histories.defaultConnectTimeout); 
        Message dummy=messages.dummy(1,1);
        client.l.info("sending: "+dummy);
        client.send(dummy,history.client);
        while(history.server.server.successes()+history.server.server.failures()==0)
            Thread.yield();
        server.stopServer();
        // p(receiver.t);
        if(receiver.message==null) p("null message!");
        checkHistory(null,history,replying,1,replying);
        boolean isOk=receiver.message!=null&&dummy.toString().equals(receiver.message.toString());
        return isOk;
    }
    @Test() public void testConnectAndClose() throws Exception {
        Histories history=new Histories();
        Receiver.DummyReceiver receiver=new Receiver.DummyReceiver();
        Server server=null;
        try {
            server=new Server(null,socketAddress,receiver,false,history.server);
        } catch(Exception e) {
            p("socket address: "+socketAddress+" failed! &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        }
        server.startServer();
        Socket socket=Client.connect(socketAddress,100,null);
        if(socket!=null) p("connected to: "+socket);
        assertTrue(socket!=null);
        Thread.sleep(100);
        socket.close();
        Thread.sleep(100);
        //while(history.serverHistory.received==0)
        //Thread.yield();
        server.stopServer();
        // p(receiver.t);
        if(receiver.message==null) p("null");
        //checkHistory(history,Group.defaultOptions.replying,1);
        p("received: "+receiver.message);
        // assert??
    }
    @Test(timeout=100) public void testOnce() throws Exception {
        //LoggingHandler.setLevel(Level.ALL);
        if(!sendAndReceiveOneMessage(socketAddress,replying)) fail("failed!");
    }
    @Test(timeout=200) public void testTwice() throws Exception {
        if(!sendAndReceiveOneMessage(socketAddress,replying)) fail("failed!");
        if(!sendAndReceiveOneMessage(socketAddress,replying)) fail("failed!");
    }
    @Test(timeout=300) public void testThrice() throws Exception {
        if(!sendAndReceiveOneMessage(socketAddress,replying)) fail("failed!");
        if(!sendAndReceiveOneMessage(socketAddress,replying)) fail("failed!");
        if(!sendAndReceiveOneMessage(socketAddress,replying)) fail("failed!");
    }
    @Test(timeout=1_500) public void testManyTimes() throws Exception {
        for(Integer i=1;i<=10;i++) {
            //p("i="+i);
            if(!(sendAndReceiveOneMessage(socketAddress,replying))) {
                p("oops");
                fail("failed at: "+i);
            }
            Thread.sleep(10);
            if(i%1000==0) p(i.toString());
        }
    }
    final SocketAddress socketAddress;
    final boolean replying;
    int service;
    int threads;
    Messages messages=new Messages();
}

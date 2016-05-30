package com.tayek.failures;
import static com.tayek.io.IO.*;
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
import com.tayek.*;
import com.tayek.Sender.Client;
import com.tayek.io.*;
import com.tayek.io.IO.AddressesWithCallable;
import com.tayek.sablet.AbstractTabletTestCase;
import com.tayek.tablet.*;
import com.tayek.Tablet.*;
import com.tayek.tablet.Message.*;
import com.tayek.tablet.MessageReceiver.DummyReceiver;
import com.tayek.tablet.io.*;
import com.tayek.utilities.Single;
@RunWith(Parameterized.class) public class TcpTestCase extends AbstractTabletTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        AbstractTabletTestCase.setUpBeforeClass();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {
        AbstractTabletTestCase.tearDownAfterClass();
    }
    @Before public void setUp() throws Exception {
        super.setUp();
        //printThreads=true;
    }
    @After public void tearDown() throws Exception {
        super.tearDown();
    }
    public TcpTestCase(SocketAddress socketAddress,Boolean replying) {
        this.socketAddress=socketAddress;
        config.replying=replying;
        if(socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress=(InetSocketAddress)socketAddress;
            Required required=new Required(inetSocketAddress.getHostName(),inetSocketAddress.getPort());
            messages=Message.instance.create(required.host,required.service,new Single<Integer>(0));
        } else throw new RuntimeException(socketAddress+" is not an InetSocketAddress!");
    }
    @Parameters public static Collection<Object[]> data() throws UnknownHostException,InterruptedException,ExecutionException {
        AddressesWithCallable addressesWithCallable=new AddressesWithCallable(testingHost);
        addressesWithCallable.run();
        Set<InetAddress> inetAddresses=addressesWithCallable.addresses;
        LoggingHandler.init();
        int serviceOffset=6000; // hackish, reconcile with the one in the abstract test case. 
        l.info("addresses: "+inetAddresses);
        List<InetSocketAddress> list=new ArrayList<>();
        list.add(new InetSocketAddress("localhost",defaultReceivePort+serviceOffset));
        list.add(new InetSocketAddress(InetAddress.getByName("127.0.0.1"),defaultReceivePort+serviceOffset));
        list.add(new InetSocketAddress(defaultHost,defaultReceivePort+serviceOffset));
        list.add(new InetSocketAddress(testingHost,defaultReceivePort+serviceOffset));
        List<Object[]> parameters=new ArrayList<Object[]>();
        for(InetSocketAddress inetSocketAddress:list) {
            parameters.add(new Object[] {inetSocketAddress,Boolean.FALSE});
            serviceOffset+=100;
            parameters.add(new Object[] {inetSocketAddress,Boolean.TRUE});
            serviceOffset+=100;
        }
        return parameters;
    }
    boolean sendAndReceiveOneMessage(SocketAddress socketAddress) throws UnknownHostException,IOException,InterruptedException {
        Receiver.DummyReceiver receiver=new Receiver.DummyReceiver();
        Histories histories=new Histories();
        Server server=new Server(null,socketAddress,receiver,config,histories);
        server.startServer();
        // where is client socket bound to?
        Client client=new Client(socketAddress,config,histories);
        Message dummy=messages.other(Type.dummy,"1","1");
        l.info("sending: "+dummy);
        client.send(dummy);
        while(histories.receiverHistory.history.successes()+histories.receiverHistory.history.failures()==0)
            Thread.yield();
        server.stopServer();
        // p(receiver.t);
        if(receiver.message==null) p("null message!");
        checkHistory(null,histories,config.replying,1,true);
        boolean isOk=receiver.message!=null&&dummy.toString().equals(receiver.message.toString());
        return isOk;
    }
    @Test() public void testConnectAndClose() throws Exception {
        LoggingHandler.setLevel(Level.INFO);
        Histories histories=new Histories();
        Receiver.DummyReceiver receiver=new Receiver.DummyReceiver();
        Config config=new Config();
        Server server=null;
        try {
            server=new Server(null,socketAddress,receiver,config,histories);
        } catch(Exception e) {
            p("socket address: "+socketAddress+" failed! &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        }
        server.startServer();
        Socket socket=Client.connect(socketAddress,100,config,null);
        if(socket!=null) p("connected to: "+socket);
        else p("check to see if network interface is up!");
        assertTrue(socket!=null);
        Thread.sleep(100);
        socket.close();
        Thread.sleep(100);
        //while(histories.serverHistory.received==0)
        //Thread.yield();
        server.stopServer();
        // p(receiver.t);
        if(receiver.message==null) p("null");
        //checkHistory(histories,Group.defaultOptions.replying,1);
        p("received: "+receiver.message);
        // assert??
    }
    @Test(timeout=100) public void testOnce() throws Exception {
        if(!sendAndReceiveOneMessage(socketAddress)) fail("failed!");
    }
    @Test(timeout=200) public void testTwice() throws Exception {
        if(!sendAndReceiveOneMessage(socketAddress)) fail("failed!");
        if(!sendAndReceiveOneMessage(socketAddress)) fail("failed!");
    }
    @Test(timeout=300) public void testThrice() throws Exception {
        if(!sendAndReceiveOneMessage(socketAddress)) fail("failed!");
        if(!sendAndReceiveOneMessage(socketAddress)) fail("failed!");
        if(!sendAndReceiveOneMessage(socketAddress)) fail("failed!");
    }
    @Test(timeout=1_500) public void testManyTimes() throws Exception {
        for(Integer i=1;i<=10;i++) {
            //p("i="+i);
            if(!sendAndReceiveOneMessage(socketAddress)) {
                p("oops");
                fail("failed at: "+i);
            }
            Thread.sleep(10);
            if(i%1000==0) p(i.toString());
        }
    }
    final SocketAddress socketAddress;
    final Message.Factory messages; // change to messageFactory!
    //int service;
    int threads;
    Config config=new Config();
}

package com.tayek.io;
import static org.junit.Assert.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.*;
import org.junit.rules.TestRule;
import com.tayek.MyTestWatcher;
import com.tayek.io.IO;
import com.tayek.io.IO.*;
import static com.tayek.io.IO.*;
import static com.tayek.utilities.Utility.*;
public class IOTestCase {
    @Rule public TestRule watcher=new MyTestWatcher();

    @BeforeClass public static void setUpBeforeClass() throws Exception {
        LoggingHandler.init();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        p("get localhost: "+InetAddress.getLocalHost());
        host=InetAddress.getLocalHost().getHostName();
        p("host: "+host);
    }
    @After public void tearDown() throws Exception {
        printThreads();
    }
    @Test public void testNormalStuff() throws UnknownHostException {
        InetAddress[] x=InetAddress.getAllByName(host);
        p(host+" all by name: "+Arrays.asList(x));
        p(host+" get by name: "+InetAddress.getByName(host));
    }
    @Test public void testGetByName() throws InterruptedException,ExecutionException {
        GetByNameCallable task=new GetByNameCallable(testingHost);
        task.run();
        InetAddress inetAddress=task.inetAddress;
        assertTrue(inetAddress.getHostAddress().contains(testingHost));
    }
    @Test public void testGetNetworkInterfacesWithHost() throws InterruptedException,ExecutionException {
        AddressesWithCallable addressesWithCallable=new AddressesWithCallable(testingHost);
        addressesWithCallable.run();
        Set<InetAddress> inetAddresses=addressesWithCallable.addresses;
        assertTrue(inetAddresses.size()>0);
        if(inetAddresses.size()>1) p("more than one nic: "+inetAddresses);
        InetAddress inetAddress=inetAddresses.iterator().next();
        assertTrue(inetAddress.getHostAddress().contains(testingHost));
    }
    @Test public void testGetNetworkInterfacesWithNetworkPrefix() throws InterruptedException,ExecutionException {
        // you will need a wireless nic or be able to plug in to the real network for this to work
        AddressesWithCallable addressesWithCallable=new AddressesWithCallable(testingHost);
        addressesWithCallable.run();
        Set<InetAddress> inetAddresses=addressesWithCallable.addresses;
        assertTrue(inetAddresses.size()>0);
        if(inetAddresses.size()>1) p("more than one nic: "+inetAddresses);
        InetAddress inetAddress=inetAddresses.iterator().next();
        assertTrue(inetAddress!=null);
        assertTrue(inetAddress.getHostAddress().contains(tabletRouterPrefix));
    }
    @Test public void testGetNetworkInterfacesWithNetworkPrefix192dot168() throws InterruptedException,ExecutionException {
        // you will need a wireless nic or be able to plug in to the real network for this to work
        AddressesWithCallable addressesWithCallable=new AddressesWithCallable(testingHost);
        addressesWithCallable.run();
        Set<InetAddress> inetAddresses=addressesWithCallable.addresses;
        assertTrue(inetAddresses.size()>0);
        if(inetAddresses.size()>1) p("more than one nic: "+inetAddresses);
        boolean foundOne=false;
        for(InetAddress inetAddress:inetAddresses)
            if(inetAddress.getHostAddress().contains(tabletRouterPrefix)) foundOne=true;
        assertTrue(foundOne);
    }
    String host;
}

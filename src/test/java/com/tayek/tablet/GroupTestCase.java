package com.tayek.tablet;
import static org.junit.Assert.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import org.junit.*;
import com.tayek.tablet.io.*;
import static com.tayek.tablet.io.IO.*;
import com.tayek.tablet.io.IO.GetNetworkInterfacesCallable;
import com.tayek.tablet.Group.*;
public class GroupTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        //LoggingHandler.init();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        io=new IO();
    }
    @After public void tearDown() throws Exception {}
    @Test public void testGetTablet() throws UnknownHostException,InterruptedException,ExecutionException {
        Set<InetAddress> inetAddresses=io.runAndWait(new GetNetworkInterfacesCallable(Main.defaultTestingHost));
        assertTrue(inetAddresses.size()>0);
        if(inetAddresses.size()>1)
            p("more than one nic: "+inetAddresses);
        InetAddress inetAddress=inetAddresses.iterator().next();
        Group group=new Group(1,new Groups().groups.get("g2"),Model.mark1,false);
        Tablet tablet=group.getTablet(inetAddress,null);
        assertEquals(tablet.tabletId(),group.tabletIds().iterator().next());
    }
    @Test public void testGetTabletWithService() throws UnknownHostException,InterruptedException,ExecutionException {
        Set<InetAddress> inetAddresses=io.runAndWait(new GetNetworkInterfacesCallable(Main.defaultTestingHost));
        assertTrue(inetAddresses.size()>0);
        if(inetAddresses.size()>1)
            p("more than one nic: "+inetAddresses);
        InetAddress inetAddress=inetAddresses.iterator().next();
        Group group=new Group(1,new Groups().groups.get("g2"),Model.mark1,false);
        Tablet tablet=group.getTablet(inetAddress,group.info(5).service);
        Iterator<Integer> i=group.tabletIds().iterator();
        i.next(); // skip the first tablet
        assertEquals(tablet.tabletId(),i.next()); // fragile
    }
    IO io;
}

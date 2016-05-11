package com.tayek.tablet;
import static org.junit.Assert.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import org.junit.*;
import org.junit.rules.TestRule;
import static com.tayek.io.IO.*;
import com.tayek.*;
import com.tayek.io.IO;
import com.tayek.tablet.Group.*;
import com.tayek.tablet.MessageReceiver.Model;
public class GroupTestCase {
    @Rule public TestRule watcher=new MyTestWatcher();

    @BeforeClass public static void setUpBeforeClass() throws Exception {
        //LoggingHandler.init();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void testATabletId(){
        Integer i=123;
        Object id=aTabletId(i);
        p("id: "+id+" "+id.getClass().getName());
        assertTrue(id instanceof String);
    }
    @Test public void testGetTabletTabletIdFromInetAddressForG2() throws UnknownHostException,InterruptedException,ExecutionException {
        Set<InetAddress> inetAddresses=IO.runAndWait(new AddressesWithCallable(testingHost));
        assertTrue(inetAddresses.size()>0);
        if(inetAddresses.size()>1) p("more than one nic: "+inetAddresses);
        InetAddress inetAddress=inetAddresses.iterator().next();
        p("inetAddress: "+inetAddress.toString());
        Group group=new Group("1",new Groups().groups.get("g2OnPc"),Model.mark1);
        String tabletId=group.getTabletIdFromInetAddress(inetAddress,null);
        TabletImpl2 tablet=(TabletImpl2)Tablet.factory.create2(group,tabletId,group.getModelClone());
        assertEquals(tablet.tabletId(),group.keys().iterator().next());
    }
    @Test public void testGetTabletTabletIdFromInetAddressForG0() throws UnknownHostException,InterruptedException,ExecutionException {
        InetAddress inetAddress=InetAddress.getByName("192.168.0.11");
        p(inetAddress.toString());
        Group group=new Group("1",new Groups().groups.get("g0"),Model.mark1);
        String tabletId=group.getTabletIdFromInetAddress(inetAddress,null);
        TabletImpl2 tablet=(TabletImpl2)Tablet.factory.create2(group,tabletId,group.getModelClone());
        assertEquals(tablet.tabletId(),group.keys().iterator().next()); // fails now since 1 is not forst!
    }
    @Test public void testGetTabletWithService() throws UnknownHostException,InterruptedException,ExecutionException {
        Set<InetAddress> inetAddresses=IO.runAndWait(new AddressesWithCallable(testingHost));
        assertTrue(inetAddresses.size()>0);
        if(inetAddresses.size()>1) p("more than one nic: "+inetAddresses);
        InetAddress inetAddress=inetAddresses.iterator().next(); // works by accident!
        Group group=new Group("1",new Groups().groups.get("g2OnPc"),Model.mark1);
        p("group: "+group);
        p("keys: "+group.keys());
        Iterator<String> i=group.keys().iterator();
        i.next();
        String tabletId=group.getTabletIdFromInetAddress(inetAddress,group.required(i.next()).service);
        TabletImpl2 tablet=(TabletImpl2)Tablet.factory.create2(group,tabletId,group.getModelClone());
        i=group.keys().iterator();
        i.next(); // skip the first tablet
        assertEquals(tablet.tabletId(),i.next()); // fragile
    }
}

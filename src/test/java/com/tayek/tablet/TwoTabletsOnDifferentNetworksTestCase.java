package com.tayek.tablet;
import static com.tayek.tablet.io.IO.p;
import static org.junit.Assert.*;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import org.junit.*;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.*;
public class TwoTabletsOnDifferentNetworksTestCase extends AbstractTabletTestCase {
    @Before public void setUp() throws Exception {
        super.setUp();
        Map<Integer,Info> infos=new Groups().groups.get("g1each");
        Map<Integer,Info> infos2=new TreeMap<>();
        p("info: "+infos);
        for(int tabletId:infos.keySet()) {
            Info info=infos.get(tabletId);
            infos2.put(tabletId,new Info(info.name,info.host,info.service+serviceOffset));
        }
        p("service offset: "+serviceOffset);
        tablets=Group.createGroupAndstartTablets(infos2);
    }
    @After public void tearDown() throws Exception {
        shutdown();
        super.tearDown();
    }
    @Test(timeout=200) public void testDummy2() throws InterruptedException,UnknownHostException,ExecutionException {
        sendOneDummyMessageFromEachTabletAndWaitAndShutdown(false);
        if(false) for(Tablet tablet:tablets)
            p(tablet.toString2());
    }
    @Test() public void testDummy2Brokem() throws InterruptedException,UnknownHostException,ExecutionException {
        for(Tablet tablet:tablets)
            tablet.stopListening(); // so send will fail
        Thread.sleep(100);
        sendOneDummyMessageFromEachTablet();
        Thread.sleep(500);
        shutdown();
        for(Tablet tablet:tablets) {
            Histories history=tablet.group.info(tablet.tabletId()).history;
            //p(tablet+" history: "+tablet.group.info(tablet.tabletId()).history);
            assertEquals(new Integer(2),history.client.client.failures());
        }
    }
    Group group;
}

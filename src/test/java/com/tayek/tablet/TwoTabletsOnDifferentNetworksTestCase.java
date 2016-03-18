package com.tayek.tablet;
import static com.tayek.utilities.Utility.*;
import static org.junit.Assert.*;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.*;
import com.tayek.tablet.io.LoggingHandler;
public class TwoTabletsOnDifferentNetworksTestCase extends AbstractTabletTestCase {
    @Before public void setUp() throws Exception {
        super.setUp();
        Map<Object,Info> infos=new Groups().groups.get("g1each");
        Map<Object,Info> infos2=new LinkedHashMap<>();
        p("info: "+infos);
        for(Object tabletId:infos.keySet()) {
            Info info=infos.get(tabletId);
            infos2.put(tabletId,new Info(info.iD,info.host,info.service+serviceOffset));
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
        LoggingHandler.setLevel(Level.SEVERE);
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

package com.tayek.sablet;
import static org.junit.Assert.*;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import org.junit.*;
import com.tayek.*;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.*;
import com.tayek.tablet.MessageReceiver.Model;
import static com.tayek.io.IO.*;
public class TwoTabletsOnDifferentNetworksTestCase extends AbstractTabletTestCase {
    @Before public void setUp() throws Exception {
        super.setUp();
        Map<String,Required> requireds=new Groups().groups.get("g1each");
        Map<String,Required> requireds2=new TreeMap<>();
        p("required: "+requireds);
        for(Object tabletId:requireds.keySet()) {
            Required required=requireds.get(tabletId);
            requireds2.put(required.id,new Required(required.id,required.host,required.service+serviceOffset));
        }
        p("service offset: "+serviceOffset);
        Group group=new Group("1",requireds2,Model.mark1); // why 2?
        tablets=group.createGroupAndstartTablets(group.groupId,requireds2);
    }
    @After public void tearDown() throws Exception {
        shutdown();
        super.tearDown();
    }
    @Test(timeout=200) public void testDummy2() throws InterruptedException,UnknownHostException,ExecutionException {
        sendOneDummyMessageFromEachTabletAndWaitAndShutdown(false);
    }
    @Test() public void testDummy2Brokem() throws InterruptedException,UnknownHostException,ExecutionException {
        for(TabletImpl2 tablet:tablets)
            tablet.stopListening(); // so send will fail
        Thread.sleep(100);
        sendOneDummyMessageFromEachTablet();
        Thread.sleep(2_000);
        for(TabletImpl2 tablet:tablets) {
            Histories histories=tablet.histories();
            p(tablet.tabletId()+": "+histories);
        }
        shutdown();
        for(TabletImpl2 tablet:tablets) {
            Histories histories=tablet.histories();
            assertTrue(new Integer(2)<=histories.senderHistory.history.failures());
            // retries that fail get counted as failures!
        }
    }
}

package com.tayek.sablet;
import static org.junit.Assert.*;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import org.junit.*;
import com.tayek.*;
import com.tayek.tablet.*;
import static com.tayek.tablet.Main.Stuff.*;
import static com.tayek.io.IO.*;
public class TwoTabletsOnDifferentNetworksTestCase extends AbstractTabletTestCase {
    @Before public void setUp() throws Exception {
        super.setUp();
        Map<String,Required> requireds=new Groups().groups.get("g1each");
        Map<String,Required> requireds2=new LinkedHashMap<>();
        p("required: "+requireds);
        for(Object tabletId:requireds.keySet()) {
            Required required=requireds.get(tabletId);
            requireds2.put(required.iD,new Required(required.iD,required.host,required.service+serviceOffset));
        }
        p("service offset: "+serviceOffset);
        tablets=Tablet.createGroupAndstartTablets(requireds2);
    }
    @After public void tearDown() throws Exception {
        shutdown();
        super.tearDown();
    }
    @Test(timeout=200) public void testDummy2() throws InterruptedException,UnknownHostException,ExecutionException {
        sendOneDummyMessageFromEachTabletAndWaitAndShutdown(false);
    }
    @Test() public void testDummy2Brokem() throws InterruptedException,UnknownHostException,ExecutionException {
        for(Tablet tablet:tablets)
            tablet.stopListening(); // so send will fail
        Thread.sleep(100);
        sendOneDummyMessageFromEachTablet();
        Thread.sleep(2_000);
        for(Tablet tablet:tablets) {
            Histories histories=tablet.histories();
            p(tablet.tabletId()+": "+histories);
        }
        shutdown();
        for(Tablet tablet:tablets) {
            Histories histories=tablet.histories();
            assertTrue(new Integer(2)<=histories.senderHistory.history.failures());
            // retries that fail get counted as failures!
        }
    }
}

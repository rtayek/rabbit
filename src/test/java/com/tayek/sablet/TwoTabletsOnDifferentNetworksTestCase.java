package com.tayek.sablet;
import static org.junit.Assert.*;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import org.junit.*;
import com.tayek.tablet.*;
import static com.tayek.tablet.Main.Stuff.*;
import static com.tayek.io.IO.*;
public class TwoTabletsOnDifferentNetworksTestCase extends AbstractTabletTestCase {
    @Before public void setUp() throws Exception {
        super.setUp();
        Map<String,Info> infos=new Groups().groups.get("g1each");
        Map<String,Info> infos2=new LinkedHashMap<>();
        p("info: "+infos);
        for(Object tabletId:infos.keySet()) {
            Info info=infos.get(tabletId);
            infos2.put(info.iD,new Info(info.iD,info.host,info.service+serviceOffset));
        }
        p("service offset: "+serviceOffset);
        tablets=Tablet.createGroupAndstartTablets(infos2);
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
        Thread.sleep(500);
        shutdown();
        for(Tablet tablet:tablets) {
            Histories history=tablet.histories();
            assertEquals(new Integer(2),history.client.client.failures());
        }
    }
}

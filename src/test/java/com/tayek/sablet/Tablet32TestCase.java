package com.tayek.sablet;
import static com.tayek.utilities.Utility.*;
import static org.junit.Assert.*;
import java.net.SocketAddress;
import java.util.*;
import org.junit.*;
import com.tayek.*;
import com.tayek.tablet.*;
import com.tayek.tablet.Main;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.Message.Type;
import com.tayek.tablet.Main.Stuff;
import com.tayek.tablet.Main.Stuff.*;
import static com.tayek.io.IO.*;
import com.tayek.utilities.*;
public class Tablet32TestCase extends AbstractTabletTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        AbstractTabletTestCase.setUpBeforeClass();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {
        AbstractTabletTestCase.tearDownAfterClass();
    }
    @Before public void setUp() throws Exception {
        super.setUp();
    }
    @After public void tearDown() throws Exception {
        super.tearDown();
    }
    @Test(timeout=10_000) public void testAll32WithOneMessage() throws InterruptedException {
        tablets=Tablet.createForTest(32,serviceOffset);
        startListening();
        Et et=new Et();
        sendOneDummyMessageFromEachTabletAndWaitAndShutdown(false);
        for(Tablet tablet:tablets) {
            checkHistory(tablet,tablets.size(),false);
        }
    }
    private void justOneWithOneMessage() throws InterruptedException {
        Map<String,Required> map=new LinkedHashMap<>();
        for(Integer i=1;i<=32;i++) // hack address so it can't connect
            map.put("T"+i+" on PC",new Required("T"+i+" on PC",testingHost,defaultReceivePort+100+serviceOffset+i));
        Stuff stuff=new Stuff(1,map,Model.mark1);
        p("map: "+map);
        tablets=Tablet.create(stuff);
        Tablet first=tablets.iterator().next();
        boolean areAllLiestening=true;
        if(areAllLiestening) startListening();
        else {
            SocketAddress socketAddress=first.stuff.socketAddress(first.tabletId());
            if(!first.startListening(socketAddress)) fail(first+" startListening() retuns false!");
        }
        first.broadcast(first.stuff.messages.other(Type.dummy,first.groupId,first.tabletId()),first.stuff);
        Thread.sleep(700);
        Histories histories=first.histories();
        assertEquals(one,histories.receiverHistory.history.successes());
        assertEquals(first.stuff.replying?one:zero,histories.receiverHistory.replies.successes());
        assertEquals(one,histories.senderHistory.history.successes());
        assertEquals(first.stuff.replying?one:zero,histories.senderHistory.replies.successes());
        assertEquals(zero,histories.receiverHistory.history.failures());
        if(areAllLiestening) assertEquals(zero,histories.senderHistory.history.failures());
        else assertEquals(thirtyOne,histories.senderHistory.history.failures());
        p("send time for all: "+sendTime);
    }
    // some of these will fail with not all sent.
    // i think this is because broadcast() does not wait.
    // so maybe we need some sleep in here before the asserts.
    @Test public void testJustOneWithOneMessage() throws InterruptedException {
        for(int i=0;i<1;i++)
            justOneWithOneMessage();
    }
    @Test(timeout=10_000) public void testSendOneDummyMessageFromFirstTabletAndWait() throws InterruptedException {
        tablets=Tablet.createForTest(32,serviceOffset);
        startListening();
        Et et=new Et();
        sendOneDummyMessageFromFirstTabletAndWaitAndShutdown(false);
        p("send time: "+et);
    }
    Histogram sendTime=new Histogram(10,0,1_000);
    static final Integer thirtyOne=new Integer(31);
    static final Integer thirtyTwo=new Integer(32);
}

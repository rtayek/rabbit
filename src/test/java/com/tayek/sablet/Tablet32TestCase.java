package com.tayek.sablet;
import static com.tayek.utilities.Utility.*;
import static org.junit.Assert.*;
import java.net.SocketAddress;
import java.util.*;
import org.junit.*;
import com.tayek.tablet.*;
import com.tayek.tablet.Main;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.Messages.Type;
import com.tayek.tablet.Main.Stuff;
import com.tayek.tablet.Main.Stuff.Info;
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
        Map<String,Info> map=new LinkedHashMap<>();
        for(Integer i=1;i<=32;i++) // hack address so it can't connect
            map.put("T"+i+" on PC",new Info("T"+i+" on PC",Main.testingHost,Main.defaultReceivePort+100+serviceOffset+i));
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
        Histories history=first.histories();
        assertEquals(one,history.server.server.successes());
        assertEquals(first.stuff.replying?one:zero,history.server.replies.successes());
        assertEquals(one,history.client.client.successes());
        assertEquals(first.stuff.replying?one:zero,history.client.replies.successes());
        assertEquals(zero,history.server.server.failures());
        if(areAllLiestening) assertEquals(zero,history.client.client.failures());
        else assertEquals(thirtyOne,history.client.client.failures());
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
    Histogram sendTime=new Histogram(10,0,1000);
    static final Integer thirtyOne=new Integer(31);
    static final Integer thirtyTwo=new Integer(32);
}

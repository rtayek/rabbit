package com.tayek.sablet;
import static com.tayek.io.IO.*;
import static com.tayek.utilities.Utility.*;
import static org.junit.Assert.*;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.Description;
import com.tayek.*;
import com.tayek.io.*;
import com.tayek.tablet.*;
import com.tayek.Tablet.*;
import com.tayek.tablet.Group.TabletImpl2;
import com.tayek.tablet.Message.Type;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.utilities.Et;
public abstract class AbstractTabletTestCase {
    @Rule public TestRule watcher=new MyTestWatcher();
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        LoggingHandler.init();
        // put the ip addresses that we need in here!
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {
        if(staticPrintThreads) printThreads();
    }
    @Before public void setUp() throws Exception {
        LoggingHandler.init();
        LoggingHandler.setLevel(defaultLevel);
        printThreads=false;
        Config.defaultConnectTimeout=200;
        threads=Thread.activeCount();
        staticServiceOffset+=100;
        serviceOffset=staticServiceOffset; // find out why this meeds to be 100, try to make it 1!
    }
    @After public void tearDown() throws Exception { 
        boolean anyFailures=false;
        if(tablets!=null) for(Tablet tablet:tablets)
            anyFailures|=tablet.histories().anyFailures();
        if(printStats/*||anyFailures*/) {
            //p("printStats or failures!");
            printStats("stats for: "+this+": "+(anyFailures?"failures":""));
        }
        checkThreads(true);
        LoggingHandler.setLevel(defaultLevel);
    }
    void checkThreads(boolean fail) {
        if(Thread.activeCount()<threads) {
            printThreads();
            if(fail) fail("extra threads!");
            else {
                p("extra threads:");
                printThreads();
            }
        }
    }
    protected void startListening() {
        Tablet first=tablets.iterator().next();
        for(Tablet tablet:tablets) {
            if(tablet instanceof TabletImpl2) {
                if(!((TabletImpl2)tablet).startServer()) fail(tablet+" startListening() retuns false!");
                assertNotNull(((TabletImpl2)tablet).server);
            }
            assertEquals(first.model().serialNumber,tablet.model().serialNumber);
            //assertEquals(first.stuff.serialNumber,tablet.stuff.serialNumber);
        }
    }
    public void sendOneDummyMessageFromEachTablet() {
        for(Tablet tablet:tablets)
            sendOneDummyMessageFromTablet(tablet);
    }
    void sendOneDummyMessageFromTablet(Tablet tablet) {
        tablet.broadcast(tablet.messageFactory().other(Type.dummy,tablet.group().groupId,tablet.tabletId()));
    }
    public void waitForEachTabletToReceiveAtLeastOneMessageFromEachTablet(boolean sleepAndPrint) throws InterruptedException {
        boolean once=false;
        boolean done=false;
        while(!done) {
            done=true;
            for(Tablet tablet:tablets) {
                Histories histories=tablet.histories();
                // put history into tablet for convenience?
                if(tablet.config().replying) {
                    if(histories.senderHistory.replies.successes()<tablets.size()) {
                        done=false;
                        break;
                    }
                } else {
                    if(histories.receiverHistory.history.successes()<tablets.size()) {
                        done=false;
                        break;
                    }
                    // check for sent also
                    if(histories.senderHistory.history.successes()<tablets.size()) {
                        done=false;
                        break;
                    }
                }
                // fast fail: check history for any failures and return early.
                // this should avoid timeouts and make the tests run faster.
                if(histories.failures()>0) {
                    if(!once) {
                        once=true;
                        p("failures: "+histories);
                    }
                    // might cause some tests to fail if we return early
                    // maybe put a guard on this
                    // return;
                }
                if(!sleepAndPrint) Thread.yield();
            }
            if(sleepAndPrint) p("-----------------");
            for(Tablet tablet:tablets) {
                Histories history=tablet.histories();
                if(sleepAndPrint) {
                    p("history "+history);
                }
            }
            if(sleepAndPrint) Thread.sleep(500);
        }
        if(false) for(Tablet tablet:tablets)
            p("history: "+tablet.histories());
        for(Tablet tablet:tablets) {
            Histories histories=tablet.histories();
            if(histories.receiverHistory.history.successes()>tablets.size()) l.warning(tablet+" received too many messages: "+histories.receiverHistory.history.successes());
        }
    }
    void waitForEachTabletToReceiveAtLeastOneMessageFromFirstTablet(boolean sleepAndPrint) throws InterruptedException {
        boolean once=false;
        boolean done=false;
        Tablet first=tablets.iterator().next();
        while(!done) {
            done=true;
            for(Tablet tablet:tablets) {
                Histories histories=tablet.histories();
                // put history into tablet for convenience?
                if(tablet.config().replying) {
                    if(histories.senderHistory.replies.successes()<1) {
                        done=false;
                        break;
                    }
                } else {
                    if(histories.receiverHistory.history.successes()<1) {
                        done=false;
                        break;
                    }
                    // check for sent also
                    if(tablet.equals(first)) if(histories.senderHistory.history.successes()<1) {
                        done=false;
                        break;
                    }
                }
                // fast fail: check history for any failures and return early.
                // this should avoid timeouts and make the tests run faster.
                if(histories.failures()>0) {
                    if(!once) {
                        once=true;
                        p("failures: "+histories);
                    }
                    // might cause some tests to fail if we return early
                    // maybe put a guard on this
                    // return;
                }
                if(!sleepAndPrint) Thread.yield();
            }
            if(sleepAndPrint) p("-----------------");
            for(Tablet tablet:tablets) {
                Histories history=tablet.histories();
                if(sleepAndPrint) {
                    p("history "+history);
                }
            }
            if(sleepAndPrint) Thread.sleep(500);
        }
        if(false) for(Tablet tablet:tablets)
            p("history: "+tablet.histories());
        for(Tablet tablet:tablets) {
            Histories history=tablet.histories();
            if(history.receiverHistory.history.successes()>tablets.size()) l.warning(tablet+" received too many messages: "+history.receiverHistory.history.successes());
        }
    }
    protected void shutdown() {
        for(Tablet tablet:tablets) {
            if(tablet instanceof TabletImpl2) {
                TabletImpl2 t2=(TabletImpl2)tablet;
                t2.stopServer();
            }
        }
    }
    protected void sendOneDummyMessageFromEachTabletAndWait(boolean sleepAndPrint) throws InterruptedException {
        sendOneDummyMessageFromEachTablet();
        Et et=new Et();
        waitForEachTabletToReceiveAtLeastOneMessageFromEachTablet(sleepAndPrint);
        //p("wait for "+tablets.size()+" tablets, took: "+et);
        for(Tablet tablet:tablets) {
            Histories history=tablet.histories();
            if(history.receiverHistory.history.successes()!=tablets.size()) p(tablet+" received: "+history.receiverHistory.history.successes()+" instead of "+tablets.size());
            if(tablet instanceof TabletImpl2) {
                TabletImpl2 t2=(TabletImpl2)tablet;
                checkHistory(t2,tablets.size(),false);
            } else {
                p("how do i check history?");
            }
        }
    }
    protected void sendOneDummyMessageFromFirstTabletAndWait(boolean sleepAndPrint) throws InterruptedException {
        sendOneDummyMessageFromTablet(tablets.iterator().next());
        Et et=new Et();
        waitForEachTabletToReceiveAtLeastOneMessageFromFirstTablet(sleepAndPrint);
        p("wait for "+tablets.size()+" tablets, took: "+et);
        for(Tablet tablet:tablets) {
            Histories histories=tablet.histories();
            if(histories.receiverHistory.history.successes()!=tablets.size()) p(tablet+" received: "+histories.receiverHistory.history.successes()+" instead of "+tablets.size());
            if(tablet instanceof TabletImpl2) {
                TabletImpl2 t2=(TabletImpl2)tablet;
                checkHistory(t2,tablets.size(),true);
            } else {
                p("how do i check history?");
            }
        }
    }
    protected void sendOneDummyMessageFromEachTabletAndWaitAndShutdown(boolean sleepAndPrint) throws InterruptedException {
        sendOneDummyMessageFromEachTabletAndWait(sleepAndPrint);
        shutdown();
    }
    protected void sendOneDummyMessageFromFirstTabletAndWaitAndShutdown(boolean sleepAndPrint) throws InterruptedException {
        sendOneDummyMessageFromFirstTabletAndWait(sleepAndPrint);
        //Thread.sleep(10);
        shutdown();
    }
    public void checkHistory(Tablet tablet,Integer n,boolean oneTablet) {
        checkHistory(tablet,tablet.histories(),tablet.config().replying,n,oneTablet);
    }
    public void checkHistory(Tablet tablet,Histories histories,boolean replying,Integer n,boolean oneTablet) {
        //p("history: "+histories);
        assertEquals(oneTablet?one:n,histories.receiverHistory.history.successes());
        assertEquals(replying?n:zero,histories.receiverHistory.replies.successes());
        //p("sent "+history.clientHistory.sent);
        if(tablet!=null) {
            if(oneTablet) assertEquals(one,histories.senderHistory.history.successes());
            else assertEquals(n,histories.senderHistory.history.successes());
        } else assertEquals(n,histories.senderHistory.history.successes());
        // fails when sending in parallel since it does not wait?
        // but how?, since sent is incremented after send is complete
        // looks like received is ok. but sent is a bit late perhaps?
        assertEquals("replying: "+replying,replying?n:zero,histories.senderHistory.replies.successes());
        assertEquals(zero,histories.receiverHistory.history.failures());
        assertEquals(zero,histories.senderHistory.history.failures());
        assertEquals(zero,histories.receiverHistory.missing.failures());
    }
    protected void printStats(String string) {
        p("print stats: "+string+" <<<<<<<");
        for(Tablet tablet:tablets) {
            if(!tablet.equals(tablets.iterator().next())) p("-------");
            p("history for: "+tablet.tabletId()+": "+tablet.histories());
        }
        p("print stats: "+string+" >>>>>>>");
    }
    public static Set<Tablet> createForTest(int n,int offset) {
        Map<String,Required> map=new TreeMap<>();
        // search for linked hash map and use tree map instead.
        for(int i=1;i<=n;i++) {
            Required required=new Required(testingHost,defaultReceivePort+100+offset+i);
            map.put(required.id,required);
        }
        Group group=new Group("1",map,Model.mark1);
        Set<Tablet> tablets=group.createAll();
        return tablets;
    }
    int threads;
    protected boolean printThreads;
    static boolean staticPrintThreads;
    protected boolean printStats;
    protected Set<Tablet> tablets;
    public static Level defaultLevel=Level.WARNING;
    public Integer serviceOffset;
    public static int staticServiceOffset=1_000; // too many places, fix!
}

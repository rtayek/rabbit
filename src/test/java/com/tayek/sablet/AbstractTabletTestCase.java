package com.tayek.sablet;
import static com.tayek.io.IO.*;
import static com.tayek.utilities.Utility.*;
import static org.junit.Assert.*;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.*;
import com.tayek.io.*;
import com.tayek.tablet.*;
import com.tayek.tablet.Message.Type;
import com.tayek.utilities.Et;
public abstract class AbstractTabletTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        LoggingHandler.init();
        // put the ip addresses that we need in here!
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {
        if(staticPrintThreads) printThreads();
    }
    @Before public void setUp() throws Exception {
        LoggingHandler.setLevel(defaultLevel);
        printThreads=false;
        threads=Thread.activeCount();
        serviceOffset+=100;
        IO.l.warning("setup");
    }
    @After public void tearDown() throws Exception { //Thread.sleep(100); // apparently not needed since we shutdown the executor service now.
        boolean anyFailures=false;
        if(tablets!=null) for(Tablet tablet:tablets)
            anyFailures|=tablet.histories().anyFailures();
        if(printStats||anyFailures) {
            //p("printStats or failures!");
            printStats("stats for: "+this+": "+(anyFailures?"failures":""));
        }
        int threads=Thread.activeCount();
        if(threads>this.threads) {
            p((threads-this.threads)+" extra threads!");
            if(printThreads) printThreads();
        }
        IO.l.warning("teardown");
        LoggingHandler.setLevel(defaultLevel);
    }
    protected void startListening() {
        Tablet first=tablets.iterator().next();
        for(Tablet tablet:tablets) {
            Histories histories=tablet.histories();
            SocketAddress socketAddress=tablet.stuff.socketAddress(tablet.tabletId());
            if(!tablet.startListening(socketAddress)) fail(tablet+" startListening() retuns false!");
            assertNotNull(tablet.server);
            assertEquals(first.model.serialNumber,tablet.model.serialNumber);
            assertEquals(first.stuff.serialNumber,tablet.stuff.serialNumber);
        }
    }
    public void sendOneDummyMessageFromEachTablet() {
        for(Tablet tablet:tablets)
            sendOneDummyMessageFromTablet(tablet);
    }
    void sendOneDummyMessageFromTablet(Tablet tablet) {
        tablet.broadcast(tablet.stuff.messages.other(Type.dummy,tablet.groupId,tablet.tabletId()),tablet.stuff);
    }
    public void waitForEachTabletToReceiveAtLeastOneMessageFromEachTablet(boolean sleepAndPrint) throws InterruptedException {
        boolean once=false;
        boolean done=false;
        while(!done) {
            done=true;
            for(Tablet tablet:tablets) {
                Histories histories=tablet.histories();
                // put history into tablet for convenience?
                if(tablet.stuff.replying) {
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
                if(tablet.stuff.replying) {
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
    void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if(!pool.awaitTermination(100,TimeUnit.MILLISECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if(!pool.awaitTermination(100,TimeUnit.MILLISECONDS)) System.err.println("Pool did not terminate");
            }
        } catch(InterruptedException ie) {
            pool.shutdownNow(); // (Re-)Cancel if current thread also interrupted
            Thread.currentThread().interrupt(); // Preserve interrupt status
        }
    }
    protected void shutdown() {
        for(Tablet tablet:tablets) {
            tablet.stopListening();
            if(!tablet.stuff.executorService.isShutdown()) shutdownAndAwaitTermination(tablet.stuff.executorService);
            if(!tablet.stuff.canceller.isShutdown()) tablet.stuff.canceller.shutdown();
            // don't do this if we are testing and all using the same service.
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
            checkHistory(tablet,tablets.size(),false);
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
            checkHistory(tablet,tablets.size(),true);
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
        checkHistory(tablet,tablet.histories(),tablet.stuff.replying,n,oneTablet);
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
    int threads;
    protected boolean printThreads;
    static boolean staticPrintThreads;
    protected boolean printStats;
    protected Set<Tablet> tablets;
    public boolean useExecutorService;
    public boolean runCanceller;
    public boolean waitForSendCallable;
    public static Level defaultLevel=Level.WARNING;
    public static int serviceOffset=1_000; // too many places, fix!
}

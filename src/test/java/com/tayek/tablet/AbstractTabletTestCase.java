package com.tayek.tablet;
import static com.tayek.tablet.io.IO.*;
import static com.tayek.utilities.Utility.*;
import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.io.*;
import com.tayek.utilities.Et;
public abstract class AbstractTabletTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {
        if(staticPrintThreads)
            printThreads();
    }
    @Before public void setUp() throws Exception {
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.WARNING);
        printThreads=false;
        threads=Thread.activeCount();
        serviceOffset+=100;
    }
    @After public void tearDown() throws Exception { //Thread.sleep(100); // apparently not needed since we shutdown the executor service now.
        int threads=Thread.activeCount();
        if(threads>this.threads) {
            p((threads-this.threads)+" extra threads!");
            if(printThreads) {
                printThreads();
                p((threads-this.threads)+" extra threads!");
            }
        }
        LoggingHandler.setLevel(Level.OFF);
    }
    public Set<Tablet> createForTest(int n,int offset) {
        Map<Object,Group.Info> map=new LinkedHashMap<>();
        for(int i=1;i<=n;i++)
            map.put(i,new Group.Info("T"+i+" on PC",Main.testingHost,Main.defaultReceivePort+100+offset+i));
        return new Group(1,map,Model.mark1,false).create();
    }
    protected void startListening() {
        Tablet first=tablets.iterator().next();
        for(Tablet tablet:tablets) {
            if(!tablet.startListening()) fail(tablet+" startListening() retuns false!");
            assertNotNull(tablet.server);
            assertEquals(first.model.serialNumber,tablet.model.serialNumber);
            assertEquals(first.group.serialNumber,tablet.group.serialNumber);
        }
    }
    public void sendOneDummyMessageFromEachTablet() {
        for(Tablet tablet:tablets)
            sendOneDummyMessageFromTablet(tablet);
    }
    void sendOneDummyMessageFromTablet(Tablet tablet) {
        tablet.broadcast(tablet.group.messages.dummy(tablet.group.groupId,tablet.tabletId()),0);
    }
    public void waitForEachTabletToReceiveAtLeastOneMessageFromEachTablet(boolean sleepAndPrint) throws InterruptedException {
        boolean once=false;
        boolean done=false;
        while(!done) {
            done=true;
            for(Tablet tablet:tablets) {
                Histories history=tablet.group.info(tablet.tabletId()).history;
                // put history into tablet for convenience?
                if(tablet.group.replying) {
                    if(history.client.replies.successes()<tablets.size()) {
                        done=false;
                        break;
                    }
                } else {
                    if(history.server.server.successes()<tablets.size()) {
                        done=false;
                        break;
                    }
                    // check for sent also
                    if(history.client.client.successes()<tablets.size()) {
                        done=false;
                        break;
                    }
                }
                // fast fail: check history for any failures and return early.
                // this should avoid timeouts and make the tests run faster.
                if(history.failures()>0) {
                    if(!once) {
                        once=true;
                        p("failures: "+history);
                    }
                    // might cause some tests to fail if we return early
                    // maybe put a guard on this
                    // return;
                }
                if(!sleepAndPrint) Thread.yield();
            }
            if(sleepAndPrint) p("-----------------");
            for(Tablet tablet:tablets) {
                Histories history=tablet.group.info(tablet.tabletId()).history;
                if(sleepAndPrint) {
                    p("history "+history);
                }
            }
            if(sleepAndPrint) Thread.sleep(500);
        }
        if(false) for(Tablet tablet:tablets)
            p("history: "+tablet.group.info(tablet.tabletId()).history);
        for(Tablet tablet:tablets) {
            Histories history=tablet.group.info(tablet.tabletId()).history;
            if(history.server.server.successes()>tablets.size()) tablet.l.warning(tablet+" received too many messages: "+history.server.server.successes());
        }
    }
    void waitForEachTabletToReceiveAtLeastOneMessageFromFirstTablet(boolean sleepAndPrint) throws InterruptedException {
        boolean once=false;
        boolean done=false;
        Tablet first=tablets.iterator().next();
        while(!done) {
            done=true;
            for(Tablet tablet:tablets) {
                Histories history=tablet.group.info(tablet.tabletId()).history;
                // put history into tablet for convenience?
                if(tablet.group.replying) {
                    if(history.client.replies.successes()<1) {
                        done=false;
                        break;
                    }
                } else {
                    if(history.server.server.successes()<1) {
                        done=false;
                        break;
                    }
                    // check for sent also
                    if(tablet.equals(first)) if(history.client.client.successes()<1) {
                        done=false;
                        break;
                    }
                }
                // fast fail: check history for any failures and return early.
                // this should avoid timeouts and make the tests run faster.
                if(history.failures()>0) {
                    if(!once) {
                        once=true;
                        p("failures: "+history);
                    }
                    // might cause some tests to fail if we return early
                    // maybe put a guard on this
                    // return;
                }
                if(!sleepAndPrint) Thread.yield();
            }
            if(sleepAndPrint) p("-----------------");
            for(Tablet tablet:tablets) {
                Histories history=tablet.group.info(tablet.tabletId()).history;
                if(sleepAndPrint) {
                    p("history "+history);
                }
            }
            if(sleepAndPrint) Thread.sleep(500);
        }
        if(false) for(Tablet tablet:tablets)
            p("history: "+tablet.group.info(tablet.tabletId()).history);
        for(Tablet tablet:tablets) {
            Histories history=tablet.group.info(tablet.tabletId()).history;
            if(history.server.server.successes()>tablets.size()) tablet.l.warning(tablet+" received too many messages: "+history.server.server.successes());
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
            if(!tablet.group.executorService.isShutdown()) shutdownAndAwaitTermination(tablet.group.executorService);
            if(!tablet.group.canceller.isShutdown()) tablet.group.canceller.shutdown();
            // don't do this if we are testing and all using the same service.
        }
    }
    protected void sendOneDummyMessageFromEachTabletAndWait(boolean sleepAndPrint) throws InterruptedException {
        sendOneDummyMessageFromEachTablet();
        Et et=new Et();
        waitForEachTabletToReceiveAtLeastOneMessageFromEachTablet(sleepAndPrint);
        //p("wait for "+tablets.size()+" tablets, took: "+et);
        for(Tablet tablet:tablets) {
            Histories history=tablet.group.info(tablet.tabletId()).history;
            if(history.server.server.successes()!=tablets.size()) p(tablet+" received: "+history.server.server.successes()+" instead of "+tablets.size());
            checkHistory(tablet,tablets.size(),false);
        }
    }
    protected void sendOneDummyMessageFromFirstTabletAndWait(boolean sleepAndPrint) throws InterruptedException {
        sendOneDummyMessageFromTablet(tablets.iterator().next());
        Et et=new Et();
        waitForEachTabletToReceiveAtLeastOneMessageFromFirstTablet(sleepAndPrint);
        p("wait for "+tablets.size()+" tablets, took: "+et);
        for(Tablet tablet:tablets) {
            Histories history=tablet.group.info(tablet.tabletId()).history;
            if(history.server.server.successes()!=tablets.size()) p(tablet+" received: "+history.server.server.successes()+" instead of "+tablets.size());
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
        checkHistory(tablet,tablet.group.info(tablet.tabletId()).history,tablet.group.replying,n,oneTablet);
    }
    public void checkHistory(Tablet tablet,Histories history,boolean replying,Integer n,boolean oneTablet) {
        //p("history: "+history);
        assertEquals(oneTablet?one:n,history.server.server.successes());
        assertEquals(replying?n:zero,history.server.replies.successes());
        //p("sent "+history.clientHistory.sent);
        if(tablet!=null) {
            if(oneTablet) assertEquals(one,history.client.client.successes());
            else assertEquals(n,history.client.client.successes());
        } else assertEquals(n,history.client.client.successes());
        // fails when sending in parallel since it does not wait?
        // but how?, since sent is incremented after send is complete
        // looks like received is ok. but sent is a bit late perhaps?
        assertEquals("replying: "+replying,replying?n:zero,history.client.replies.successes());
        assertEquals(zero,history.server.server.failures());
        assertEquals(zero,history.client.client.failures());
        assertEquals(zero,history.server.missing.failures());
    }
    protected void printStats() {
        if(printStats) for(Tablet tablet:tablets)
            p("send time: "+tablet.group.info(tablet.tabletId()).history);
    }
    int threads;
    protected boolean printThreads;
    static boolean staticPrintThreads;
    protected boolean printStats;
    protected Set<Tablet> tablets;
    public static int serviceOffset=1_000; // too many places, fix!
    public static final Integer zero=new Integer(0),one=new Integer(1);
}

package com.tayek.sablet;
import static com.tayek.io.IO.*;
import static com.tayek.utilities.Utility.*;
import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import org.junit.*;
import com.tayek.Tablet;
import com.tayek.io.*;
import com.tayek.io.LogServer.Copier;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.TabletImpl2;
import com.tayek.tablet.io.*;
import com.tayek.utilities.Utility;
public class TabletsWithLogServerTestCase extends AbstractTabletTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        AbstractTabletTestCase.setUpBeforeClass();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {
        AbstractTabletTestCase.tearDownAfterClass();
    }
    @Before public void setUp() throws Exception {
        super.setUp();
        LogManager.getLogManager().reset();
        printThreads();
        logServer=new LogServer(host,++service,getClass().getName());
        thread=new Thread(new Runnable() {
            @Override public void run() {
                logServer.run();
            }
        },"log server");
        thread.start();
    }
    @After public void tearDown() throws Exception {
        p("copiers: "+logServer.copiers);
        logServer.stop();
        printThreads();
        int big=2*Thread.activeCount();
        Thread[] threads=new Thread[big];
        Thread.enumerate(threads);
        for(Thread thread:threads)
            if(thread!=null&&thread.getName().contains("SocketHandlerCallable")) {
                p(thread.toString()+" --------");
                p("interrupted: "+thread.isInterrupted()+", alive: "+thread.isAlive());
            }
        super.tearDown();
    }
    @Test(timeout=5_000) public void test() throws InterruptedException {
        LoggingHandler.once=false;
        LoggingHandler.init();
        LoggingHandler.toggleSockethandlers();
        p("hanlders; "+Arrays.asList(IO.l.getHandlers()));
        // start tablets
        tablets=createForTest(2,serviceOffset);
        startListening();
        sendOneDummyMessageFromEachTabletAndWaitAndShutdown(false);
        for(Tablet tablet:tablets)
            if(tablet instanceof TabletImpl2) {
                TabletImpl2 t2=(TabletImpl2)tablet;
                checkHistory(t2,tablets.size(),false);
            } else {
                p("how do i check history?");
            }
        Thread.sleep(200);
        shutdown();
        // stop socket logging
        for(Iterator<Copier> i=logServer.copiers.iterator();i.hasNext();) {
            Copier copier=i.next();
            StringBuffer stringBuffer=new StringBuffer();
            Utility.fromFile(stringBuffer,copier.file);
            p("file: "+copier.file);
            //p("contents: "+stringBuffer.toString());
            //assertTrue(stringBuffer.toString().contains(expected));
        }
        LoggingHandler.toggleSockethandlers();
    }
    String host="127.0.0.1";
    LogServer logServer;
    Thread thread;
    final String expected="i am a duck.";
    static int service=LogServer.defaultService+1000;
}

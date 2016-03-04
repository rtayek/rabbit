package failing;
import static com.tayek.tablet.io.IO.p;
import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import org.junit.*;
import com.tayek.tablet.*;
import com.tayek.tablet.io.*;
import com.tayek.tablet.io.LogServer.Copier;
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
        //printThreads();
        LoggingHandler.socketHandler=null; // static, was causing tests to fail!
        logServer=new LogServer(host,++service,getClass().getName());
        thread=new Thread(new Runnable() {
            @Override public void run() {
                logServer.run();
            }
        },"log server");
        thread.start();
        LoggingHandler.setLevel(Level.INFO);
        tablets=createForTest(2,serviceOffset);
    }
    @After public void tearDown() throws Exception {
        shutdown();
        LoggingHandler.setLevel(Level.OFF);
        p("copiers: "+logServer.copiers);
        logServer.stop();
        //printThreads();
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
        // start socket logging
        LoggingHandler.startSocketHandler(host,service);
        LoggingHandler.once=false;
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.ALL);
        LoggingHandler.addSocketHandler(LoggingHandler.socketHandler);
        p("hanlders; "+Arrays.asList(IO.staticLogger.getHandlers()));
        // start tablets
        tablets=createForTest(2,serviceOffset);
        startListening();
        sendOneDummyMessageFromEachTabletAndWaitAndShutdown(false);
        for(Tablet tablet:tablets)
            checkHistory(tablet,tablets.size(),false);
        printStats();
        Thread.sleep(200);
        // stop socket logging
        LoggingHandler.setLevel(Level.OFF);
        LoggingHandler.stopSocketHandler();
        for(Iterator<Copier> i=logServer.copiers.iterator();i.hasNext();) {
            Copier copier=i.next();
            StringBuffer stringBuffer=new StringBuffer();
            Utility.fromFile(stringBuffer,copier.file);
            p("file: "+copier.file);
            //p("contents: "+stringBuffer.toString());
            //assertTrue(stringBuffer.toString().contains(expected));
        }
    }
    String host="127.0.0.1";
    LogServer logServer;
    Thread thread;
    final String expected="i am a duck.";
    static int service=LogServer.defaultService+1000;
}

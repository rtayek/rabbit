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
public class TabletsWithRealLogServerTestCase extends AbstractTabletTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        AbstractTabletTestCase.setUpBeforeClass();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {
        AbstractTabletTestCase.tearDownAfterClass();
    }
    @Before public void setUp() throws Exception {
        super.setUp();
        LogManager.getLogManager().reset();
        executorService=Executors.newSingleThreadExecutor();
        //printThreads();
        LoggingHandler.socketHandler=null; // static, was causing tests to fail!
        LoggingHandler.setLevel(Level.INFO);
        tablets=createForTest(2,serviceOffset);
    }
    @After public void tearDown() throws Exception {
        shutdown();
        LoggingHandler.setLevel(Level.OFF);
        executorService.shutdown();
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
    @Test public void test() throws InterruptedException {
        // start socket logging
        LoggingHandler.startSocketHandler(Main.defaultLogServerHost,LogServer.defaultService);
        // use real port for a while
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
        StringBuffer stringBuffer=new StringBuffer();
        // need to get file contents!
        //Utility.fromFile(stringBuffer,copier.file);
        // hard to do, just verify that the log file got written for now
    }
    private ExecutorService executorService;
    final String expected="i am a duck.";
}

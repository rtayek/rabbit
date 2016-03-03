package com.tayek.tablet.io;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import org.junit.*;
import com.tayek.tablet.Main;
import com.tayek.tablet.io.LogServer.Copier;
import com.tayek.tablet.io.LoggingHandler.MyFormatter;
import com.tayek.utilities.Utility;
import static com.tayek.tablet.io.IO.*;
public class LogServerWriteFileTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LogManager.getLogManager().reset();
        executorService=Executors.newSingleThreadExecutor();
        //printThreads();
        LoggingHandler.socketHandler=null; // static, was causing tests to fail!
        logServer=new LogServer(++service,getClass().getName());
        thread=new Thread(new Runnable() {
            @Override public void run() {
                logServer.run();
            }
        },"log server");
        thread.start();
    }
    @After public void tearDown() throws Exception {
        executorService.shutdown();
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
    }
   /* @Test*/ public void testFileHandler() throws SecurityException,IOException {
        String string=System.getProperty("java.util.logging.config.file");
        p("java.util.logging.config.file: "+string);
        /*FileHandler fileHandler=*/new FileHandler("%hjvm%g.log",1000,4,true);
        // work on this one?
    }
    private void test() throws IOException,InterruptedException {
        p("------------------------------------------");
        LoggingHandler.startSocketHandler(Main.defaultLogServerHost,service);
        printThreads();
        Logger logger=Logger.getLogger("foo");
        logger.setUseParentHandlers(false);
        Handler handler=new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(MyFormatter.instance);
        logger.addHandler(handler);
        if(LoggingHandler.socketHandler!=null) {
            logger.addHandler(LoggingHandler.socketHandler);
            LoggingHandler.socketHandler.setLevel(Level.ALL);
        } else p("socket handler is null!");
        p("hanlders; "+Arrays.asList(logger.getHandlers()));
        logger.setLevel(Level.ALL);
        logger.finest(expected);
        //Thread.sleep(100); // this makes it work!
        logger.setLevel(Level.OFF);
        LoggingHandler.stopSocketHandler();
        Copier copier=logServer.copiers.iterator().next();
        StringBuffer stringBuffer=new StringBuffer();
        Utility.fromFile(stringBuffer,copier.file);
        assertTrue(stringBuffer.toString().contains(expected));
    }
    @Test public void test2() throws IOException,InterruptedException {
        LogManager.getLogManager().reset();
        LoggingHandler.startSocketHandler(Main.defaultLogServerHost,service);
        LoggingHandler.once=false;
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.ALL);
        LoggingHandler.addSocketHandler(LoggingHandler.socketHandler);
        IO.staticLogger.finest(expected);
        LoggingHandler.setLevel(Level.OFF);
        IO.staticLogger.finest(expected);
        // should check to see if the second is not logged
        // otherwise, how is this test different from the other?
        Thread.sleep(100); // this makes it work!
        LoggingHandler.stopSocketHandler();
        Iterator<Copier> i=logServer.copiers.iterator();
        if(i.hasNext()) {
            Copier copier=i.next();
            StringBuffer stringBuffer=new StringBuffer();
            Utility.fromFile(stringBuffer,copier.file);
            if(!stringBuffer.toString().contains(expected))
                ;//p("fail: "+stringBuffer.toString());
            assertTrue(stringBuffer.toString().contains(expected));
        }
        else p("3log server has : "+logServer.copiers.size()+" copiers.");
    }
    LogServer logServer;
    Thread thread;
    private ExecutorService executorService;
    final String expected="i am a duck.";
    static int service=LogServer.defaultService;
}

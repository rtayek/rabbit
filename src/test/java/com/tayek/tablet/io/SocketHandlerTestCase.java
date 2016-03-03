package com.tayek.tablet.io;
import static com.tayek.tablet.io.IO.*;
import static org.junit.Assert.*;
import java.io.*;
import java.util.Arrays;
import java.util.logging.*;
import org.junit.*;
import com.tayek.tablet.Main;
import com.tayek.tablet.io.LogServer.Copier;
import com.tayek.tablet.io.LoggingHandler.MyFormatter;
public class SocketHandlerTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LoggingHandler.setLevel(Level.OFF);
        LogManager.getLogManager().reset();
        LoggingHandler.socketHandler=null; // static, was causing tests to fail!
        writer=new StringWriter();
        Copier.Factory factory=new Copier.Factory(writer);
        logServer=new LogServer(++service,factory,getClass().getName());
        thread=new Thread(new Runnable() {
            @Override public void run() {
                logServer.run();
            }
        },"log server");
        thread.start();
    }
    @After public void tearDown() throws Exception {
        //executorService.shutdown();
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
    }
    void test() throws IOException,InterruptedException {
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
        Thread.sleep(100); // this makes it work!
        logger.setLevel(Level.OFF);
        LoggingHandler.stopSocketHandler();
        writer.flush();
        //p("writer: "+writer.toString());
        if(!writer.toString().contains(expected)) p("will fail!");
        assertTrue(writer.toString().contains(expected));
    }
    @Test public void test0() throws IOException,InterruptedException {
        test();
    }
    @Test public void test1() throws IOException,InterruptedException {
        test();
    }
    LogServer logServer;
    Writer writer;
    Thread thread;
    final String expected="i am a duck.";
    static int service=LogServer.defaultService;
}

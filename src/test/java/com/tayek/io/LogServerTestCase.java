package com.tayek.io;
import static com.tayek.io.IO.p;
import static org.junit.Assert.*;
import java.io.*;
import java.util.logging.*;
import org.junit.*;
import com.tayek.io.LogServer.Copier;
import com.tayek.utilities.Utility;
public class LogServerTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void test() throws InterruptedException,IOException {
        LogManager.getLogManager().reset();
        LoggingHandler.once=false;
        LoggingHandler.init();
        logServer=new LogServer("localhost",++service,getClass().getSimpleName());
        thread=new Thread(new Runnable() {
            @Override public void run() {
                logServer.run();
            }
        },"log server");
        thread.start();
        socketHandler=LoggingHandler.startSocketHandler("localhost",service);
        LoggingHandler.setLevel(Level.ALL);
        LoggingHandler.addSocketHandler(socketHandler);
        IO.l.info(expected);
        IO.l.info("foo");
        Thread.sleep(200); // need to wait a bit
        Copier copier=logServer.copiers.iterator().next();
        copier.close();
        StringBuffer stringBuffer=new StringBuffer();
        Utility.fromFile(stringBuffer,copier.file);
        p("contents of file: "+copier.file+": "+stringBuffer.toString());
        assertTrue(stringBuffer.toString().contains(expected));
        LoggingHandler.stopSocketHandler(socketHandler);
        logServer.stop();
    }
    @Test public void testRestartTablet() throws InterruptedException,IOException {
        LogManager.getLogManager().reset();
        LoggingHandler.once=false;
        LoggingHandler.init();
        logServer=new LogServer("localhost",++service,getClass().getSimpleName());
        thread=new Thread(new Runnable() {
            @Override public void run() {
                logServer.run();
            }
        },"log server");
        thread.start();
        socketHandler=LoggingHandler.startSocketHandler("localhost",service);
        LoggingHandler.setLevel(Level.ALL);
        LoggingHandler.addSocketHandler(socketHandler);
        IO.l.info(expected);
        IO.l.info("foo");
        Thread.sleep(200); // need to wait a bit
        if(true) {
            Copier copier=logServer.copiers.iterator().next();
            copier.flush();
        } else {
            Copier copier=logServer.copiers.iterator().next();
            copier.close();
            StringBuffer stringBuffer=new StringBuffer();
            Utility.fromFile(stringBuffer,copier.file);
            p("contents of file: "+copier.file+": "+stringBuffer.toString());
            assertTrue(stringBuffer.toString().contains(expected));
            // how to kill socket handler?
            LoggingHandler.stopSocketHandler(socketHandler);
            logServer.stop();
        }
        Thread.sleep(1_000); // at least 1 second!
        LogManager.getLogManager().reset();
        LoggingHandler.once=false;
        LoggingHandler.init();
        socketHandler=LoggingHandler.startSocketHandler("localhost",service);
        LoggingHandler.setLevel(Level.ALL);
        LoggingHandler.addSocketHandler(socketHandler);
        IO.l.info(expected);
        IO.l.info("bar");
        Thread.sleep(200); // need to wait a bit
        logServer.stop();
    }
    LogServer logServer;
    SocketHandler socketHandler;
    Thread thread;
    Writer writer;
    final String expected="i am a duck.";
    static int service=LogServer.defaultService;
}

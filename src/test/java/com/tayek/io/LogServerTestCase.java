package com.tayek.io;
import static com.tayek.io.IO.*;
import static org.junit.Assert.*;
import java.io.*;
import java.net.Socket;
import java.util.logging.*;
import org.junit.*;
import org.junit.rules.TestRule;
import com.tayek.MyTestWatcher;
import com.tayek.io.LogServer.Copier;
import com.tayek.utilities.Utility;
public class LogServerTestCase {
    @Rule public TestRule watcher=new MyTestWatcher();
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LogManager.getLogManager().reset();
        LoggingHandler.once=false;
        LoggingHandler.init();
        service=staticService++;
    }
    @After public void tearDown() throws Exception {}
    @Test public void testConnectAndWrite() throws Exception,IOException {
        writer=new StringWriter();
        LogServer.Factory factory=new LogServer.Factory(writer);
        logServer=new LogServer(host,service,factory,getClass().getName());
        logServer.verbose=true;
        thread=new Thread(new Runnable() {
            @Override public void run() {
                logServer.run();
            }
        },"log server");
        thread.start();
        Socket socket=new Socket(host,service);
        BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        bufferedWriter.write(expected);
        bufferedWriter.flush();
        bufferedWriter.close();
        Thread.sleep(100);
        logServer.stop();
        assertTrue(writer.toString().contains(expected));
    }
    @Test public void testConnectAndWriteFile() throws Exception,IOException {
        logServer=new LogServer(host,service,getClass().getSimpleName());
        logServer.verbose=true;
        thread=new Thread(new Runnable() {
            @Override public void run() {
                logServer.run();
            }
        },"log server");
        thread.start();
        Socket socket=new Socket(host,service);
        BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        bufferedWriter.write(expected);
        bufferedWriter.flush();
        bufferedWriter.close();
        socket.close();
        Thread.sleep(100);
        Copier copier=logServer.copiers.iterator().next();
        copier.close();
        logServer.stop();
        StringBuffer stringBuffer=new StringBuffer();
        Utility.fromFile(stringBuffer,copier.file);
        p("contents of file: "+copier.file+": '"+stringBuffer.toString()+"'");
        assertTrue(stringBuffer.toString().contains(expected));
    }
    @Test public void testSockethandler() throws Exception {
        writer=new StringWriter();
        LogServer.Factory factory=new LogServer.Factory(writer);
        logServer=new LogServer(host,service,factory,getClass().getName());
        logServer.verbose=true;
        thread=new Thread(new Runnable() {
            @Override public void run() {
                logServer.run();
            }
        },"log server");
        thread.start();
        socketHandler=LoggingHandler.createSocketHandlerAndWait(host,service);
        p("socket handler: "+socketHandler);
        LoggingHandler.setLevel(Level.ALL);
        LoggingHandler.addSocketHandler(socketHandler);
        IO.l.severe(expected);
        Thread.sleep(100); // need to wait a bit
        logServer.stop();
        assertTrue(writer.toString().contains(expected));
        LoggingHandler.stopSocketHandler(socketHandler);
    }
    @Test public void testSockethandlerAndWriteFiles() throws Exception {
        logServer=new LogServer(host,service,getClass().getSimpleName());
        logServer.verbose=true;
        thread=new Thread(new Runnable() {
            @Override public void run() {
                logServer.run();
            }
        },"log server");
        thread.start();
        socketHandler=LoggingHandler.createSocketHandlerAndWait(host,service);
        LoggingHandler.setLevel(Level.ALL);
        LoggingHandler.addSocketHandler(socketHandler);
        IO.l.severe(expected);
        Thread.sleep(100); // need to wait a bit
        Copier copier=logServer.copiers.iterator().next();
        copier.isShuttingdown=true;
        copier.close();
        StringBuffer stringBuffer=new StringBuffer();
        Utility.fromFile(stringBuffer,copier.file);
        p("contents of file: "+copier.file+": '"+stringBuffer.toString()+"'");
        assertTrue(stringBuffer.toString().contains(expected));
        LoggingHandler.stopSocketHandler(socketHandler);
        logServer.stop();
    }
    @Test public void testRestartTablet() throws InterruptedException,IOException {
        // only passes because we don't test anything
        // just tests the restart capability!
        logServer=new LogServer(host,service,getClass().getSimpleName());
        logServer.verbose=true;
        thread=new Thread(new Runnable() {
            @Override public void run() {
                logServer.run();
            }
        },"log server");
        thread.start();
        socketHandler=LoggingHandler.createSocketHandlerAndWait(host,service);
        LoggingHandler.setLevel(Level.ALL);
        LoggingHandler.addSocketHandler(socketHandler);
        IO.l.info(expected);
        IO.l.info("foo");
        Thread.sleep(100); // need to wait a bit
        if(true) {
            Copier copier=logServer.copiers.iterator().next();
            copier.isShuttingdown=true;
            copier.flush();
        } else {
            Copier copier=logServer.copiers.iterator().next();
            copier.close();
            StringBuffer stringBuffer=new StringBuffer();
            Utility.fromFile(stringBuffer,copier.file);
            p("contents of file: "+copier.file+": '"+stringBuffer.toString()+"'");
            assertTrue(stringBuffer.toString().contains(expected));
            // how to kill socket handler?
            LoggingHandler.stopSocketHandler(socketHandler);
            logServer.stop();
        }
        Thread.sleep(1_000); // at least 1 second!
        LogManager.getLogManager().reset();
        LoggingHandler.once=false;
        LoggingHandler.init();
        socketHandler=LoggingHandler.createSocketHandlerAndWait(host,service);
        p("socket handler: "+socketHandler);
        LoggingHandler.setLevel(Level.ALL);
        LoggingHandler.addSocketHandler(socketHandler);
        IO.l.info(expected);
        IO.l.info("bar");
        Thread.sleep(100); // need to wait a bit
        logServer.stop();
    }
    String host="localhost";
    Integer service;
    LogServer logServer;
    SocketHandler socketHandler;
    Thread thread;
    Writer writer;
    final String expected="i am a duck.";
    static int staticService=defaultLogServerService+1000;
}

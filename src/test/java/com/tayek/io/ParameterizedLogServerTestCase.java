package com.tayek.io;
import static org.junit.Assert.assertTrue;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static com.tayek.io.IO.*;
import com.tayek.MyTestWatcher;
import com.tayek.io.LogServer.Copier;
import com.tayek.utilities.Utility;
@RunWith(Parameterized.class) public class ParameterizedLogServerTestCase {
    @Rule public TestRule watcher=new MyTestWatcher();

    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LogManager.getLogManager().reset();
        LoggingHandler.once=false;
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.ALL);
        service=staticService++;
        if(useWriter) {
            writer=new StringWriter();
            LogServer.Factory factory=new LogServer.Factory(writer);
            logServer=new LogServer(host,service,factory,getClass().getName());
        } else {
            logServer=new LogServer(host,service,getClass().getName());
        }
        thread=new Thread(new Runnable() {
            @Override public void run() {
                logServer.run();
            }
        },"log server");
        thread.start();
        socketHandler=LoggingHandler.startSocketHandler(host,service);
        LoggingHandler.setLevel(Level.ALL);
        LoggingHandler.addSocketHandler(socketHandler);
    }
    @After public void tearDown() throws Exception {
        LoggingHandler.stopSocketHandler(socketHandler);
        logServer.stop();
    }
    public ParameterizedLogServerTestCase(String host,boolean useWriter) {
        this.host=host;
        this.useWriter=useWriter;
    }
    @Parameters public static Collection<Object[]> data() throws UnknownHostException,InterruptedException,ExecutionException {
        Set<String> hosts=new TreeSet();
        hosts.add("127.0.0.1");
        hosts.add("localhost");
        hosts.add(defaultHost);
        hosts.add(testingHost);
        List<Object[]> parameters=new ArrayList<Object[]>();
        for(String string:hosts) {
            parameters.add(new Object[] {string,true});
            parameters.add(new Object[] {string,false});
        }
        return parameters;
    }
    @Test public void test() throws IOException,InterruptedException {
        p("host: "+host);
        IO.l.info(expected);
        for(int i=0;i<10;i++)
            IO.l.severe(expected);
        Thread.sleep(900); // need to wait a bit
        if(useWriter) {
            writer.flush();
            if(!writer.toString().contains(expected)) p("will fail!");
            assertTrue(writer.toString().contains(expected));
        } else {
            Thread.sleep(900); // need to wait a bit
            Copier copier=logServer.copiers.iterator().next();
            copier.close();
            StringBuffer stringBuffer=new StringBuffer();
            Utility.fromFile(stringBuffer,copier.file);
            assertTrue(stringBuffer.toString().contains(expected));
        }
        l.severe(logServer.copiers+" copiers.");
    }
    final String host;
    final boolean useWriter;
    LogServer logServer;
    SocketHandler socketHandler;
    Thread thread;
    Writer writer;
    Integer service;
    final String expected="i am a duck.";
    static int staticService=LogServer.defaultService+3000;
}

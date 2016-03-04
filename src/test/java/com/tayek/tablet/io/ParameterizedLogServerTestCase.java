package com.tayek.tablet.io;
import static com.tayek.tablet.io.IO.p;
import static org.junit.Assert.assertTrue;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.tayek.tablet.Main;
import com.tayek.tablet.io.IO.GetNetworkInterfacesCallable;
import com.tayek.tablet.io.LogServer.Copier;
import com.tayek.utilities.Utility;
@RunWith(Parameterized.class) public class ParameterizedLogServerTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LogManager.getLogManager().reset();
        LoggingHandler.once=false;
        LoggingHandler.init();
        LoggingHandler.socketHandler=null; // static, was causing tests to fail!
        if(useWriter) {
            writer=new StringWriter();
            LogServer.Factory factory=new LogServer.Factory(writer);
            logServer=new LogServer(host,++service,factory,getClass().getName());
        } else {
            logServer=new LogServer(host,++service,getClass().getName());
        }
        thread=new Thread(new Runnable() {
            @Override public void run() {
                logServer.run();
            }
        },"log server");
        thread.start();
        LoggingHandler.startSocketHandler(host,service);
        LoggingHandler.setLevel(Level.ALL);
        LoggingHandler.addSocketHandler(LoggingHandler.socketHandler);
    }
    @After public void tearDown() throws Exception {
        LoggingHandler.stopSocketHandler();
        logServer.stop();
    }
    public ParameterizedLogServerTestCase(String host,boolean useWriter) {
        this.host=host;
        this.useWriter=useWriter;
    }
    @Parameters public static Collection<Object[]> data() throws UnknownHostException,InterruptedException,ExecutionException {
        List<String> list=new ArrayList<>();
        list.add("127.0.0.1");
        list.add("localhost");
        list.add(Main.networkHost);
        list.add(Main.testingHost);
        List<Object[]> parameters=new ArrayList<Object[]>();
        for(String string:list) {
            parameters.add(new Object[] {string,true});
            parameters.add(new Object[] {string,false});
        }
        return parameters;
    }
    @Test public void test() throws IOException,InterruptedException {
        p("host: "+host);
        IO.staticLogger.info(expected);
        Thread.sleep(200); // need to wait a bit
        if(useWriter) {
            writer.flush();
            p("writer: "+writer.toString());
            if(!writer.toString().contains(expected)) p("will fail!");
            assertTrue(writer.toString().contains(expected));
        } else {
            Copier copier=logServer.copiers.iterator().next();
            copier.close();
            StringBuffer stringBuffer=new StringBuffer();
            Utility.fromFile(stringBuffer,copier.file);
            p("contents of file: "+copier.file+": "+stringBuffer.toString());
            assertTrue(stringBuffer.toString().contains(expected));
        }
    }
    final String host;
    final boolean useWriter;
    LogServer logServer;
    Thread thread;
    Writer writer;
    final String expected="i am a duck.";
    static int service=LogServer.defaultService;
}

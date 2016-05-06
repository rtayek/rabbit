package com.tayek.io;
import static org.junit.Assert.*;
import java.util.Map;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.Required;
import com.tayek.tablet.Group;
import com.tayek.tablet.Group.Groups;
import com.tayek.tablet.MessageReceiver.Model;
import static com.tayek.io.IO.*;
import com.tayek.tablet.io.*;
public class RunnerTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.WARNING);
        runner=new RunnerABC(group,raysRouter,raysRouterPrefix);
        runner.loopSleep=3_000;
        new Thread(runner,"tablet runner").start();
    }
    @After public void tearDown() throws Exception {
        runner.thread.interrupt();
    }
    // add a test to delete the properties file!
    @Test public void test() throws InterruptedException {
        Thread.sleep(12_000);
        p("n: "+runner.n);
        p("restarts: "+runner.restarts);
        if(runner.tablet!=null) p("histories: "+runner.tablet.histories());
    }
    Map<String,Required> requireds=new Groups().groups.get("g2OnRouter");
    Group group=new Group("1",requireds,Model.mark1);
    RunnerABC runner;
}

package com.tayek.io;
import static org.junit.Assert.*;
import java.util.Map;
import java.util.logging.Level;
import org.junit.*;
import org.junit.rules.TestRule;
import com.tayek.*;
import com.tayek.io.Audio.AudioObserver;
import com.tayek.tablet.Group;
import com.tayek.tablet.Group.Groups;
import com.tayek.tablet.MessageReceiver.Model;
import static com.tayek.io.IO.*;
import com.tayek.tablet.io.*;
public class RunnerABCTestCase {
    @Rule public TestRule watcher=new MyTestWatcher();

    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.WARNING);
        Prefs.factory.create().clear();
    }
    @After public void tearDown() throws Exception {
        if(runner!=null&&runner.thread!=null) runner.thread.interrupt();
        Prefs.factory.create().clear();
    }
    // add a test to delete the properties file!
    @Test public void testThatModelsAreEqual() throws InterruptedException {
        runner=new RunnerABC(group,raysRouter,raysRouterPrefix);
        runner.loopSleep=100;
        new Thread(runner,"tablet runner").start();
        Thread.sleep(400);
        assertNotNull(runner.tablet);
        assertNotNull(runner.tablet.model().equals(runner.model));
    }
    protected Map<String,Required> requireds=new Groups().groups.get("g2OnPc");
    protected Group group=new Group("1",requireds,Model.mark1);
    protected RunnerABC runner;
}

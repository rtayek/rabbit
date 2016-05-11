package com.tayek.io;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TestRule;
import com.tayek.MyTestWatcher;
import static com.tayek.io.IO.*;
public class ExecTestCase {
    @Rule public TestRule watcher=new MyTestWatcher();
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void testPing127_0_0_1() throws InterruptedException {
        assertTrue(Exec.canWePing("127.0.0.1",5_000));
    }
    @Test public void testNetwokInterface() throws InterruptedException {
        if(isAndroid()) assertTrue(Exec.canWePing(tabletRouter,5_000));
        else assertTrue(Exec.canWePing(raysRouter,5_000));
    }
    @Test public void testPingNotAHostName() throws InterruptedException {
        assertFalse(Exec.canWePing("notAHostName",5_000));
    }
}

package com.tayek.speed;
import static com.tayek.io.IO.*;
import static com.tayek.speed.Server.factory;
import static com.tayek.utilities.Utility.connect;
import static org.junit.Assert.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.Histories;
import com.tayek.io.LoggingHandler;
import com.tayek.utilities.Et;
public class ServerTestCase extends AbstractServerTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        super.setUp();
    }
    @After public void tearDown() throws Exception {
        super.tearDown();
    }
    @Test public void testCreateAndStopServers1() throws InterruptedException {
        create(1);
        stopServers();
        Thread.sleep(100);
        assertTrue(Thread.activeCount()<=threads);
    }
    @Test public void testCreateStartServersAndStopServers1() throws InterruptedException {
        createStartAndStop(1);
    }
    @Test public void testCreateStartServersAndStopServers2() throws InterruptedException {
        createStartAndStop(2);
    }
    @Test public void testCreateStartServersAndStopServers3() throws InterruptedException {
        createStartAndStop(3);
    }
    @Test public void testCreateStartServersAndStopServers10() throws InterruptedException {
        createStartAndStop(10);
    }
    @Test public void test1_1() throws InterruptedException {
        run(1,1);
        assertTrue(Thread.activeCount()<=threads);
    }
    @Test public void test2_1() throws InterruptedException {
        run(2,1);
        assertTrue(Thread.activeCount()<=threads);
    }
    @Test public void test10_1() throws InterruptedException {
        run(10,1);
        assertTrue(Thread.activeCount()<=threads);
    }
    @Test public void test1_10() throws InterruptedException {
        run(1,10);
        assertTrue(Thread.activeCount()<=threads);
    }
    @Test public void test2_10() throws InterruptedException {
        run(2,10);
        assertTrue(Thread.activeCount()<=threads);
    }
    @Test public void test10_10() throws InterruptedException {
        run(10,10);
        assertTrue(Thread.activeCount()<=threads);
    }
}

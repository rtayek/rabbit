package com.tayek.tablet.io;
import static org.junit.Assert.*;
import java.util.logging.*;
import org.junit.*;
import com.tayek.tablet.Main;
import com.tayek.tablet.io.*;
public class RealLogServerTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LogManager.getLogManager().reset();
        LoggingHandler.once=false;
        LoggingHandler.init();
        LoggingHandler.toggleSockethandlers();
        LoggingHandler.setLevel(Level.WARNING);
    }
    @After public void tearDown() throws Exception {
        LoggingHandler.toggleSockethandlers();
    }
    @Test public void test() { // just testing that we can log.
        IO.staticLogger.info("sample log message");
    }
    SocketHandler socketHandler;
}

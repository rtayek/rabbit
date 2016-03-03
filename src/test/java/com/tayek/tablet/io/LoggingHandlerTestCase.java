package com.tayek.tablet.io;
import java.util.Enumeration;
import java.util.logging.*;
import org.junit.*;
import static com.tayek.tablet.io.IO.*;
public class LoggingHandlerTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void names() {
        Enumeration<String> x=LogManager.getLogManager().getLoggerNames();
        for(;x.hasMoreElements();)
            p(x.nextElement());
    }
    @Test public void test() {
        Logger logger=Logger.getLogger("foo");
        p("initial log level: "+logger.getLevel());
        logger.finest("bar");
        logger.finer("bar");
        logger.fine("bar");
        logger.config("bar");
        logger.info("bar");
        logger.warning("bar");
        logger.severe("bar");
    }
}

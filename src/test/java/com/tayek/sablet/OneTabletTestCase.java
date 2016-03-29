package com.tayek.sablet;
import static com.tayek.io.IO.*;
import static org.junit.Assert.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.tablet.*;
import com.tayek.tablet.io.LoggingHandler;
public class OneTabletTestCase extends AbstractTabletTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        AbstractTabletTestCase.setUpBeforeClass();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {
        AbstractTabletTestCase.tearDownAfterClass();
    }
    @Before public void setUp() throws Exception {
        super.setUp();
        printThreads=true;
    }
    @After public void tearDown() throws Exception {
        super.tearDown();
    }
    @Test public void testSetUpAndTearDown() {}
    @Test public void startupAndShutdown() {
        tablets=Tablet.createForTest(1,serviceOffset);
        startListening();
        shutdown();
    }
    @Test(timeout=200) public void testSendOneMessage() throws InterruptedException {
        tablets=Tablet.createForTest(1,serviceOffset);
        startListening();
        sendOneDummyMessageFromEachTabletAndWait(false);
        shutdown();
    }
}

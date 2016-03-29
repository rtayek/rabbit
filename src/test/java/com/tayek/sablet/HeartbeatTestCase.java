package com.tayek.sablet;
import static org.junit.Assert.*;
import org.junit.*;
import com.tayek.tablet.*;
public class HeartbeatTestCase extends AbstractTabletTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        AbstractTabletTestCase.setUpBeforeClass();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {
        AbstractTabletTestCase.tearDownAfterClass();
    }
    @Before public void setUp() throws Exception {
        super.setUp();
    }
    @After public void tearDown() throws Exception {
        super.tearDown();
    }
    @Test public void test() {
        tablets=Tablet.createForTest(2,serviceOffset);
        startListening();
        // how do we test?
        // need to keep some history
        // add heartbeats to history class?
        shutdown();
    }
}

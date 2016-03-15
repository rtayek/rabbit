package com.tayek.tablet;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.tayek.tablet.*;
@RunWith(Suite.class) @Suite.SuiteClasses({TabletTestCase.class,TwoTabletsTestCase.class,HeartbeatTestCase.class,}) public class TabletTestSuite {
    @BeforeClass public static void setUpBeforeClass() {
    }
    @AfterClass public static void tearDownAfterClass() {
    }
    @Before public void setUp() throws Exception {
        /* does not get called */
    }
    @After public void tearDown() throws Exception {
        /* does not get called */
    }
}

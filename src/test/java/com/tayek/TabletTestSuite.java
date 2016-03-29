package com.tayek;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.tayek.failures.TabletTestCase;
import com.tayek.sablet.*;
import static com.tayek.io.IO.*;
@RunWith(Suite.class) @Suite.SuiteClasses({TabletTestCase.class,TwoTabletsTestCase.class,HeartbeatTestCase.class,})
public class TabletTestSuite {
    @BeforeClass public static void setUpBeforeClass() {
        p("static suite setup");
    }
    @AfterClass public static void tearDownAfterClass() {
        p("static suite teardown");
    }
    @Before public void setUp() throws Exception {
        p("suite setup");
        /* does not get called */
    }
    @After public void tearDown() throws Exception {
        p("suite teardown");
        /* does not get called */
    }
}

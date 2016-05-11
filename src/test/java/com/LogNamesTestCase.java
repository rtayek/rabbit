package com;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.Description;
import com.tayek.MyTestWatcher;
import static com.tayek.io.IO.*;
public class LogNamesTestCase {
    @Rule public TestRule watcher=new MyTestWatcher();
    @Rule public TestName name=new TestName();
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void test123() {
        p("name="+name.getMethodName());
    }
}

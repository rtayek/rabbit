package com.tayek;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TestRule;
import com.tayek.utilities.Colors;
import static com.tayek.io.IO.*;
public class ColorsTestCase {
    @Rule public TestRule watcher=new MyTestWatcher();

    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void testFailureRate() {
        for(double rate=0;rate<=1;rate+=.05)
            p("rate: "+rate+", gets color: "+Colors.toString(Colors.color(rate)));
    }
    @Test public void testFailureRate2() {
        p("yellow is: "+Colors.toString(Colors.yellow));
        for(double rate=0;rate<=1;rate+=.05)
            p("rate: "+rate+", gets color2: "+Colors.toString(Colors.color2(rate)));
    }
    @Test public void testToString() {
        p(Colors.toString(Colors.aColor(Colors.red)));
        p(Colors.toString(Colors.aColor(Colors.orange)));
        p(Colors.toString(Colors.aColor(Colors.yellow)));
        p(Colors.toString(Colors.aColor(Colors.green)));
        p(Colors.toString(Colors.aColor(Colors.blue)));
    }
}

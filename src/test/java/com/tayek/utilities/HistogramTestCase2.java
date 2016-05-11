package com.tayek.utilities;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TestRule;
import com.tayek.MyTestWatcher;
public class HistogramTestCase2 {
    @Rule public TestRule watcher=new MyTestWatcher();

    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void test() {
        time.add(.1);
        failureRate.add(0);
        time.add(Double.NaN);
        failureRate.add(1);
        time.add(Double.NaN);
        failureRate.add(1);
        time.add(.2);
        failureRate.add(.5);
        System.out.println("time: "+time);
        System.out.println("failureRate: "+failureRate);
        // makes perfect sense
        // the nan's are not counted in the time histogram.
        // but they are added as 1's (a 100% failure rate).
        // and they show up as missings, since the range is [0,1) and not [0,1].
        // and they are counted.
    }
    Histogram time=new Histogram();
    Histogram failureRate=new Histogram();
}

package com.tayek.utilities;
import static org.junit.Assert.*;
import org.junit.*;
public class HistogramTestCase3 {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void testHistogram() {}
    @Test public void testHistogramIntDoubleDouble() {
        Histogram histogram=new Histogram(10,1,2);
        // needs more tests
    }
    @Test public void testAddDoubleArray() {
        double[] x=new double[2];
        histogram.add(x);
        assertEquals(x.length,histogram.n());
    }
    @Test public void testAddDouble() {
        double expected=.1234;
        histogram.add(expected);
        assertEquals(expected,histogram.mean(),1e-10);
    }
    @Test public void testAddHistogram() {
        double value=.1234;
        histogram.add(value);
        Histogram h=new Histogram();
        h.add(.4321);
        histogram.add(h);
        assertEquals(.5555/2,histogram.mean(),1e-10);
        assertEquals(.1234,histogram.min(),1e-10);
        assertEquals(.4321,histogram.max(),1e-10);
    }
    @Test public void testAddHistogram2() {
        histogram.add(.1);
        histogram.add(.2);
        System.out.println(histogram);
        Histogram h=new Histogram();
        h.add(.8);
        h.add(.9);
        System.out.println(h);
        histogram.add(h);
        System.out.println(histogram);
        assertEquals(4,histogram.n());
        assertEquals(.5,histogram.mean(),1e-10);
        assertEquals(.1,histogram.min(),1e-10);
        assertEquals(.9,histogram.max(),1e-10);
    }
    @Test public void testMissing() {
        histogram.add(Double.NaN);
        assertEquals(1,histogram.missing());
    }
    @Test public void testUndeflows() {
        histogram.add(-1);
        assertEquals(1,histogram.underflows());
    }
    @Test public void testOverflows() {
        histogram.add(2);
        assertEquals(1,histogram.overflows());
    }
    @Test public void testRecent() {
        double value=.1234;
        histogram.add(value);
        assertTrue(histogram.recent().contains(value));
    }
    @Test public void testClear() {
        histogram.clear();
        assertEquals(0,histogram.n());
        // these return Nan when n==0;
        //assertEquals(Double.MAX_VALUE,histogram.min(),1e-10);
        //assertEquals(Double.MIN_VALUE,histogram.max(),1e-10);
    }
    @Test public void testN() {
        assertEquals(0,histogram.n());
        double value=.1234;
        histogram.add(value);
        assertEquals(1,histogram.n());
    }
    @Test public void testLow() {
        assertEquals(0,histogram.low(),1e-10);
    }
    @Test public void testHigh() {
        assertEquals(1,histogram.high(),1e-10);
    }
    @Test public void testRange() {}
    @Test public void testBins() {
        assertEquals(10,histogram.bins());
    }
    @Test public void testMin() {
        double value=.1234;
        histogram.add(value);
        assertEquals(value,histogram.min(),1e-10);
    }
    @Test public void testMax() {
        double value=.1234;
        histogram.add(value);
        assertEquals(value,histogram.max(),1e-10);
    }
    @Test public void testSum() {
        double value=.1234;
        histogram.add(value);
        assertEquals(value,histogram.sum(),1e-10);
    }
    @Test public void testMean() {
        double value=.1234;
        histogram.add(value);
        assertEquals(value,histogram.mean(),1e-10);
        histogram.add(.4321);
        assertEquals(.5555/2,histogram.mean(),1e-10);
    }
    @Test public void testVariance() {
        double value=.1234;
        histogram.add(value);
        histogram.add(value);
        assertEquals(0,histogram.variance(),1e-10);
    }
    @Test public void testBin() {
        for(int i=-1;i<=10;i++)
            histogram.add(i/10.);
        for(int i=-1;i<=10;i++)
            assertEquals(1,histogram.bin(i));
    }
    @Test public void testMaxDifference() {
        //fail("Not yet implemented");
    }
    @Test public void testToString() {
        //fail("Not yet implemented");
    }
    @Test public void testToStringFrequency() {
        //fail("Not yet implemented");
    }
    @Test public void testObject() {
        //fail("Not yet implemented");
    }
    @Test public void testHashCode() {
        //fail("Not yet implemented");
    }
    @Test public void testEquals() {
        //fail("Not yet implemented");
    }
    @Test public void testClone() {
        //fail("Not yet implemented");
    }
    @Test public void testToString1() {
        //fail("Not yet implemented");
    }
    Histogram histogram=new Histogram();
}

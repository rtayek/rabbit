package com.tayek;
import static org.junit.Assert.*;
import org.junit.*;
import com.tayek.*;
import com.tayek.Histories.History;
import static com.tayek.io.IO.*;
public class HistoriesTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void testRecent() {
        assertEquals(zero,history.recentFailureRate());
        int failures=3;
        for(int i=1;i<=failures;i++)
            history.failure("failure "+i);
        for(int i=0;i<History.lruMax-failures;i++)
            history.success();
        for(int i=0;i<failures+1;i++) {
            double expected=(failures-i)/(double)History.lruMax;
            double actual=history.recentFailureRate();
            assertEquals(expected,actual,1e-9);
            history.success();
        }
    }
    History history=new History();
    static final Double zero=0.;
}

package com.tayek.utilities;
import static org.junit.Assert.*;
import java.util.Date;
import org.junit.*;
import org.junit.rules.TestRule;
import com.tayek.MyTestWatcher;
import com.tayek.utilities.Range;
import static com.tayek.utilities.Range.*;
public class RangeTestCase {
    @Rule public TestRule watcher=new MyTestWatcher();

    
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void test() {
        assertTrue(range(11,22).contains(17));
        assertFalse(range(11,22).contains(28));
        //assertTrue(range(date("11.04.2005"),date("27.09.2007")).contains(date("23.07.2006")));
        //assertTrue(range(date("01.01.2001"),date("01.01.2037")).contains(new Date()));
        assertTrue(range('a','z').contains('q'));
    }
}

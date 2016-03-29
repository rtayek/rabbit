package com.tayek.tablet;
import static com.tayek.io.IO.*;
import static org.junit.Assert.*;
import java.util.Set;
import org.junit.*;
import com.tayek.tablet.Tablet;
public class CreateTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void test() {
        Set<Tablet> tablets=Tablet.createForTest(1,1234);
        for(Tablet tablet:tablets)
            if(tablet.historiesAreInconsitant()) fail("historiesAreInconsitant");
    }
}

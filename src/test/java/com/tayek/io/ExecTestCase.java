package com.tayek.io;
import static org.junit.Assert.*;
import org.junit.*;
import static com.tayek.io.IO.*;
public class ExecTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void test() throws InterruptedException {
        assertTrue(Exec.canWePing("127.0.0.1",5_000));
        assertTrue(Exec.canWePing("localhost",5_000));
        if(System.getProperty("os.name").contains("indows")) assertTrue(Exec.canWePing("mit.edu",5_000));
        else assertFalse(Exec.canWePing("mit.edu",5_000));
        assertFalse(Exec.canWePing("notAHostName",5_000));
    }
}

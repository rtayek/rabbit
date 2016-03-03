package com.tayek.tablet.io;
import static org.junit.Assert.*;
import java.net.*;
import java.util.Set;
import org.junit.*;
import com.tayek.tablet.Main;
import static com.tayek.tablet.io.IO.*;
public class NetworkIOtestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void testDefaultNetworkPrefix() throws UnknownHostException {
        Set<InetAddress> x=myInetAddress(Main.networkPrefix);
        assertTrue(x.size()>0);
        for(InetAddress y:x)
            assertTrue(y.getHostAddress().contains(Main.networkPrefix));
    }
    @Test public void testDefaultTestingHost() throws UnknownHostException {
        Set<InetAddress> x=myInetAddress(Main.defaultTestingHost);
        assertTrue(x.size()>0);
        for(InetAddress y:x)
            assertTrue(y.getHostAddress().contains(Main.defaultTestingHost));
    }
}

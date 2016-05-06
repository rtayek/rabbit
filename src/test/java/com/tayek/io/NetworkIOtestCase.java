package com.tayek.io;
import static com.tayek.io.IO.*;
import static org.junit.Assert.*;
import java.net.*;
import java.util.Set;
import org.junit.*;
import com.tayek.tablet.*;
public class NetworkIOtestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void testDefaultNetworkPrefix() throws UnknownHostException {
        Set<InetAddress> x=addressesWith(tabletRouterPrefix);
        assertTrue(x.size()>0);
        for(InetAddress y:x)
            assertTrue(y.getHostAddress().contains(tabletRouterPrefix));
    }
    @Test public void testDefaultTestingHost() throws UnknownHostException {
        Set<InetAddress> x=addressesWith(testingHost);
        assertTrue(x.size()>0);
        for(InetAddress y:x)
            assertTrue(y.getHostAddress().contains(testingHost));
    }
}

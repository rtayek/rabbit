package com.tayek.tablet;
import static org.junit.Assert.*;
import org.junit.*;
import com.tayek.tablet.*;
public class HeartbeatTestCase extends AbstractTabletTestCase {
    @Test public void test() {
        tablets=createForTest(2,serviceOffset);
        startListening();
        // how do we test?
        // need to keep some history
        // add heartbeats to history class?
        shutdown();
    }
}

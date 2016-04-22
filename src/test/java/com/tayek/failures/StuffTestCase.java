package com.tayek.failures;
import static org.junit.Assert.*;
import java.util.*;
import java.util.logging.Level;
import org.junit.*;
import static com.tayek.io.IO.*;
import com.tayek.Required;
import com.tayek.io.LoggingHandler;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.*;
import com.tayek.tablet.MessageReceiver.Model;
public class StuffTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.WARNING);
    }
    @After public void tearDown() throws Exception {}
    @Test public void testNullWithLinkedHashSet() {
        Set<String> set=new LinkedHashSet<>();
        boolean foo=set.contains(null);
    }
    @Test(expected=NullPointerException.class) public void testNullWithTreeSet() {
        Set<String> set=new TreeSet<>();
        set.add(null);
        boolean foo=set.contains(null);
    }
    @Test(expected=NullPointerException.class) public void testNullWithTreeMap() {
        TreeMap<String,Required> map=new TreeMap<>();
        Set<String> set=map.keySet();
        boolean foo=set.contains(null);
    }
    @Test public void testClone() {
        Map<String,Required> map=new Groups().groups.get("g0");
        Group group=new Group("1",map,Model.mark1);
        Group clone=group.clone();
        group.required("T1").histories().senderHistory.history.success();
        if(false) {
            p("original group: "+group.required("T1").histories().senderHistory.history.successes());
            p("original histories #: "+group.required("T1").histories().serialNumber);
            p("clone group: "+clone.required("T1").histories().senderHistory.history.successes());
            p("clone histories #: "+clone.required("T1").histories().serialNumber);
        }
        // histories have same serial numbers.
        // they should
        Set<TabletImpl2> tablets=group.createAll();
        Iterator<TabletImpl2> i=tablets.iterator();
        TabletImpl2 first=i.next();
        TabletImpl2 second=i.next();
        if(false) {
            p("first tablet histories #: "+first.histories().serialNumber);
            p("second tablet histories #: "+second.histories().serialNumber);
        }
        assertFalse(first.histories().serialNumber.equals(second.histories().serialNumber));
    }
    @Test public void testClone2() {
        Map<String,Required> map=new Groups().groups.get("g2");
        Group group=new Group("1",map,Model.mark1);
        Set<TabletImpl2> tablets=group.createAll();
        Iterator<TabletImpl2> i=tablets.iterator();
        TabletImpl2 tablet1=i.next(),tablet2=i.next();
        tablet1.histories().senderHistory.history.success();
        if(false) {
            p("t1 successes: "+tablet1.histories().senderHistory.history.successes());
            p("t1 histories #: "+tablet1.histories().serialNumber);
            p("t2 successes: "+tablet2.histories().senderHistory.history.successes());
            p("t2 histories #: "+tablet2.histories().serialNumber);
        }
        assertFalse(tablet1.histories().serialNumber.equals(tablet2.histories().serialNumber));
        assertEquals(Integer.valueOf(1),tablet1.histories().senderHistory.history.successes());
        assertEquals(Integer.valueOf(0),tablet2.histories().senderHistory.history.successes());
    }
    @Test public void testToString() {
        Group group=new Group("1",new Groups().groups.get("g0"),Model.mark1);
        String string=group.toString();
        // what to test for here?
    }
}

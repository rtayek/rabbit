package com.tayek.failures;
import static org.junit.Assert.*;
import java.util.*;
import org.junit.*;
import com.tayek.tablet.Tablet;
import static com.tayek.io.IO.*;
import com.tayek.tablet.Main.Stuff;
import com.tayek.tablet.Main.Stuff.*;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.Messages.Message;
public class StuffTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
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
        TreeMap<String,Info> map=new TreeMap<>();
        Set<String> set=map.keySet();
        boolean foo=set.contains(null);
    }
    @Test public void test() {
        Stuff stuff=new Stuff();
        Tablet tablet=new Tablet(stuff.clone(),null);
        tablet.startListening(null);
        String tabletId="1";
        Message message=tablet.stuff.messages.normal(tablet.groupId,tabletId,1,tablet.model);
        tablet.broadcast(message,stuff);
        // check for something!
    }
    @Test public void testToString() {
        Stuff stuff=new Stuff(1,new Groups().groups.get("g0"),Model.mark1);
        p("stuff: "+stuff);
    }
}

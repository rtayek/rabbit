package com.tayek.failures;
import static org.junit.Assert.*;
import java.util.*;
import org.junit.*;
import static com.tayek.io.IO.*;
import com.tayek.Required;
import com.tayek.tablet.*;
import com.tayek.tablet.Main.Stuff;
import com.tayek.tablet.Main.Stuff.*;
import com.tayek.tablet.MessageReceiver.Model;
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
        TreeMap<String,Required> map=new TreeMap<>();
        Set<String> set=map.keySet();
        boolean foo=set.contains(null);
    }
    @Test public void test() {
        Stuff stuff=new Stuff();
        Tablet tablet=new Tablet(stuff.clone(),"T1");
        tablet.startListening(null);
        String tabletId="1";
        Message message=tablet.stuff.messages.normal(tablet.groupId,tabletId,1,tablet.model.toCharacters());
        tablet.broadcast(message,stuff);
        // check for something!
    }
    @Test public void testToString() {
        Stuff stuff=new Stuff(1,new Groups().groups.get("g0"),Model.mark1);
        p("stuff: "+stuff);
    }
}

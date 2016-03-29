package com.tayek.tablet;
import java.util.Map;
import org.junit.*;
import com.tayek.tablet.Main.Stuff;
import static com.tayek.tablet.Main.Stuff.*;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.Enums.MenuItem;
import com.tayek.tablet.io.*;
public class TabletMenuItemTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        LoggingHandler.init();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        Map<String,Info> infos=new Groups().groups.get("g2");
        Stuff stuff=new Stuff(1,infos,Model.mark1);
        tablet=Tablet.create(stuff,stuff.keys().iterator().next());
        stuff=null; // tablet has a clone of group!
    }
    @After public void tearDown() throws Exception {}
    @Test public void test() {
        for(MenuItem menuItem:MenuItem.values()) {
            //p("menu item: "+menuItem);
            menuItem.doItem(tablet);
            // not really testing these are we?
            // also, getting socket closed exceptions
        }
    }
    Tablet tablet;
}

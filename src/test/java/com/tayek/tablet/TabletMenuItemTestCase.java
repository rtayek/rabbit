package com.tayek.tablet;
import java.util.Map;
import org.junit.*;
import com.tayek.tablet.Group.*;
import com.tayek.tablet.Receiver.Model;
import com.tayek.tablet.Enums.MenuItem;
import com.tayek.tablet.io.*;
public class TabletMenuItemTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        LoggingHandler.init();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        Map<Integer,Info> info=new Groups().groups.get("g2");
        group=new Group(1,info,Model.mark1,false);
        tablet=group.create(group.tabletIds().iterator().next());
        group=null; // tablet has a clone of group!
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
    Group group;
    Tablet tablet;
}

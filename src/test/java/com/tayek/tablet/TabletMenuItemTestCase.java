package com.tayek.tablet;
import java.util.Map;
import org.junit.*;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.*;
import com.tayek.io.LoggingHandler;
import com.tayek.tablet.Enums.MenuItem;
import com.tayek.tablet.io.*;
import com.tayek.tablet.Group.*;
public class TabletMenuItemTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        LoggingHandler.init();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        Map<String,Required> requireds=new Groups().groups.get("g2OnPc");
        Group group=new Group("1",requireds,Model.mark1);
        Model model=group.getModelClone();
        tablet=(TabletImpl2)Tablet.factory.create2(group.keys().iterator().next(),group,model);
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
    TabletImpl2 tablet;
}

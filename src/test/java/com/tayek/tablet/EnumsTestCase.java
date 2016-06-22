package com.tayek.tablet;
import java.util.*;
import java.util.logging.Level;
import org.junit.*;
import org.junit.rules.TestRule;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.*;
import com.tayek.io.LoggingHandler;
import com.tayek.tablet.io.*;
import static com.tayek.io.IO.*;
import static org.junit.Assert.*;
import com.tayek.tablet.Enums.*;
import com.tayek.tablet.Group.*;
import com.tayek.tablet.Message.Type;
public class EnumsTestCase {
    @Rule public TestRule watcher=new MyTestWatcher();

    static Set<Class> classes=new LinkedHashSet<>();
    static {
        classes.add(MenuItem.class);
        classes.add(LevelSubMenuItem.class);
    }
    public static <T extends Enum<T>> T getInstance(final String value,final Class<T> enumClass) {
        T t=null;
        try {
            t=Enum.valueOf(enumClass,value); // http://stackoverflow.com/a/6769004/51292
        } catch(IllegalArgumentException e) {}
        return t;
    }
    public static <T extends Enum<T>> Enum findEnum(String string) {
        for(Class<T> clazz:classes) {
            T t=null;
            try {
                t=Enum.valueOf(clazz,string); // http://stackoverflow.com/a/6769004/51292
            } catch(IllegalArgumentException e) {}
            if(t!=null) return t;
        }
        return null;
    }
    @Test public void testFindEnum() {
        String expected=MenuItem.Connect.name();
        Enum actual=findEnum(expected);
        assertEquals(expected,actual.name());
        String expected2=LevelSubMenuItem.all.name();
        Enum actual2=findEnum(expected2);
        assertEquals(expected2,actual2.name());
    }
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        LoggingHandler.init();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        Map<String,Required> requireds=new Groups().groups.get("g2OnPc");
        Group group=new Group("1",requireds,Model.mark1);
        tablet=Tablet.factory.create(Tablet.Type.normal,group,group.keys().iterator().next(),group.getModelClone());
        group=null; // tablet has a clone of group!
    }
    @After public void tearDown() throws Exception {}
    @Test public void testDoItem() {
        for(MenuItem menuItem:MenuItem.values()) {
            //p("menu item: "+menuItem);
            menuItem.doItem(tablet);
            // not really testing these are we?
            // also, getting socket closed exceptions
        }
    }
    Tablet tablet;
}

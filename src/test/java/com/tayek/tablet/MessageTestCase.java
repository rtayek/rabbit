package com.tayek.tablet;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TestRule;
import static com.tayek.io.IO.*;
import static com.tayek.utilities.Utility.*;
import com.tayek.utilities.Single;
import com.tayek.*;
import com.tayek.io.LoggingHandler;
import com.tayek.tablet.Message.*;
import com.tayek.tablet.MessageReceiver.Model;
public class MessageTestCase {
    @Rule public TestRule watcher=new MyTestWatcher();

    @BeforeClass public static void setUpBeforeClass() throws Exception {
        LoggingHandler.init();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void testTypes() {
        for(Type type:Type.values())
            if(type.equals(Type.normal)) {
                assertTrue(type.isNormal());
                assertFalse(type.isControl());
            } else {
                assertFalse(type.isNormal());
                assertTrue(type.isControl());
            }
    }
    @Test public void testNormalMessageWithTrue() {
        model.setState(3,true);
        Message message=factory.normal("1","2",3,model.toCharacters());
        assertEquals(one,message.number());
        String expected=message.toString();
        p("expected: "+expected);
        Message m=factory.from(expected);
        String actual=m.toString();
        p("actual  : "+actual);
        assertEquals(expected,actual);
    }
    @Test public void testNormalMessageWithFalse() {
        Message message=factory.normal("1","2",3,model.toCharacters());
        String expected=message.toString();
        Message m=factory.from(expected);
        String actual=m.toString();
        assertEquals(expected,actual);
    }
    @Test public void testDummyMessage() {
        Message message=factory.other(Type.dummy,"1","1");
        String expected=message.toString();
        Message m=factory.from(expected);
        String actual=m.toString();
        assertEquals(expected,actual);
    }
    @Test public void testErrorMessage() {
        Message message=factory.error("foo");
        String expected=message.toString();
        Message m=factory.from(expected);
        //m.number=message.number;
        String actual=m.toString();
        p(expected+"=?="+actual);
        //assertEquals(expected,actual);
        // error message is very special!
    }
    @Test public void testResetMessage() {
        Message message=factory.other(Type.reset,"1","1");
        String expected=message.toString();
        Message m=factory.from(expected);
        String actual=m.toString();
        assertEquals(expected,actual);
    }
    @Test public void testNameMessage() {
        // how to do this without rolling up a tablet
    }
    Model model=Model.mark1.clone();
    Required required=new Required("T0","localhost",++service);
    Factory factory=Message.instance.create(required.host,required.service,new Single<Integer>(0));
    static int service=1111;
}

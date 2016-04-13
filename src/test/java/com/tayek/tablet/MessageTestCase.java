package com.tayek.tablet;
import static org.junit.Assert.*;
import org.junit.*;
import static com.tayek.io.IO.*;
import static com.tayek.utilities.Utility.*;
import com.tayek.utilities.Single;
import com.tayek.io.LoggingHandler;
import com.tayek.tablet.Message.*;
import com.tayek.tablet.MessageReceiver.Model;
public class MessageTestCase {
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
        Message message=messages.normal("1","2",3,model.toCharacters());
        assertEquals(one,message.number());
        String expected=message.toString();
        Message m=messages.from(expected);
        String actual=m.toString();
        assertEquals(expected,actual);
    }
    @Test public void testNormalMessageWithFalse() {
        Message message=messages.normal("1","2",3,model.toCharacters());
        String expected=message.toString();
        Message m=messages.from(expected);
        String actual=m.toString();
        assertEquals(expected,actual);
    }
    @Test public void testDummyMessage() {
        Message message=messages.other(Type.dummy,"1","1");
        String expected=message.toString();
        Message m=messages.from(expected);
        String actual=m.toString();
        assertEquals(expected,actual);
    }
    @Test public void testErrorMessage() {
        Message message=messages.error("foo");
        String expected=message.toString();
        Message m=messages.from(expected);
        //m.number=message.number;
        String actual=m.toString();
        p(expected+"=?="+actual);
        //assertEquals(expected,actual);
        // error message is very special!
    }
    @Test public void testResetMessage() {
        Message message=messages.other(Type.reset,"1","1");
        String expected=message.toString();
        Message m=messages.from(expected);
        String actual=m.toString();
        assertEquals(expected,actual);
    }
    @Test public void testNameMessage() {
        // how to do this without rolling up a tablet
    }
    Model model=Model.mark1.clone();
    Factory messages=Message.instance.create(new Single<Integer>(0));
}

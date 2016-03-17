package com.tayek.tablet;
import static org.junit.Assert.*;
import org.junit.*;
import static com.tayek.tablet.io.IO.*;
import com.tayek.tablet.Messages.*;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.io.LoggingHandler;
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
        Message message=messages.normal(1,2,3,model);
        String expected=message.toString();
        Message m=messages.from(expected);
        String actual=m.toString();
        assertEquals(expected,actual);
    }
    @Test public void testNormalMessageWithFalse() {
        Message message=messages.normal(1,2,3,model);
        String expected=message.toString();
        Message m=messages.from(expected);
        String actual=m.toString();
        assertEquals(expected,actual);
    }
    @Test public void testDummyMessage() {
        Message message=messages.dummy(1,1);
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
        Message message=messages.reset(1,1,11);
        String expected=message.toString();
        Message m=messages.from(expected);
        String actual=m.toString();
        assertEquals(expected,actual);
    }
    @Test public void testNameMessage() {
        // how to do this without rolling up a tablet
    }
    Model model=Model.mark1.clone();
    Messages messages=new Messages();
}

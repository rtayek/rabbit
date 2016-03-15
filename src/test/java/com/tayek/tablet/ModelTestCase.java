package com.tayek.tablet;
import static com.tayek.tablet.io.IO.p;
import static org.junit.Assert.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.tablet.Messages.Message;
import com.tayek.tablet.Receiver.Model;
import com.tayek.tablet.io.LoggingHandler;
public class ModelTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        LoggingHandler.init();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LoggingHandler.setLevel(Level.OFF);
        model.l.warning("setup");
    }
    @After public void tearDown() throws Exception {
        model.l.warning("teardown");
    }
    /*@Test*/ public void testShort() {
        message=messages.normal(1,1,2,model);
        model.receive(message);
        assertTrue(model.state(2));
    }
    @Test public void testJustRight() {
        Model m=model.clone();
        m.setState(2,true);
        message=messages.normal(1,1,2,m);
        model.receive(message);
        assertTrue(model.state(2));
    }
    /*@Test*/ public void testTooLong() {
        message=messages.normal(1,1,2,model);
        model.receive(message);
        assertTrue(model.state(2));
    }
    @Test public void testStateWithValueTooSmall() {
        Boolean state=model.state(0);
        assertTrue(state==null);
    }
    @Test public void testStateWithValueTooBig() {
        Boolean state=model.state(model.buttons+1);
        assertTrue(state==null);
    }
    @Test public void testSetStateWithValueTooSmall() {
        model.setState(0,true);
    }
    @Test public void testSetStateWithValueTooBig() {
        model.setState(model.buttons+1,true);
    }
    @Test public void testCheck() {
        int actual=model.check(model.toCharacters());
        assertEquals(0,actual);
        model.setState(1,true);
        actual=model.check(model.toCharacters());
        assertEquals(0,actual);
    }
    @Test public void testCheckWithOneError() {
        String states=model.toCharacters();
        model.setState(1,true);
        int actual=model.check(states);
        assertEquals(1,actual);
    }
    @Test public void testCheckWithTwoErrors() {
        String states=model.toCharacters();
        model.setState(1,true);
        model.setState(2,true);
        p("model: "+model);
        int actual=model.check(states);
        assertEquals(2,actual);
    }
    @Test public void testCheckWithAllErrors() {
        String states=model.toCharacters();
        for(int buttonId=1;buttonId<=model.buttons-/*avoid reset*/1;buttonId++)
            model.setState(buttonId,true);
        int actual=model.check(states);
        assertEquals(model.buttons-1,actual);
    }
    @Test public void testReset() {
        for(int i=1;i<=model.buttons;i++)
            model.setState(i,true);
        model.reset();
        for(int i=1;i<=model.buttons;i++)
            assertFalse(model.state(i));
    }
    Model model=new Model(7,null);
    Messages messages=new Messages();
    Message message;
}

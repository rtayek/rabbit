package com.tayek.tablet;
import static org.junit.Assert.*;
import java.util.logging.Level;
import org.junit.*;
import com.tayek.Required;
import com.tayek.io.LoggingHandler;
import com.tayek.tablet.Message.Factory;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.utilities.Single;
import static com.tayek.io.IO.*;
public class ModelTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        LoggingHandler.init();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        LoggingHandler.setLevel(Level.OFF);
        l.warning("setup");
    }
    @After public void tearDown() throws Exception {
        l.warning("teardown");
    }
    /*@Test*/ public void testShort() {
        message=factory.normal("1","1",2,model.toCharacters());
        model.receive(message);
        assertTrue(model.state(2));
    }
    @Test public void testJustRight() {
        Model m=model.clone();
        m.setState(2,true);
        message=factory.normal("1","1",2,m.toCharacters());
        model.receive(message);
        assertTrue(model.state(2));
    }
    /*@Test*/ public void testTooLong() {
        message=factory.normal("1","1",2,model.toCharacters());
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
    Required required=new Required("T0","localhost",++service);
    Factory factory=Message.instance.create(required.host,required.service,new Single<Integer>(0));
    Message message;
    static int service=2222;
}

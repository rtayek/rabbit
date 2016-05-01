package com.tayek.tablet;
import static org.junit.Assert.*;
import java.io.*;
import java.util.Map;
import org.junit.*;
import com.tayek.*;
import com.tayek.io.LoggingHandler;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.Group.*;

import com.tayek.tablet.io.*;
public class ControllerTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        LoggingHandler.init();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        Group group=new Group("1",new Groups().groups.get("g2OnPc"),Model.mark1);
        Model model=group.getModelClone();
        tablet=(TabletImpl2)Tablet.factory.create2(group.keys().iterator().next(),group,model);
        group=null; // this tablet has a clone of group!
    }
    @After public void tearDown() throws Exception {}
    @Test public void test() {}
    @Test public void testController() throws InterruptedException,IOException {
        String input="s\nb 1\np\nh\na\na\nc\nc\nr\nt\nq\n";
        InputStream bais=new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        PrintStream ps=new PrintStream(baos);
        Group group=new Group("1",new Groups().groups.get("g2OnPc"),Model.mark1);
        controller=new Controller(group,false,bais,ps);
        controller.run();
        Thread.sleep(10);
        ps.flush();
        ps.close();
        assertTrue(baos.toString().contains("{TFFFFFFFFFF}"));
    }
    Tablet tablet;
    Controller controller;
    static Map<Integer,Required> required;
}

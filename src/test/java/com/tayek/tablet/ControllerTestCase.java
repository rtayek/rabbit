package com.tayek.tablet;
import static org.junit.Assert.*;
import java.io.*;
import java.util.Map;
import org.junit.*;
import com.tayek.tablet.Main.Stuff;
import static com.tayek.tablet.Main.Stuff.*;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.io.*;
public class ControllerTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        LoggingHandler.init();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {
        Stuff stuff=new Stuff(1,new Groups().groups.get("g2"),Model.mark1);
        tablet=Tablet.create(stuff,stuff.keys().iterator().next());
        stuff=null; // this tablet has a clone of group!
    }
    @After public void tearDown() throws Exception {}
    @Test public void test() {}
    @Test public void testController() throws InterruptedException,IOException {
        String input="s\nb 1\np\nh\na\na\nc\nc\nr\nt\nq\n";
        InputStream bais=new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        PrintStream ps=new PrintStream(baos);
        controller=new Controller(tablet,bais,ps);
        controller.run();
        Thread.sleep(10);
        ps.flush();
        ps.close();
        assertTrue(baos.toString().contains("{TFFFFFFFFFF}"));
    }
    Tablet tablet;
    Controller controller;
    static Map<Integer,Info> info;
}

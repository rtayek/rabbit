package com.tayek.speed;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TestRule;
import com.tayek.MyTestWatcher;
import static com.tayek.io.IO.*;
public class MessageTestCase {
    @Rule public TestRule watcher=new MyTestWatcher();

    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void test() {
       // message=new Message("foo","bar");
        //String string=message.toString();
        //p("string: "+string);
        //Message m=message=Message.from(string);
       // assertEquals(string,m.toString());
    }
    //Message message;
}

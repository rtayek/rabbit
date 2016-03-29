package com.tayek.sablet;
import static org.junit.Assert.*;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import org.junit.*;
import com.tayek.tablet.*;
import com.tayek.tablet.MessageReceiver.Model;
import static com.tayek.io.IO.*;
public class TwoTabletsSendingNormalMessagesTestCase extends AbstractTabletTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        AbstractTabletTestCase.setUpBeforeClass();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {
        AbstractTabletTestCase.tearDownAfterClass();
    }
    @Before public void setUp() throws Exception {
        super.setUp();
    }
    @After public void tearDown() throws Exception {
        super.tearDown();
    }
    void sendMessagesToTablets(int n) throws InterruptedException {
        tablets=Tablet.createForTest(2,serviceOffset);
        for(Tablet tablet:tablets)
            p(tablet.stuff.useExecutorService+" "+tablet.stuff.runCanceller+" "+tablet.stuff.waitForSendCallable);
        startListening();
        Model model=tablets.iterator().next().model;
        //p("model: "+model);
        int buttonId=model.buttons-1;
        for(Tablet tablet:tablets) { // maybe have a version that just sends to first?
            for(int i=0;i<n;i++) {
                tablet.click((++buttonId-1)%(model.buttons-1)+1);
                Thread.sleep(20); // nobody can press buttons this fast
            }
            Thread.sleep(100); // let other tablets process the message(s)
        }
        Thread.sleep(100);
        shutdown();
        for(Tablet tablet:tablets) {
            Histories history=tablet.histories();
            assertEquals(new Integer(0),history.client.client.failures());
            assertEquals(new Integer(0),history.server.server.failures());
            assertEquals(new Integer(0),history.server.missing.failures());
            //p("model: "+tablet.model);
            assertEquals(model.toCharacters(),tablet.model.toCharacters());
            assertEquals(model.toString(),tablet.model.toString());
        }
    }
    @Test() public void testOneNormalMessage() throws InterruptedException,UnknownHostException,ExecutionException {
        sendMessagesToTablets(1);
    }
    @Test() public void testThreeNormalMessage() throws InterruptedException,UnknownHostException,ExecutionException {
        sendMessagesToTablets(3);
    }
    @Test() public void testFiveNormalMessage() throws InterruptedException,UnknownHostException,ExecutionException {
        sendMessagesToTablets(5);
    }
}

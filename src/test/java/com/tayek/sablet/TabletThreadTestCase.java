package com.tayek.sablet;
import static org.junit.Assert.*;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import com.tayek.io.IO;
import static com.tayek.utilities.Utility.*;
@RunWith(Parameterized.class) public class TabletThreadTestCase extends TwoTabletsSendingNormalMessagesTestCase {
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
    public TabletThreadTestCase(Boolean useExecutorService,Boolean runCanceller,Boolean waitForSendCallable) {
        this.useExecutorService=useExecutorService;
        this.runCanceller=runCanceller;
        this.waitForSendCallable=waitForSendCallable;
    }
    @Parameters public static Collection<Object[]> data() throws UnknownHostException,InterruptedException,ExecutionException {
        List<Object[]> parameters=new ArrayList<Object[]>();
        parameters.add(new Object[] {T,T,F});
        parameters.add(new Object[] {T,F,F});
        parameters.add(new Object[] {F,F,T});
        parameters.add(new Object[] {F,F,T});
        return parameters;
    }
    @Test public void test() {}
}

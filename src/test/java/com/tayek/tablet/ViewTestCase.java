package com.tayek.tablet;
import static org.junit.Assert.*;
import java.util.Observable;
import org.junit.*;
import com.tayek.tablet.io.LoggingHandler;
public class ViewTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {
        LoggingHandler.init();
    }
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void test() {
        Model model=new Model(5,null);
        /*View view=*/new View.CommandLine(model);
        model.setState(1,true);
        assertTrue(model.state(1)); // does not really test the view :(
    }
    @Test public void testWhenWeSeeAnotherModelLikeOurs() {
        Model model=new Model(5,null);
        View view=new View.CommandLine(model);
        model.addObserver(view);
        Model model2=new Model(7,null);
        model2.addObserver(view);
        model2.setState(6,true);
    }
    static class TestObservable extends Observable {
        public void setChangedAndNotify(Object object) {
            setChanged();
            notifyObservers(object);
        }
    }
    @Test public void testWhenWeSeeAnotherModelUnlikeOurs() {
        Model model=new Model(5,null);
        View view=new View.CommandLine(model);
        TestObservable testObservable=new TestObservable();
        testObservable.addObserver(view);
        testObservable.setChangedAndNotify(new Object());
    }
}

package com.tayek.tablet;
import static org.junit.Assert.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
class Foo {}
@RunWith(Parameterized.class) public class ExampleParameterizedTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    public ExampleParameterizedTestCase(Foo foo) {
        this.foo=foo;
    }
    @Parameters public static Collection<Object[]> data() {
        List<Object[]> list=new ArrayList<Object[]>();
        list.add(new Object[] {new Foo()});
        list.add(new Object[] {new Foo()});
        for(Object[] objects:list)
            System.out.println(Arrays.asList(objects));
        return list;
    }
    @Test public void test() {
        assertNotNull(foo);
    }
    final Foo foo;
}

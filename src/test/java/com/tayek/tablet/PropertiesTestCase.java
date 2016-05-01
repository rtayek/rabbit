package com.tayek.tablet;
import static org.junit.Assert.*;
import java.io.*;
import java.util.Properties;
import org.junit.*;
public class PropertiesTestCase {
    @BeforeClass public static void setUpBeforeClass() throws Exception {}
    @AfterClass public static void tearDownAfterClass() throws Exception {}
    @Before public void setUp() throws Exception {}
    @After public void tearDown() throws Exception {}
    @Test public void testRoundTripWithString() throws IOException {
        Properties expected=new Properties();
        expected.setProperty("foo","bar");
        StringWriter stringWriter=new StringWriter();
        expected.store(stringWriter,null);
        stringWriter.close();
        StringReader stringReader=new StringReader(stringWriter.toString());
        Properties actual=new Properties();
        actual.load(stringReader);
        assertEquals(expected,actual);
    }
    @Test public void testRoundTripWithResource() throws IOException {
        Properties expected=new Properties();
        expected.setProperty("foo","bar");
        File dir=new File("./src/test/resources/");
        File dir2=new File(dir,"com/tayek/tablet/");
        File file=new File(dir2,"test.properties");
        Writer writer=new FileWriter(file);
        expected.store(writer,null);
        writer.close();
        Properties actual=new Properties();
        InputStream in=getClass().getResourceAsStream("test.properties");
        actual.load(in);
        in.close();
        assertEquals(expected,actual);
    }
}

package com;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TestRule;
import com.tayek.MyTestWatcher;
import java.util.*;
interface Item {
    void f();
}
enum Color implements Item {
    red,green,blue;
    @Override public void f() {
        System.out.println("color is: "+this);
    }
}
enum Unit implements Item {
    gram,meter,second;
    @Override public void f() {
        System.out.println("unit is: "+this);
    }
}
class Enums {
    public <T extends Enum<T>> Enum<?> findEnum(String string) {
        for(Class<T> clazz:classes)
            try {
                T t=Enum.valueOf(clazz,string); // http://stackoverflow.com/a/6769004/51292
                return t;
            } catch(IllegalArgumentException e) {}
        return null;
    }
    Set<Class> classes=new LinkedHashSet<>();
    {
        classes.add(Color.class);
        classes.add(Unit.class);
    }
    public static void main(String[] arguments) {
        ((Item)new Enums().findEnum(Color.green.name())).f();
        ((Item)new Enums().findEnum(Unit.meter.name())).f();
    }
}
public class SampleEnumsTestCase {
    @Rule public TestRule watcher=new MyTestWatcher();

    @Test public void testFindEnumRed() {
        Color expected=Color.red;
        assertEquals(expected,enums.findEnum(expected.name()));
    }
    @Test public void testFindEnumSecond() {
        Unit expected=Unit.second;
        assertEquals(expected,enums.findEnum(expected.name()));
    }
    Enums enums=new Enums();
}

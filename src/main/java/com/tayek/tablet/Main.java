package com.tayek.tablet;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import com.tayek.tablet.Main;
import com.tayek.tablet.io.*;
import static com.tayek.tablet.io.IO.*;
import com.tayek.utilities.*;
// http://cs.nyu.edu/~yap/prog/cygwin/FAQs.html
// http://poppopret.org/2013/01/07/suterusu-rootkit-inline-kernel-function-hooking-on-x86-and-arm/
// http://angrytools.com/android/
public class Main { // http://steveliles.github.io/invoking_processes_from_java.html
    public static class K { // constants
        // add p() here?
        // and all constants
    }
    // install notes:
    // android project needs sdk location.
    // core fails to find gradle wrapper main. fix: run gradle wrapper directly.
    // http://stackoverflow.com/questions/23081263/adb-android-device-unauthorized
    // G0K0H404542514AX - fire 1 with factory reset. (ray's 3'rf fire)
    // G0K0H40453650FLR - fire 2 (ray's 2'nd fire)
    // 015d2109aa080e1a - my nexus 7
    // 094374c354415780809 - azpen a727
    // new nexus 7's want to be in photo transfer mode (ptp?) to be recognized ny windoze. 
    // laptop ip addresses
    // 192.168.0.101
    // 192.168.1.104
    public static void main(String[] arguments) throws IllegalAccessException,IllegalArgumentException,InvocationTargetException,NoSuchMethodException,SecurityException,IOException {
        new Dispatcher(arguments) {
            {
                while(entryPoints.size()>0)
                    remove(1);
                add(Tablet.class);
                add(LogServer.class);
            }
        }.run();
    }
    public static boolean isLaptop=!System.getProperty("user.dir").contains("D:\\");
    public static final Integer defaultReceivePort=33000;
    public static String networkStub="192.168.";
    public static String networkPrefix="192.168.0.";
    public static String testingPrefix="192.168.1.";
    public static String networkAddress=isLaptop?"100":"101";
    public static String testingAddress=isLaptop?"100":"2";
    public static String networkHost=networkPrefix+networkAddress;
    public static String testingHost=testingPrefix+testingAddress;
    static {
        p("user dir: "+System.getProperty("user.dir"));
        p("network host: "+networkHost);
        p("testing host: "+testingHost);
    }
    public static final Map<Integer,String> tablets=new TreeMap<>();
    static {
        tablets.put(1,"0a9196e8"); // ab97465ca5e2af1a
        tablets.put(2,"0ab62080");
        tablets.put(3,"0ab63506"); // d0b9261d73d60b2c
        tablets.put(4,"0ab62207");
        tablets.put(5,"0b029b33"); // 3bcdcfbdd2cd4e42
        tablets.put(6,"0ab61d9b"); // 7c513f24bfe99daa
    }
}

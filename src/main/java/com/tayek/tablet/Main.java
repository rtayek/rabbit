package com.tayek.tablet;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
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
    public static final boolean isRaysPc=System.getProperty("user.dir").contains("D:\\");
    public static final boolean isLaptop=System.getProperty("user.dir").contains("C:\\Users\\");
    public static final Integer defaultReceivePort=33000;
    public static final String networkStub="192.168.";
    public static final String networkPrefix="192.168.0.";
    public static final String testingPrefix="192.168.1.";
    public static final String networkHost;
    public static final String testingHost;
    static {
        if(isRaysPc||isLaptop) {
            Set<InetAddress> myInetAddresses=IO.myInetAddresses(networkPrefix);
            if(myInetAddresses.size()>0) {
                InetAddress inetAddress=myInetAddresses.iterator().next();
                networkHost=inetAddress.getHostAddress();
            } else networkHost="localhost";
            myInetAddresses=IO.myInetAddresses(testingPrefix);
            if(myInetAddresses.size()>0) {
                InetAddress inetAddress=myInetAddresses.iterator().next();
                testingHost=inetAddress.getHostAddress();
            } else testingHost="localhost";
        } else {
            networkHost="localhost"; // nothing but trouble :(
            testingHost="localhost";
        }
    }
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

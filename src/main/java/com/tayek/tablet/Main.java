package com.tayek.tablet;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import com.tayek.*;
import com.tayek.io.LogServer;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.Group.TabletImpl2;
import com.tayek.tablet.Main;
import com.tayek.tablet.Message.Factory;
import com.tayek.tablet.io.*;
import static com.tayek.io.IO.*;
import com.tayek.utilities.*;
// http://cs.nyu.edu/~yap/prog/cygwin/FAQs.html
// http://poppopret.org/2013/01/07/suterusu-rootkit-inline-kernel-function-hooking-on-x86-and-arm/
// http://angrytools.com/android/
// http://stackoverflow.com/questions/36273115/android-app-freezing-after-few-days
// https://www.bignerdranch.com/blog/splash-screens-the-right-way/
// http://blog.iangclifton.com/2011/01/01/android-splash-screens-done-right/
// http://pete.akeo.ie/2013/09/rooting-your-nexus-7-2013-for-android.html
public class Main { // http://steveliles.github.io/invoking_processes_from_java.html
    // router login and password are both: admin
    // put chainsaw on laptop!
    // android studio project is Cb7: https://github.com/rtayek/Cb7.git
    // core java project is rabbit: https://github.com/rtayek/rabbit.git
    // swing gui is in rabbit: https://github.com/rtayek/rabbitgui2.git?
    // ssh root@192.168.0.11 -p 2222 password is admin
    // source on pc is in:
    // d:\ray\dev\conradapps\rabbit
    // d:\ray\dev\conradapps\rabbitgui2
    // d:\AndroidStudioProjects\Cb7
    // projects on laptop are in:
    // c:\Users\ray\workspace\rabbit
    // c:\Users\ray\Cb7
    // install notes: (mostly for laptop)
    // git pull origin master - should get you the latest stuff in either exiting project 
    // password for ray on git is 11213 (user: rtayek)
    // password for ray on conrad's pc and git is 11213
    // password for rays network is: i am a duck
    // password for tablets network is: i am a duck 2
    // android project needs sdk location.
    // swing gui is in rabitgui2/ 
    // core fails to find gradle wrapper main. fix: run gradle wrapper directly (gradlew).
    // when build fails because there is a lobk on the jar, stop the log server!
    // http://stackoverflow.com/questions/23081263/adb-android-device-unauthorized
    // G0K0H404542514AX - fire 1 with factory reset. (ray's 3'rd fire)
    // G0K0H40453650FLR - fire 2 (ray's 2'nd fire)
    // 015d2109aa080e1a - my nexus 7
    // 094374c354415780809 - azpen a727
    // new nexus 7's want to be in photo transfer mode (ptp?) to be recognized ny windoze. 
    // laptop ip addresses vary
    public static void main(String[] arguments) throws IllegalAccessException,IllegalArgumentException,InvocationTargetException,NoSuchMethodException,SecurityException,IOException {
        new Dispatcher(arguments) {
            {
                while(entryPoints.size()>0)
                    remove(1);
                add(TabletImpl2.class);
                add(LogServer.class);
                add(Controller.class);
            }
        }.run();
    }
}

package com.tayek.io;
import java.io.File;
import com.tayek.utilities.Utility;
public class ReadLogFiles {
    public static void main(String[] args) {
        File file=new File("5000.192.168.0.16.43662.44.20.33.log");
        StringBuffer stringBuffer=new StringBuffer();
        Utility.fromFile(stringBuffer,file);
        String string=stringBuffer.toString();
        int index=string.lastIndexOf("</record>");
        if(index!=-1) {
        //p.Foo.print(file); // gradle problems
        }
        /*
        5000.192.168.0.11.54736.44.14.44.log
        5000.192.168.0.16.43662.44.20.33.log
        5000.192.168.0.12.50271.44.14.43.log
        5000.192.168.0.16.50772.44.14.43.log
        5000.192.168.0.13.45491.44.14.43.log
        5000.192.168.0.16.58404.44.20.35.log
        5000.192.168.0.14.45104.44.14.44.log
        5000.192.168.0.15.49312.44.14.43.log
       */

    }
}

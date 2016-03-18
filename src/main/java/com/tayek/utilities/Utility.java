package com.tayek.utilities;
import static com.tayek.utilities.Utility.p;
import java.io.*;
import java.net.*;
public class Utility {
    public static String method(int n) {
        return Thread.currentThread().getStackTrace()[n].getMethodName()+"()";
    }
    public static String method() {
        return method(2);
    }
    public static void pn(PrintStream out,String string) {
        out.print(string);
        out.flush();
    }
    public static void p(String string) {
        // i hope this can stay static :(
        p(System.out,string);
    }
    public static void p(PrintStream out,String string) {
        synchronized(out) {
            pn(out,string);
            pn(out,System.getProperty("line.separator"));
        }
    }
    public static Integer toInteger(String argument) {
        Integer n=null;
        try {
            n=Integer.valueOf(argument);
        } catch(NumberFormatException e) {
            System.out.println(argument+" is not a valid integer!");
        }
        return n;
    }
    public static void fromFile(final StringBuffer stringBuffer,final File file) {
        try {
            Reader r=new FileReader(file);
            int c=0;
            while((c=r.read())!=-1)
                stringBuffer.append((char)c);
            r.close();
        } catch(FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static ServerSocket serverSocket(SocketAddress socketAddress) throws IOException {
        ServerSocket serverSocket=new ServerSocket();
        serverSocket.bind(socketAddress);
        return serverSocket;
    }
    public static void printThreads() {
        //this may need to be non static and filter tablets or groups!
        int big=2*Thread.activeCount();
        Thread[] threads=new Thread[big];
        Thread.enumerate(threads);
        for(Thread thread:threads)
            if(thread!=null) p(thread.toString());
    }
}

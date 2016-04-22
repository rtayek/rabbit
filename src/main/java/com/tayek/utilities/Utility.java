package com.tayek.utilities;
import static com.tayek.io.IO.*;
import java.io.*;
import java.net.*;
import com.tayek.io.IO;
public class Utility {
    public static String pad(String string,int length) {
        for(;string.length()<length;string+=' ')
            ;
        return string;
    }
    public static String method(int n) {
        //Thread.currentThread().dumpStack();
        return Thread.currentThread().getStackTrace()[n].getMethodName()+"()"+Thread.currentThread().getStackTrace()[n].getClassName()+" "+Thread.currentThread().getStackTrace().length;
    }
    public static String method() {
        return method(2);
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
    public static ServerSocket serverSocket(SocketAddress socketAddress) {
        Et et=new Et();
        ServerSocket serverSocket=null;
        try {
            serverSocket=new ServerSocket();
            serverSocket.bind(socketAddress);
        } catch(BindException e) {
            e.printStackTrace();
            p("after: "+et+",  caught: '"+e+"'");
        } catch(IOException e) {
            e.printStackTrace();
            p("after: "+et+",  caught: '"+e+"'");
        } catch(Exception e) {
            e.printStackTrace();
            p("after: "+et+",  caught: '"+e+"'");
        }
        return serverSocket;
    }
    public static Socket connect(SocketAddress socketAddress,int timeout) {
        Et et=new Et();
        Socket socket=new Socket();
        try {
            socket.connect(socketAddress,timeout);
            return socket;
        } catch(SocketTimeoutException e) {
            IO.l.warning(socketAddress+", after: "+et+", with timeout: "+timeout+", caught: '"+e+"'");
        } catch(IOException e) {
            IO.l.warning(socketAddress+", after: "+et+", with timeout: "+timeout+", caught: '"+e+"'");
        }
        return null;
    }
    public static final Boolean T=true,F=false;
    public static final Integer zero=0,one=1,two=2;
}

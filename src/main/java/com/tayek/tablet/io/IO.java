package com.tayek.tablet.io;
import static com.tayek.tablet.io.IO.p;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import com.tayek.tablet.Main;
public class IO {
    public interface Callback<T> { // should be Consumer<T>
        void call(T t);
    }
    public static <T> T runAndWait(Callable<T> callable) throws InterruptedException,ExecutionException {
        ExecutorService executorService=Executors.newFixedThreadPool(1); 
        Future<T> future=executorService.submit(callable);
        while(!future.isDone())
            Thread.yield();
        return future.get();
    }
    public static class GetByNameCallable implements Callable<InetAddress> {
        public GetByNameCallable(String host) {
            this.host=host;
        }
        @Override public InetAddress call() throws Exception {
            Thread.currentThread().setName(getClass().getName());
            InetAddress inetAddress=null;
            inetAddress=InetAddress.getByName(host);
            return inetAddress;
        }
        final String host;
    }
    public static Set<InetAddress> myInetAddresses(String networkPrefix) {
        Set<InetAddress> set=new LinkedHashSet<>(); // 
        try {
            Enumeration<NetworkInterface> networkInterfaces=NetworkInterface.getNetworkInterfaces();
            for(NetworkInterface networkInterface:Collections.list(networkInterfaces)) {
                for(InetAddress inetAddress:Collections.list(networkInterface.getInetAddresses()))
                    if(inetAddress.isSiteLocalAddress()&&inetAddress.getHostAddress().contains(networkPrefix)) set.add(inetAddress);
            }
        } catch(SocketException e) {
            e.printStackTrace();
        }
        return set;
    }
   public static class GetNetworkInterfacesCallable implements Callable<Set<InetAddress>> {
        public GetNetworkInterfacesCallable(String networkPrefix) {
            this.networkPrefix=networkPrefix;
        }
        @Override public Set<InetAddress> call() throws Exception {
            Thread.currentThread().setName(getClass().getName());
            return myInetAddresses(networkPrefix);
        }
        final String networkPrefix;
    }
    public static class SocketHandlerCallable implements Callable<SocketHandler> {
        public SocketHandlerCallable(String host,int service) {
            this.host=host;
            this.service=service;
        }
        @Override public SocketHandler call() throws Exception {
            Thread.currentThread().setName(getClass().getName());
            SocketHandler socketHandler=null;
            try {
                socketHandler=new SocketHandler(host,service);
                // socketHandler.setFormatter(new LoggingHandler());
                socketHandler.setLevel(Level.ALL);
                p("socket handler constructed to: "+host+":"+service);
            } catch(IOException e) {
                p("caught: '"+e+"' constructing socket handler on: "+host+":"+service);
                throw new RuntimeException(e);
            }
            return socketHandler;
        }
        final String host;
        final int service;
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
    public static void printThreads() {
        //this may need to be non static and filter tablets or groups!
        int big=2*Thread.activeCount();
        Thread[] threads=new Thread[big];
        Thread.enumerate(threads);
        for(Thread thread:threads)
            if(thread!=null) p(thread.toString());
    }
    static void printNetworkInterface(NetworkInterface netint) {
        p("Display name: "+netint.getDisplayName()+", Name: "+netint.getName());
        Enumeration<InetAddress> inetAddresses=netint.getInetAddresses();
        for(InetAddress inetAddress:Collections.list(inetAddresses))
            p("\tInetAddress: "+inetAddress+" "+inetAddress.isSiteLocalAddress());
    }
    public static void printNetworkInterfaces() {
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces=NetworkInterface.getNetworkInterfaces();
            for(NetworkInterface networkInterface:Collections.list(networkInterfaces))
                printNetworkInterface(networkInterface);
        } catch(SocketException e) {
            p("ni caught: '"+e+"'");
        }
    }
    public static void printInetAddresses(String prefix) {
        Set<InetAddress> x=myInetAddresses(prefix);
        p("addresses: "+x);
    }
    public static void main(String args[]) throws UnknownHostException {
        printNetworkInterfaces();
        InetAddress localHost=InetAddress.getLocalHost();
        p("local: "+localHost);
        String host=localHost.getHostName();
        p("host: "+host);
        InetAddress inetAddress=InetAddress.getByName(host);
        p("address: "+inetAddress);
        printInetAddresses(Main.networkStub);
        printInetAddresses(Main.networkPrefix);
        printInetAddresses(Main.testingPrefix);
        printInetAddresses(Main.networkHost);
        printInetAddresses(Main.testingHost);
    }
    public final Logger l=Logger.getLogger(getClass().getName());
    // put into instance
    public static final Logger staticLogger=Logger.getLogger(IO.class.getName());
}

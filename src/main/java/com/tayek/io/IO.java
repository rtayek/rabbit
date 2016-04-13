package com.tayek.io;
import static com.tayek.io.IO.*;
import static com.tayek.utilities.Utility.connect;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
public class IO {
    public static void pn(PrintStream out,String string) {
        out.print(string);
        out.flush();
    }
    public static void p(PrintStream out,String string) {
        synchronized(out) {
            pn(out,string);
            pn(out,System.getProperty("line.separator"));
        }
    }
    public static void p(String string) {
        // i hope this can stay static :(
        p(System.out,string);
    }
    public static String toString(Thread thread) {
        return thread.toString()+", state: "+thread.getState()+", is alive: "+thread.isAlive()+", is interrupted:  "+thread.isInterrupted();
    }
    public static Thread[] getThreads() {
        int big=2*Thread.activeCount();
        Thread[] threads=new Thread[big];
        Thread.enumerate(threads);
        return threads;
    }
    public static void printThreads() {
        // this may need to be non static and filter tablets or groups!
        // we could put id in thread name?
        Thread[] threads=getThreads();
        for(Thread thread:threads)
            if(thread!=null) p(toString(thread));
    }
    public interface Callback<T> { // should be Consumer<T>
        void call(T t);
    }
    public static <T> T runAndWait(java.util.concurrent.Callable<T> callable) throws InterruptedException,ExecutionException {
        ExecutorService executorService=Executors.newFixedThreadPool(1);
        Future<T> future=executorService.submit(callable);
        while(!future.isDone())
            Thread.yield();
        return future.get();
    }
    public static class ShutdownOptions {
        public boolean shutdownInput,shutdownOutput,closeInput,closeOutput,closeSocket=true;
    }
    public static class GetByNameCallable implements java.util.concurrent.Callable<java.net.InetAddress> {
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
    public static Set<InetAddress> addressesWith(String networkPrefix) {
        Set<InetAddress> set=new LinkedHashSet<>(); // 
        try {
            Enumeration<NetworkInterface> networkInterfaces=NetworkInterface.getNetworkInterfaces();
            for(NetworkInterface networkInterface:Collections.list(networkInterfaces))
                for(InetAddress inetAddress:Collections.list(networkInterface.getInetAddresses()))
                    if(inetAddress.isSiteLocalAddress()&&inetAddress.getHostAddress().contains(networkPrefix)) set.add(inetAddress);
        } catch(SocketException e) {
            e.printStackTrace();
        }
        return set;
    }
    public static class AddressesWithCallable implements java.util.concurrent.Callable<java.util.Set<java.net.InetAddress>> {
        public AddressesWithCallable(String networkPrefix) {
            this.networkPrefix=networkPrefix;
        }
        @Override public Set<InetAddress> call() throws Exception {
            Thread.currentThread().setName(getClass().getName());
            return addressesWith(networkPrefix);
        }
        final String networkPrefix;
    }
    public static class SocketHandlerCallable implements java.util.concurrent.Callable<java.util.logging.SocketHandler> {
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
            } catch(IOException e) {
                l.info("caught: '"+e+"' constructing socket handler on: "+host+":"+service);
            }
            return socketHandler;
        }
        final String host;
        final int service;
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
        Set<InetAddress> inetAddresses=addressesWith(prefix);
        p("addresses starting with: "+prefix+": "+inetAddresses);
    }
    public static Set<SocketAddress> discoverReal(int n) {
        Set<SocketAddress> socketAddresses=new LinkedHashSet<>();
        for(int i=11;i<11+n;i++) // fragile!
            socketAddresses.add(new InetSocketAddress(tabletNetworkPrefix+i,defaultReceivePort));
        Set<SocketAddress> good=new LinkedHashSet<>();
        int retries=2;
        for(SocketAddress socketAddress:socketAddresses)
            for(int i=1;i<=1+retries;i++) {
                Socket socket=connect(socketAddress,200);
                if(socket!=null) {
                    try {
                        socket.close();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                    good.add(socketAddress);
                    p("added: "+socketAddress);
                    break;
                }
            }
        return good;
    }
    public static void main(String args[]) throws UnknownHostException {
        printNetworkInterfaces();
        InetAddress localHost=InetAddress.getLocalHost();
        p("local: "+localHost);
        String host=localHost.getHostName();
        p("host: "+host);
        InetAddress inetAddress=InetAddress.getByName(host);
        p("address: "+inetAddress);
        printInetAddresses(networkStub);
        printInetAddresses(tabletNetworkPrefix);
        printInetAddresses(defaultHost);
        if(!defaultHost.equals(testingHost)) printInetAddresses(testingHost);
    }
    public static final boolean isRaysPc=System.getProperty("user.dir").contains("D:\\");
    public static final boolean isLaptop=System.getProperty("user.dir").contains("C:\\Users\\");
    public static final Integer defaultReceivePort=33000;
    public static final String networkStub="192.168.";
    public static final String raysPcOnTabletNetworkToday="192.168.0.101";
    public static final String laptopToday="192.168.0.100";
    public static String tabletNetworkPrefix="192.168.0.";
    public static String fakeNetworkPrefix="192.168.1.2";
    public static String defaultHost=tabletNetworkPrefix+101; // hack!
    // really need to find the above using a thread
    public static String testingHost=fakeNetworkPrefix;
    public static final Map<String,SocketHandler> logServerHosts=new LinkedHashMap<>();
    static {
        logServerHosts.put("192.168.1.2",null); // static ip on my pc
        logServerHosts.put("192.168.0.101",null); // my pc today
        logServerHosts.put("192.168.0.100",null); // laptop today
    }
    public static final Map<Integer,String> androidIds=new TreeMap<>();
   static {
        androidIds.put(1,"0a9196e8"); // ab97465ca5e2af1a
        androidIds.put(2,"0ab62080");
        androidIds.put(3,"0ab63506"); // d0b9261d73d60b2c
        androidIds.put(4,"0ab62207");
        androidIds.put(5,"0b029b33"); // 3bcdcfbdd2cd4e42
        androidIds.put(6,"0ab61d9b"); // 7c513f24bfe99daa
    }
    public static final Logger l=Logger.getLogger(IO.class.getName());
}
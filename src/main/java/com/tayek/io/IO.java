package com.tayek.io;
import static com.tayek.io.IO.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.logging.*;
import com.tayek.utilities.*;
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
        synchronized(System.out) {
            p(System.out,string);
        }
    }
    public static String toString(Thread thread) {
        return thread.toString()+", state: "+thread.getState()+", is alive: "+thread.isAlive()+", is interrupted:  "+thread.isInterrupted();
    }
    public static String toString(ServerSocket serverSocket) {
        return serverSocket+": "+serverSocket.isBound()+" "+serverSocket.isClosed();
    }
    public static boolean isAndroid() {
        return System.getProperty("http.agent")!=null;
    }
    public static void printSystemProperties() {
        Properties systemProperties=System.getProperties();
        p("system properties:");
        p("system properties size: "+systemProperties.size());
        for(Entry<Object,Object> x:systemProperties.entrySet())
            p(""+x.getKey()+'='+x.getValue());
        
    }
    // move some of these to utility?
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
    public static InetAddress addressWith(String networkPrefix) {
        Set<InetAddress> inetAddresses=addressesWith(networkPrefix);
        if(inetAddresses.size()>1) l.severe("more than one inetAddress: "+inetAddresses);
        return inetAddresses.size()>0?inetAddresses.iterator().next():null;
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
    public static class SocketHandlerCallable implements Runnable,java.util.concurrent.Callable<java.util.logging.SocketHandler> {
        public SocketHandlerCallable(String host,int service) {
            this.host=host;
            this.service=service;
        }
        @Override public void run() {
            Thread.currentThread().setName(getClass().getSimpleName()+" "+host+":"+service);
            try {
                socketHandler=new SocketHandler(host,service);
                // socketHandler.setFormatter(new LoggingHandler());
                socketHandler.setLevel(Level.ALL);
            } catch(IOException e) {
                l.info("caught: '"+e+"' constructing socket handler on: "+host+":"+service);
            }
        }
        @Override public SocketHandler call() throws Exception {
            run();
            return socketHandler;
        }
        final String host;
        final int service;
        SocketHandler socketHandler;
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
    public static Socket silentConnect(SocketAddress socketAddress,int timeout) {
        Et et=new Et();
        Socket socket=new Socket();
        try {
            socket.connect(socketAddress,timeout);
            return socket;
        } catch(Exception e) {}
        return null;
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
    public static boolean canConnect(String host,int service,int timeout) {
        boolean canConnect=false;
        InetSocketAddress inetSocketAddress=new InetSocketAddress(host,80);
        Socket socket=silentConnect(inetSocketAddress,timeout);
        if(socket!=null) {
            p("connected to router: "+host);
            canConnect=true;
            try {
                socket.close();
            } catch(IOException e) {}
        } else {
            p("can not connect to router: "+host);
            canConnect=false;
        }
        return canConnect;
    }
    public static Set<Pair<Integer,SocketAddress>> discover(boolean real,int n,int service) {
        Set<Pair<Integer,SocketAddress>> socketAddresses=new LinkedHashSet<>();
        Set<Pair<Integer,SocketAddress>> good=new LinkedHashSet<>();
        if(real) {
            for(int i=11;i<11+n;i++) // fragile!
                socketAddresses.add(new Pair<Integer,SocketAddress>(i-10,new InetSocketAddress(tabletRouterPrefix+i,service)));
        } else {
            for(int i=1;i<=n;i++)
                socketAddresses.add(new Pair<Integer,SocketAddress>(i,new InetSocketAddress(defaultHost,service+i)));
            for(int i=1;i<=n;i++)
                socketAddresses.add(new Pair<Integer,SocketAddress>(i,new InetSocketAddress(testingHost,service+i)));
        }
        int retries=3;
        for(Pair<Integer,SocketAddress> pair:socketAddresses) {
            p("trying : "+pair);
            for(int i=1;i<=1+retries;i++) {
                Socket socket=connect(pair.second,real?1_000:200);
                if(socket!=null) {
                    try {
                        socket.close();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                    p("adding: "+pair);
                    if(good.contains(pair)) p(good+" already contains: "+pair);
                    good.add(pair);
                    break;
                }
            }
        }
        return good;
    }
    public static String aTabletId(Integer tabletId) {
        return "T"+tabletId;
    }
    public static Set<Pair<Integer,SocketAddress>> discoverTestTablets(int n,int serviceBase) {
        return discover(false,n,serviceBase);
    }
    public static Set<Pair<Integer,SocketAddress>> discoverRealTablets(int n) {
        return discover(true,n,defaultReceivePort);
    }
    public static void main(String args[]) throws UnknownHostException {
        final Et et=new Et();
        printNetworkInterfaces();
        InetAddress localHost=InetAddress.getLocalHost();
        p("local: "+localHost);
        String host=localHost.getHostName();
        p("host: "+host);
        InetAddress inetAddress=InetAddress.getByName(host);
        p("address: "+inetAddress);
        printInetAddresses(networkStub);
        printInetAddresses(tabletRouterPrefix);
        printInetAddresses(defaultHost);
        if(!defaultHost.equals(testingHost)) printInetAddresses(testingHost);
        Set<InetAddress> inetAddresses=addressesWith(tabletRouterPrefix);
        p("addresses on: "+tabletRouterPrefix+" are: "+inetAddresses);
        if(!inetAddresses.contains(InetAddress.getByName(raysPcOnTabletNetworkToday))) p("address has changed, expected: "+raysPcOnTabletNetworkToday+", but got: "+inetAddresses);
        inetAddresses=addressesWith(raysRouterPrefix);
        p("addresses on: "+raysRouterPrefix+" are: "+inetAddresses);
        if(!inetAddresses.contains(InetAddress.getByName(raysPcOnRaysNetwork))) p("address has changed, expected: "+raysPcOnTabletNetworkToday+", but got: "+inetAddresses);
        p(raysRouter+" "+canConnect(raysRouter,80,1_000));
        p(tabletRouter+" "+canConnect(tabletRouter,80,1_000));
        p("ping at: "+et);
        int rc=Exec.exec("ping "+tabletRouter);
        p("ping returns: "+rc+" at: "+et);
        p("exec ping at: "+et);
        rc=Exec.ping(tabletRouter);
        p("exec ping returns: "+rc+" at: "+et);
        new Thread(new Runnable() {
            @Override public void run() {
                p("ping in thread at: "+et);
                int rc=Exec.exec("ping "+tabletRouter);
                p("ping in thread returns: "+rc+" at: "+et);
            }
        }).start();
        new Thread(new Runnable() {
            @Override public void run() {
                p("exec ping in thread at: "+et);
                int rc=Exec.ping(tabletRouter);
                p("exec ping in thread returns: "+rc+" at: "+et);
            }
        }).start();
    }
    public static final boolean isRaysPc=System.getProperty("user.dir").contains("D:\\");
    public static final boolean isLaptop=System.getProperty("user.dir").contains("C:\\Users\\");
    public static final Integer defaultReceivePort=33000;
    public static final String networkStub="192.168.";
    public static final String tabletRouterPrefix="192.168.0.";
    public static final String tabletRouter="192.168.0.1";
    public static final String tabletWifiSsid="\"tablets\"";
    public static final String raysRouterPrefix="192.168.1.";
    public static final String raysRouter="192.168.1.1";
    public static final String raysPc="192.168.1.2";
    public static final String raysPcOnTabletNetworkToday="192.168.0.101";
    public static final String raysPcOnRaysNetwork="192.168.1.2";
    public static final String laptopToday="192.168.0.100";
    public static final String defaultHost=raysPcOnTabletNetworkToday;
    public static final String testingHost=raysPcOnRaysNetwork;
    public static final Map<String,SocketHandler> logServerHosts=new TreeMap<>();
    static {
        logServerHosts.put(raysPc,null); // static ip on my pc
        logServerHosts.put(raysPcOnTabletNetworkToday,null); // my pc today
        logServerHosts.put(laptopToday,null); // laptop today
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

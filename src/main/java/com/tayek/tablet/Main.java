package com.tayek.tablet;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.SocketHandler;
import com.tayek.io.IO;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.Main;
import com.tayek.tablet.io.*;
import static com.tayek.io.IO.*;
import com.tayek.utilities.*;
// http://cs.nyu.edu/~yap/prog/cygwin/FAQs.html
// http://poppopret.org/2013/01/07/suterusu-rootkit-inline-kernel-function-hooking-on-x86-and-arm/
// http://angrytools.com/android/
// http://stackoverflow.com/questions/36273115/android-app-freezing-after-few-days
public class Main { // http://steveliles.github.io/invoking_processes_from_java.html
    public static class Stuff implements Cloneable { // maybe this belongs in sender?
        public static class Groups {
            public Groups() {
                // this needs to take testing prefix
                // really? for socket logging or what?
                boolean old=false;
                if(!old) {
                    int tablets=6;
                    for(int tabletId=1;tabletId<=tablets;tabletId++)
                        g0.put(aTabletId(tabletId),new Info(aTabletId(tabletId),Main.networkPrefix+(10+tabletId),Main.defaultReceivePort));
                } else {
                    g0.put(aTabletId(1),new Info("fire 1",Main.networkPrefix+21,Main.defaultReceivePort));
                    g0.put(aTabletId(2),new Info("fire 2",Main.networkPrefix+22,Main.defaultReceivePort));
                    g0.put(aTabletId(3),new Info("nexus 7",Main.networkPrefix+70,Main.defaultReceivePort));
                    g0.put(aTabletId(4),new Info("pc-4",Main.networkPrefix+100,Main.defaultReceivePort+4));
                    g0.put(aTabletId(5),new Info("pc-5",Main.networkPrefix+100,Main.defaultReceivePort+5));
                    g0.put(aTabletId(6),new Info("azpen",Main.networkPrefix+33,Main.defaultReceivePort));
                    g0.put(aTabletId(7),new Info("at&t",Main.networkPrefix+77,Main.defaultReceivePort));
                    g0.put(aTabletId(8),new Info("conrad",Main.networkPrefix+88,Main.defaultReceivePort));
                    //g0.put(99,new Info("nexus 4",99,IO.defaultReceivePort)));
                }
                groups.put("g0",g0);
                g2.put("pc-4",new Info("pc-4",Main.testingHost,Main.defaultReceivePort+4));
                g2.put("pc-5",new Info("pc-5",Main.testingHost,Main.defaultReceivePort+5));
                groups.put("g2",g2);
                //g1each.put(3,new Info("nexus 7",Main.networkPrefix+70,Main.defaultReceivePort));
                // two fake tablets on pc, but on different networks.
                // the 100 is dhcp'ed, so it may change once in a while.
                g1each.put("pc-4",new Info("pc-4",Main.networkHost,Main.defaultReceivePort+4));
                g1each.put("pc-5",new Info("pc-5",Main.testingHost,Main.defaultReceivePort+4));
                groups.put("g1each",g1each);
            }
            private final Map<String,Info> g2=new TreeMap<>();
            private final Map<String,Info> g0=new TreeMap<>();
            private final Map<String,Info> g1each=new TreeMap<>();
            public final Map<String,Map<String,Info>> groups=new TreeMap<>();
            // hack, change the above before calling new Groups!
        }
        public static class Info {
            public Info(String iD,String host,int port) {
                this.iD=iD;
                this.host=host;
                this.service=port;
                // add socketAddress?
                histories=new Histories();
            }
            public Histories histories() {
                return histories;
            }
            @Override public String toString() {
                return iD+" "+host+" "+service;
            }
            public final String iD;
            public final String host;
            public final Integer service;
            private final Histories histories;
        }
        public static String aTabletId(Integer tabletId) {
            return "T"+tabletId;
        }
        public Stuff() {
            this(0,Collections.<String,Info> emptyMap(),(Model)null);
        }
        public Stuff(int id,Map<String,Info> idToInfo,Model model) {
            this(id,idToInfo,model,++serialNumbers);
        }
        private Stuff(int groupId,Map<String,Info> idToInfo,Model model,int serialNumber) {
            this.serialNumber=serialNumber;
            this.groupIdx=groupId;
            this.idToInfo.putAll(idToInfo);
            // these may all end up with the same ifo, hence histories!
            int n=idToInfo.size();
            executorService=Executors.newFixedThreadPool(4*n+2);
            canceller=Executors.newScheduledThreadPool(4*n+2);
            prototype=model;
        }
        private Stuff(Stuff stuff,int serialNumber) {
            this.serialNumber=stuff.serialNumber;
            this.groupIdx=stuff.groupIdx;
            if(stuff.idToInfo!=null) this.idToInfo.putAll(stuff.idToInfo);
            int n=stuff.idToInfo.size();
            executorService=Executors.newFixedThreadPool(4*n+2);
            canceller=Executors.newScheduledThreadPool(4*n+2);
            prototype=stuff.prototype;
            copyFrom(stuff);
        }
        @Override public Stuff clone() {
            //super.clone();
            Stuff stuff=new Stuff(this,serialNumber);
            return stuff;
        }
        private void copyFrom(Stuff stuff) {
            // fragile!
            // find a better way.
            useExecutorService=stuff.useExecutorService;
            waitForSendCallable=stuff.waitForSendCallable;
            runCanceller=stuff.runCanceller;
            replying=stuff.replying;
            connectTimeout=stuff.connectTimeout;
            sendTimeout=stuff.sendTimeout;
            reportPeriod=stuff.reportPeriod;
        }
        public Set<String> keys() {
            return idToInfo!=null?idToInfo.keySet():null;
        }
        public Info info(String id) {
            return id==null?null:idToInfo!=null?idToInfo.get(id):null;
        }
        public InetSocketAddress socketAddress(String destinationTabletId) {
            Info info=info(destinationTabletId);
            if(info!=null) return new InetSocketAddress(info.host,info.service);
            return null;
        }
        public String getTabletIdFromInetAddress(InetAddress inetAddress,Integer service) {
            for(String i:keys()) // fragile!
                if(info(i)!=null) if(inetAddress.getHostAddress().equals(info(i).host)&&(service==null||service.equals(info(i).service))) return i;
            return null;
        }
        public Model getModelClone() {
            return prototype!=null?prototype.clone():null;
        }
        @Override public String toString() {
            StringBuffer sb=new StringBuffer();
            sb.append("stuff: group: ");
            sb.append(groupIdx);
            sb.append("(");
            sb.append(serialNumber);
            sb.append(")");
            Info info;
            for(String id:keys())
                if((info=info(id))!=null) {
                    sb.append("\nfor: "+id+": ");
                    sb.append(info.histories());
                }
            sb.append("\nend of stuff: -------------------------------");
            return sb.toString();
        }
        public String report(String id) {
            // need to find the tablet to get the right history?
            // looks like the server history only makes sense for the driving tablet when testing.
            // so maybe check for that when driving and omit the print.
            StringBuffer sb=new StringBuffer();
            sb.append("histories from: "+id+" ------------------------------------");
            for(String i:keys()) {
                Histories history=info(i).histories();
                if(history.anyFailures()||history.client.client.attempts()!=0) sb.append("\n\tfor "+id+" to: "+i+", history: "+history);
                else sb.append("\n\tfrom "+id+" to: "+i+", history: no failures.");
            }
            Map<Object,Float> failures=new LinkedHashMap<>();
            double sends=0;
            for(String i:keys()) {
                Histories history=info(i).histories();
                sends+=history.client.client.attempts();
                failures.put(i,(float)(1.*history.client.client.failures()/history.client.client.attempts()));
                sb.append("\nsummary from: "+id+", to:"+i+" send failure rate: "+history.client.client.failures()+"/"+history.client.client.attempts()+"="
                        +(1.*history.client.client.failures()/history.client.client.attempts()));
            }
            sb.append("\n failure rates from: "+id+" to others: "+failures);
            int big=2*Thread.activeCount();
            Thread[] threads=new Thread[big];
            Thread.enumerate(threads);
            for(Thread thread:threads)
                if(thread!=null) sb.append("\n"+thread.toString());
            sb.append("\nend of histories from: "+id+" ------------------------------------");
            return sb.toString();
        }

        public final int groupIdx;
        private final Model prototype;
        public final Integer serialNumber;
        private final Map<String,Info> idToInfo=new TreeMap<>();
        public final Messages messages=new Messages();
        public final ScheduledExecutorService canceller;
        public final ExecutorService executorService;
        public boolean useExecutorService;
        public boolean waitForSendCallable=true;
        public boolean runCanceller;
        public boolean replying;
        public Integer connectTimeout=Histories.defaultConnectTimeout; // set by tablet
        public Integer sendTimeout=Histories.defaultSendTimeout; // set by tablet
        public Integer reportPeriod=Histories.defaultReportPeriod;
        private static int serialNumbers;
    }
    public static class Instance {
        // maybe just put messages in group?
        // we did
        // now we need to set options into group?
        // or put them into the abstract test case and use
        // how to get them into the 
        // seems line each group needs it's own instance
        private Instance(String iD) {
            this.iD=iD;
        }
        public Instance create(String tabletId) {
            return new Instance(tabletId);
        }
        public Set<Instance> create(Set<String> tabletIds) { // maybe we don't need some much here
            Set<Instance> instances=new LinkedHashSet<>();
            for(String tabletId:tabletIds)
                instances.add(create(tabletId));
            return instances;
        }
        final String iD;
        SocketAddress socketAddress;
        Messages messages=new Messages();
        // looks like these two guys are the things we need
    }
    // install notes:
    // android project needs sdk location.
    // core fails to find gradle wrapper main. fix: run gradle wrapper directly.
    // http://stackoverflow.com/questions/23081263/adb-android-device-unauthorized
    // G0K0H404542514AX - fire 1 with factory reset. (ray's 3'rd fire)
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
                add(com.tayek.speed.Main.class);
                add(com.tayek.speed.Service.class);
            }
        }.run();
    }
    public static final boolean isRaysPc=System.getProperty("user.dir").contains("D:\\");
    public static final boolean isLaptop=System.getProperty("user.dir").contains("C:\\Users\\");
    public static final Integer defaultReceivePort=33000;
    public static final String networkStub="192.168.";
    public static final String networkPrefix="192.168.0.";
    public static final String testingPrefix="192.168.1.";
    public static final String logServerHost;
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
            logServerHost=isRaysPc?testingHost:networkHost;
        } else {
            networkHost="localhost"; // nothing but trouble :(
            testingHost="localhost";
            //logServerHost="192.168.1.2";
            logServerHost="192.168.0.102"; // or the laptop's 192.68.0
        }
    }
    static {
        p("user dir: "+System.getProperty("user.dir"));
        p("network host: "+networkHost);
        p("testing host: "+testingHost);
        p("log serverHost host: "+logServerHost);
    }
    public static final Map<Object,Instance> instances=new LinkedHashMap<>();
    public static final Map<String,SocketHandler> logServerHosts=new LinkedHashMap<>();
    static {
        logServerHosts.put("192.168.1.2",null); // static ip on my pc
        logServerHosts.put("192.168.0.101",null); // my pc today
        logServerHosts.put("192.168.0.100",null); // laptop today
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

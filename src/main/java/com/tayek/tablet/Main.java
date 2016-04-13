package com.tayek.tablet;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import com.tayek.*;
import com.tayek.io.LogServer;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.Main;
import com.tayek.tablet.Message.Factory;
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
                        g0.put(aTabletId(tabletId),new Required(aTabletId(tabletId),tabletNetworkPrefix+(10+tabletId),defaultReceivePort));
                } else {
                    g0.put(aTabletId(1),new Required("fire 1",tabletNetworkPrefix+21,defaultReceivePort));
                    g0.put(aTabletId(2),new Required("fire 2",tabletNetworkPrefix+22,defaultReceivePort));
                    g0.put(aTabletId(3),new Required("nexus 7",tabletNetworkPrefix+70,defaultReceivePort));
                    g0.put(aTabletId(4),new Required("pc-4",tabletNetworkPrefix+100,defaultReceivePort+4));
                    g0.put(aTabletId(5),new Required("pc-5",tabletNetworkPrefix+100,defaultReceivePort+5));
                    g0.put(aTabletId(6),new Required("azpen",tabletNetworkPrefix+33,defaultReceivePort));
                    g0.put(aTabletId(7),new Required("at&t",tabletNetworkPrefix+77,defaultReceivePort));
                    g0.put(aTabletId(8),new Required("conrad",tabletNetworkPrefix+88,defaultReceivePort));
                    //g0.put(99,new Info("nexus 4",99,IO.defaultReceivePort)));
                }
                groups.put("g0",g0);
                g2.put("pc-4",new Required("pc-4",testingHost,defaultReceivePort+4));
                g2.put("pc-5",new Required("pc-5",testingHost,defaultReceivePort+5));
                groups.put("g2",g2);
                //g1each.put(3,new Info("nexus 7",Main.networkPrefix+70,Main.defaultReceivePort));
                // two fake tablets on pc, but on different networks.
                // the 100 is dhcp'ed, so it may change once in a while.
                g1each.put("pc-4",new Required("pc-4",defaultHost,defaultReceivePort+4));
                g1each.put("pc-5",new Required("pc-5",testingHost,defaultReceivePort+4));
                groups.put("g1each",g1each);
            }
            private final Map<String,Required> g2=new TreeMap<>();
            private final Map<String,Required> g0=new TreeMap<>();
            private final Map<String,Required> g1each=new TreeMap<>();
            public final Map<String,Map<String,Required>> groups=new TreeMap<>();
            // hack, change the above before calling new Groups!
        }
        public static String aTabletId(Integer tabletId) {
            return "T"+tabletId;
        }
        public Stuff() {
            this(0,Collections.<String,Required> emptyMap(),(Model)null);
        }
        public Stuff(int id,Map<String,Required> idToInfo,Model model) {
            this(id,idToInfo,model,++serialNumbers);
        }
        private Stuff(int groupId,Map<String,Required> idToInfo,Model model,int serialNumber) {
            this.serialNumber=serialNumber;
            this.groupIdx=groupId;
            this.idToRequired.putAll(idToInfo);
            // these may all end up with the same ifo, hence histories!
            int n=idToInfo.size();
            executorService=Executors.newFixedThreadPool(4*n+2);
            canceller=Executors.newScheduledThreadPool(4*n+2);
            prototype=model;
        }
        private Stuff(Stuff stuff,int serialNumber) {
            this.serialNumber=stuff.serialNumber;
            this.groupIdx=stuff.groupIdx;
            if(stuff.idToRequired!=null) this.idToRequired.putAll(stuff.idToRequired);
            int n=stuff.idToRequired.size();
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
        }
        public Set<String> keys() {
            return idToRequired!=null?idToRequired.keySet():null;
        }
        public Required required(String id) {
            return id==null?null:idToRequired!=null?idToRequired.get(id):null;
        }
        public InetSocketAddress socketAddress(String destinationTabletId) {
            Required required=required(destinationTabletId);
            if(required!=null) return new InetSocketAddress(required.host,required.service);
            return null;
        }
        public String getTabletIdFromInetAddress(InetAddress inetAddress,Integer service) {
            for(String i:keys()) // fragile!
                if(required(i)!=null) if(inetAddress.getHostAddress().equals(required(i).host)&&(service==null||service.equals(required(i).service))) return i;
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
            Required required;
            for(String id:keys())
                if((required=required(id))!=null) {
                    sb.append("\nfor: "+id+": ");
                    sb.append(required.histories());
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
            sb.append("\nconfig: "+config());
            for(String i:keys()) {
                Histories histories=required(i).histories();
                if(histories.anyFailures()||histories.senderHistory.history.attempts()!=0) sb.append("\n\tfor "+id+" to: "+i+", history: "+histories);
                else sb.append("\n\tfrom "+id+" to: "+i+", history: no failures.");
            }
            Map<Object,Float> failures=new LinkedHashMap<>();
            double sends=0;
            for(String i:keys()) {
                Histories histories=required(i).histories();
                sends+=histories.senderHistory.history.attempts();
                failures.put(i,(float)(1.*histories.senderHistory.history.failures()/histories.senderHistory.history.attempts()));
                sb.append("\nsummary from: "+id+", to:"+i+" send failure rate: "+histories.senderHistory.history.failures()+"/"+histories.senderHistory.history.attempts()+"="
                        +(1.*histories.senderHistory.history.failures()/histories.senderHistory.history.attempts()));
            }
            sb.append("\n failure rates from: "+id+" to others: "+failures);
            failures.clear();
            for(String i:keys()) {
                Histories hireceiverHistory=required(i).histories();
                sends+=hireceiverHistory.senderHistory.retries.attempts();
                failures.put(i,(float)(1.*hireceiverHistory.senderHistory.retries.failures()/hireceiverHistory.senderHistory.retries.attempts()));
                sb.append("\nsummary from: "+id+", to:"+i+" retry failure rate: "+hireceiverHistory.senderHistory.retries.failures()+"/"+hireceiverHistory.senderHistory.retries.attempts()+"="
                        +(1.*hireceiverHistory.senderHistory.retries.failures()/hireceiverHistory.senderHistory.retries.attempts()));
            }
            sb.append("\n retry failure rates from: "+id+" to others: "+failures);
            int big=2*Thread.activeCount();
            Thread[] threads=new Thread[big];
            Thread.enumerate(threads);
            for(Thread thread:threads)
                if(thread!=null) sb.append("\n"+thread.toString());
            sb.append("\nend of report histories from: "+id+" ------------------------------------");
            return sb.toString();
        }
        String config() {
            return "connect: "+connectTimeout+", send: "+sendTimeout+", use: "+useExecutorService+", cancel: "+runCanceller+", replying: "+replying;
        }
        public final int groupIdx;
        private final Model prototype;
        public final Integer serialNumber;
        private final Map<String,Required> idToRequired=new TreeMap<>();
        public final Factory messages=Message.instance.create(new Single<Integer>(0));
        public final ScheduledExecutorService canceller;
        public final ExecutorService executorService;
        // split these into an options class and a constants class?
        public boolean useExecutorService;
        public boolean waitForSendCallable=true;
        public boolean runCanceller;
        public boolean replying;
        public Integer connectTimeout=defaultConnectTimeout; // set by tablet
        public Integer sendTimeout=defaultSendTimeout; // set by tablet
        private static int serialNumbers;
        public static Integer defaultConnectTimeout=500; // 40;
        public static Integer defaultSendTimeout=defaultConnectTimeout+50; // 60;
        public static Integer defaultDriveWait=200; // 100;
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
        public final Factory messages=Message.instance.create(new Single<Integer>(0));
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
            }
        }.run();
    }
    // the above is used by tests a lot.
    // maybe have the tests use something else, so as to not interfere with real tablets when testing?
    public static final Map<Object,Instance> instances=new LinkedHashMap<>();
}

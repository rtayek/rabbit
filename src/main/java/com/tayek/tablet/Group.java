package com.tayek.tablet;
import static com.tayek.tablet.io.IO.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import com.tayek.tablet.Group.Info;
import com.tayek.tablet.Messages.*;
import com.tayek.tablet.Receiver.Model;
import com.tayek.tablet.Sender.Client;
import com.tayek.tablet.Tablet.*;
import com.tayek.tablet.io.*;
public class Group implements Cloneable {
    public static class Groups {
        public Groups() {
            // this needs to take testing prefix
            boolean old=false;
            if(!old) {
                int tablets=6;
                for(int tabletId=1;tabletId<=6;tabletId++)
                    g0.put(tabletId,new Info("T"+(char)('0'+tabletId),Main.networkPrefix+(10+tabletId),Main.defaultReceivePort));
            } else {
                g0.put(1,new Info("fire 1",Main.networkPrefix+21,Main.defaultReceivePort));
                g0.put(2,new Info("fire 2",Main.networkPrefix+22,Main.defaultReceivePort));
                g0.put(3,new Info("nexus 7",Main.networkPrefix+70,Main.defaultReceivePort));
                g0.put(4,new Info("pc-4",Main.networkPrefix+100,Main.defaultReceivePort+4));
                g0.put(5,new Info("pc-5",Main.networkPrefix+100,Main.defaultReceivePort+5));
                g0.put(6,new Info("azpen",Main.networkPrefix+33,Main.defaultReceivePort));
                g0.put(7,new Info("at&t",Main.networkPrefix+77,Main.defaultReceivePort));
                g0.put(8,new Info("conrad",Main.networkPrefix+88,Main.defaultReceivePort));
                //g0.put(99,new Info("nexus 4",99,IO.defaultReceivePort)));
            }
            groups.put("g0",g0);
            g2.put(4,new Info("pc-4",Main.testingHost,Main.defaultReceivePort+4));
            g2.put(5,new Info("pc-5",Main.testingHost,Main.defaultReceivePort+5));
            groups.put("g2",g2);
            //g1each.put(3,new Info("nexus 7",Main.networkPrefix+70,Main.defaultReceivePort));
            // two fake tablets on pc, but on different networks.
            // the 100 is dhcp'ed, so it may change once in a while.
            g1each.put(4,new Info("pc-4",Main.networkHost,Main.defaultReceivePort+4));
            g1each.put(5,new Info("pc-5",Main.testingHost,Main.defaultReceivePort+4));
            groups.put("g1each",g1each);
        }
        private final Map<Integer,Info> g2=new TreeMap<>();
        private final Map<Integer,Info> g0=new TreeMap<>();
        private final Map<Integer,Info> g1each=new TreeMap<>();
        public final Map<String,Map<Integer,Info>> groups=new TreeMap<>();
        // hack, change the above before calling new Groups!
    }
    public static class Info {
        public Info(String name,String host,int port) {
            this.name=name;
            this.host=host;
            this.service=port;
            history=new Histories();
        }
        @Override public String toString() {
            return name+" "+host+" "+service;
        }
        public final String name;
        public final String host;
        public final Integer service;
        public final Histories history;
        // add colors and other stuff;
    }
    public Group(Integer groupId,Map<Integer,Info> info,Model model,boolean replying) {
        this(groupId,info,++serialNumbers,model,replying);
    }
    private Group(Integer groupId,Map<Integer,Info> info,Integer serialNumber,Model model,boolean replying) {
        this.serialNumber=serialNumber;
        this.groupId=groupId;
        this.info=new TreeMap<>();
        this.info.putAll(info);
        this.replying=replying;
        int n=info().size()+4;
        executorService=Executors.newFixedThreadPool(4*n+2);
        canceller=Executors.newScheduledThreadPool(4*n+2);
        prototype=model;
        messages=new Messages();
    }
    @Override public Group clone() { // for testing
        // maybe for more than testing?
        return new Group(groupId,info,serialNumber,getModelClone(),replying);
    }
    private void send(Tablet tablet,final Message message,Integer destinationTabletId,int timeout) {
        InetSocketAddress inetSocketAddress=socketAddress(destinationTabletId);
        if(message.type.equals(Type.ack)) l.warning("sending act to: "+inetSocketAddress);
        Client client=new Client(inetSocketAddress,replying,timeout);
        //if(!destinationTabletId.equals(tabletId()))
        Histories history=info(destinationTabletId).history;
        l.info("tablet "+tablet+" sending: "+message+" to tablet "+name(destinationTabletId));
        try {
            boolean ok=client.send(message,history.client);
            if(!ok) ; //l.warning("tablet: "+tabletId+", send to: "+inetSocketAddress+" failed!");
            if(false&&!ok) {
                p("trying to send again to: "+inetSocketAddress);
                ok=client.send(message,history.client);
                if(ok) p("worked the second time send to: "+inetSocketAddress);
                else p("second time failed sending to: "+inetSocketAddress);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public class SendCallable implements Callable<Void> {
        public SendCallable(Tablet tablet,Message message,Integer destinationTabletId,int timeout) {
            this.tablet=tablet;
            this.message=message;
            this.destinationTabletId=destinationTabletId;
            this.timeout=timeout;
        }
        @Override public Void call() throws Exception {
            Thread.currentThread().setName(getClass().getSimpleName()+", tablet: "+tablet.tabletId()+" send: #"+message.number+", to: "+destinationTabletId);
            l.fine("call send to: "+destinationTabletId);
            if(message.type.equals(Type.ack)) l.warning(tablet+", sending ack: "+message+", to: "+destinationTabletId);
            Group.this.send(tablet,message,destinationTabletId,timeout);
            return null;
        }
        private final Tablet tablet;
        private final Message message;
        private final Integer destinationTabletId;
        private final Integer timeout;
    }
    public <T> Future<T> executeTaskAndCancelIfItTakesTooLong(final Callable<T> callable,final long timeoutMS,ScheduledExecutorService canceller) {
        final Future<T> future=executorService.submit(callable);
        // awk this makes another thread!
        if(runCanceller) canceller.schedule(new Callable<Void>() {
            public Void call() {
                if(!future.isDone()) {
                    l.warning("future: "+future+", callable: "+callable+" task is not finished after: "+timeoutMS);
                    future.cancel(true);
                }
                return null;
            }
        },timeoutMS,TimeUnit.MILLISECONDS);
        return future;
    }
    private class BroadcastCallable implements Callable<Void> {
        BroadcastCallable(Tablet tablet,Message message) {
            this.tablet=tablet;
            this.message=message;
        }
        @Override public Void call() throws Exception {
            Thread.currentThread().setName(getClass().getName()+" tablet: "+tablet.tabletId()+" broadcast");
            l.info("broadcast: "+message);
            for(Integer destinationTabletId:tabletIds()) {
                l.info("broadcasting to: "+destinationTabletId);
                // what should this wait really be?
                executeTaskAndCancelIfItTakesTooLong(new SendCallable(tablet,message,destinationTabletId,connectTimeout),tablet.group.sendTimeout,canceller);
            }
            return null;
        }
        final Tablet tablet;
        final Message message;
    }
    public synchronized void broadcast(Tablet tablet,final Message message,int unused) {
        // does this wait?
        // false seemed to work, but still got a lot of timeouts
        // move this to group?
        l.info("broadcasting: "+message);
        if(false) executorService.submit(new BroadcastCallable(tablet,message));
        else for(Integer destinationTabletId:tabletIds())
            executeTaskAndCancelIfItTakesTooLong(new SendCallable(tablet,message,destinationTabletId,connectTimeout),tablet.group.sendTimeout,canceller);
        // no, it does not wait!
        // we only need this to be on a separate thread if it waits
        // and currently, it does not wait.
        Histories histories=info(tablet.tabletId()).history;
        Histories.ClientHistory clientHistory=histories.client;
        if(reportPeriod>0&&histories.anyAttempts()&&(clientHistory.client.attempts()%reportPeriod==0||histories.server.server.attempts()%reportPeriod==0))
            if(histories.anyFailures()) l.warning("histories from client: "+histories(tablet));
        else l.warning("no failures in client (really?)&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
    }
    public Tablet create(int tabletId) {
        return create(this,tabletId);
    }
    public static Tablet create(Group group,int tabletId) {
        return new Tablet(group.clone(),tabletId);
    }
    public Set<Tablet> create() { // mostly for testing
        Set<Tablet> tablets=new LinkedHashSet<>();
        for(int tabletId:tabletIds())
            tablets.add(create(tabletId));
        return tablets;
    }
    public static Set<Tablet> createGroupAndstartTablets(Map<Integer,Info> info) {
        Set<Tablet> tablets=new LinkedHashSet<>();
        Group group=new Group(1,info,Model.mark1,false);
        for(Integer tabletId:group.tabletIds()) {
            Tablet tablet=new Tablet(group.clone(),tabletId);
            tablets.add(tablet);
            tablet.model.addObserver(new AudioObserver(tablet.model)); // maybe not if testing?
            tablet.startListening();
        }
        return tablets;
    }
    public Tablet getTablet(InetAddress inetAddress,Integer service) {
        p("host address: "+inetAddress.getHostAddress());
        for(int i:tabletIds()) {
            p("info: "+info(i));
            if(info(i)!=null) if(inetAddress.getHostAddress().equals(info(i).host)&&(service==null||service.equals(info(i).service))) return new Tablet(this,i);
            else p("info("+i+") is not us.");
            else l.severe("info("+i+") is null!");
        }
        return null;
    }
    public InetSocketAddress socketAddress(int destinationTabletId) {
        Info info=info(destinationTabletId);
        if(info!=null) return new InetSocketAddress(info.host,info.service);
        return null;
    }
    public Model getModelClone() {
        return prototype.clone();
    }
    public Set<Integer> tabletIds() {
        synchronized(info) {
            return info.keySet();
        }
    }
    public Info info(int tabletId) {
        synchronized(info) {
            return info.get(tabletId);
        }
    }
    public String name(int tabletId) { // threw npe on nexus 4?
        Info info=info(tabletId);
        return info!=null?info(tabletId).name:"null";
    }
    public void print(Integer tabletId) {
        l.info("group: "+groupId+"("+serialNumber+"):"+tabletId);
        Map<Integer,Info> copy=info();
        for(int i:copy.keySet())
            l.info("\t"+i+": "+copy.get(i));
    }
    public String histories(Tablet tablet/* may be null*/) {
        // need to find the tablet to get the right history?
        // looks like the server history only makes sense for the driving tablet when testing.
        // so maybe check for that when driving and omit the print.
        StringBuffer sb=new StringBuffer();
        sb.append("histories from tablet: "+tablet+" ------------------------------------");
        for(int i:info.keySet()) {
            Histories history=info.get(i).history;
            if(history.anyFailures()||history.client.client.attempts()!=0) sb.append("\nfor group ("+serialNumber+") tablet: "+i+" history: "+history);
            else sb.append("\nfor group ("+serialNumber+") tablet: "+i+" history: no failures.");
        }
        Map<Integer,Float> failures=new TreeMap<>();
        double sends=0;
        for(int i:info.keySet()) {
            Histories history=info.get(i).history;
            sends+=history.client.client.attempts();
            failures.put(i,(float)(1.*history.client.client.failures()/history.client.client.attempts()));
            sb.append("\nhistory for tablet: "+i+", group ("+serialNumber+") tablet: "+i+" send failure rate: "+history.client.client.failures()+"/"+history.client.client.attempts()+"="
                    +(1.*history.client.client.failures()/history.client.client.attempts()));
        }
        sb.append("\ngroup ("+serialNumber+") send ("+(sends/info.size())+") failure rates: "+failures);
        int big=2*Thread.activeCount();
        Thread[] threads=new Thread[big];
        Thread.enumerate(threads);
        for(Thread thread:threads)
            if(thread!=null) sb.append("\n"+thread.toString());
        sb.append("\nend of histories from tablet: "+tablet+" ------------------------------------");
        return sb.toString();
    }
    public Map<Integer,Info> info() {
        Map<Integer,Info> copy=new TreeMap<>();
        synchronized(info) { // may not need to sync anymore?
            copy.putAll(info);
        }
        return Collections.unmodifiableMap(copy);
    }
    public static Message random(Tablet tablet) {
        int buttonId=random.nextInt(tablet.model.buttons)+1;
        return tablet.group.messages.normal(tablet.group.groupId,tablet.tabletId(),buttonId,tablet.model);
    }
    public static Message randomToggle(Tablet tablet) {
        int buttonId=random.nextInt(tablet.model.buttons)+1;
        //boolean state=!tablet.model.state(buttonId);
        //String string=tablet.model.toCharacters();
        //int index=buttonId-1;
        //Character c=Model.toCharacter(state);
        //String newString=string.substring(0,index)+c+string.substring(index+1,string.length());
        return tablet.group.messages.normal(tablet.group.groupId,tablet.tabletId(),buttonId,tablet.model);
        //return new Message(Type.normal,tablet.group.groupId,tablet.tabletId(),buttonId,newString);
    }
    public static final Random random=new Random();
    @Override public String toString() {
        return "group: "+groupId+"("+serialNumber+"): "+info().keySet();
    }
    private Model prototype;
    private final Map<Integer,Info> info; // usually 1-n
    public final Messages messages;
    public final Integer serialNumber;
    public final Integer groupId;
    public final boolean replying;
    public final ExecutorService executorService;
    public final ScheduledExecutorService canceller;
    public Toaster toaster;
    public Integer connectTimeout;
    public Integer sendTimeout;
    public Integer reportPeriod=defaultReportPeriod;
    public static Integer defaultReportPeriod=100;
    public static Integer defaultConnectTimeout=200; // 40;
    public static Integer defaultSendTimeout=250; // 60;
    public static Integer driveWait=50; // 100;
    public static boolean runCanceller=false;
    private static int serialNumbers;
    public static final Integer defaultButtons=11;
    public static final Integer defaultTablets=defaultButtons+2;
    public static final Integer maxTablets=50;
    public static final Integer minTabletId=1,maxTabletId=99;
    public static final Integer maxButtons=20;
    public final Logger l=Logger.getLogger(getClass().getName());
}

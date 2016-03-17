package com.tayek.tablet;
import static com.tayek.tablet.io.IO.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import com.tayek.tablet.Group.Info;
import com.tayek.tablet.Messages.*;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.Tablet.*;
import com.tayek.tablet.io.*;
import com.tayek.tablet.io.Sender.Client;
import com.tayek.utilities.Et;
import static com.tayek.utilities.Utility.*;
public class Group implements Cloneable { // power set?
    public static class Groups {
        public Groups() {
            // this needs to take testing prefix
            // really? for socket logging or what?
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
        private final Map<Object,Info> g2=new LinkedHashMap<>();
        private final Map<Object,Info> g0=new LinkedHashMap<>();
        private final Map<Object,Info> g1each=new LinkedHashMap<>();
        public final Map<String,Map<Object,Info>> groups=new LinkedHashMap<>();
        // hack, change the above before calling new Groups!
    }
    public static class Info {
        public Info(Object iD,String host,int port) {
            this.iD=iD;
            this.host=host;
            this.service=port;
            history=new Histories();
        }
        @Override public String toString() {
            return iD+" "+host+" "+service;
        }
        public final Object iD;
        public final String host;
        public final Integer service;
        public final Histories history;
        // add colors and other stuff;
    }
    public Group(Integer groupId,Map<Object,Info> info,Model model,boolean replying) {
        this(groupId,info,++serialNumbers,model,replying);
    }
    private Group(Integer groupId,Map<Object,Info> info,Integer serialNumber,Model model,boolean replying) {
        this.serialNumber=serialNumber;
        this.groupId=groupId;
        this.info=new LinkedHashMap<>();
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
    private void send(Tablet tablet,final Message message,Object destinationTabletId,int timeout) {
        InetSocketAddress inetSocketAddress=socketAddress(destinationTabletId);
        if(message.type.equals(Type.ack)) l.warning("sending act to: "+inetSocketAddress);
        Client client=new Client(inetSocketAddress,replying,timeout);
        //if(!destinationTabletId.equals(tabletId()))
        Histories history=info(destinationTabletId).history;
        l.info("tablet "+tablet+" sending: "+message+" to tablet "+iD(destinationTabletId));
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
        public SendCallable(Tablet tablet,Message message,Object destinationTabletId,int timeout) {
            this.tablet=tablet;
            this.message=message;
            this.destinationTabletId=destinationTabletId;
            this.timeout=timeout;
        }
        @Override public Void call() throws Exception {
            Thread.currentThread().setName(getClass().getSimpleName()+", tablet: "+tablet.tabletId()+" send: #"+message.number+", to: "+destinationTabletId);
            l.fine("call send to: "+destinationTabletId);
            if(message.type.equals(Type.ack)) l.warning(tablet+", sending ack: "+message+", to: "+destinationTabletId);
            sendCalled=true;
            Group.this.send(tablet,message,destinationTabletId,timeout);
            return null;
        }
        public boolean sendCalled;
        private final Tablet tablet;
        private final Message message;
        private final Object destinationTabletId;
        private final Integer timeout;
    }
    public Future executeTaskAndCancelIfItTakesTooLong2(final SendCallable callable,final long timeoutMS,ScheduledExecutorService canceller) {
        final Future<Void> future=executorService.submit(callable);
        // awk this makes another thread!
        Et et=new Et();
        if(waitForSendCallable){
        while(!callable.sendCalled)
            Thread.yield();
        l.warning("send called took: "+et);}
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
            for(Object destinationTabletId:tabletIds()) {
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
        else for(Object destinationTabletId:tabletIds())
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
    public Tablet create(Object tabletId) {
        return create(this,tabletId);
    }
    public static Tablet create(Group group,Object tabletId) {
        return new Tablet(group.clone(),tabletId);
    }
    public Set<Tablet> create() { // mostly for testing
        Set<Tablet> tablets=new LinkedHashSet<>();
        for(Object tabletId:tabletIds())
            tablets.add(create(tabletId));
        return tablets;
    }
    public static Set<Tablet> createGroupAndstartTablets(Map<Object,Info> info) {
        Set<Tablet> tablets=new LinkedHashSet<>();
        Group group=new Group(1,info,Model.mark1,false);
        for(Object tabletId:group.tabletIds()) {
            Tablet tablet=new Tablet(group.clone(),tabletId);
            tablets.add(tablet);
            tablet.model.addObserver(new AudioObserver(tablet.model)); // maybe not if testing?
            tablet.startListening();
        }
        return tablets;
    }
    public Tablet getTablet(InetAddress inetAddress,Integer service) {
        p("host address: "+inetAddress.getHostAddress());
        for(Object i:tabletIds()) {
            p("info: "+info(i));
            if(info(i)!=null) if(inetAddress.getHostAddress().equals(info(i).host)&&(service==null||service.equals(info(i).service))) return new Tablet(this,i);
            else p("info("+i+") is not us.");
            else l.severe("info("+i+") is null!");
        }
        return null;
    }
    public InetSocketAddress socketAddress(Object destinationTabletId) {
        Info info=info(destinationTabletId);
        if(info!=null) return new InetSocketAddress(info.host,info.service);
        return null;
    }
    public Model getModelClone() {
        return prototype.clone();
    }
    public Set<Object> tabletIds() {
        synchronized(info) {
            return info.keySet();
        }
    }
    public Info info(Object tabletId) {
        synchronized(info) {
            return info.get(tabletId);
        }
    }
    public Object iD(Object iD) { // threw npe on nexus 4?
        Info info=info(iD);
        return info!=null?info(iD).iD:"null";
    }
    public void print(Integer tabletId) {
        l.info("group: "+groupId+"("+serialNumber+"):"+tabletId);
        Map<Object,Info> copy=info();
        for(Object i:copy.keySet())
            l.info("\t"+i+": "+copy.get(i));
    }
    public String histories(Tablet tablet/* may be null*/) {
        // need to find the tablet to get the right history?
        // looks like the server history only makes sense for the driving tablet when testing.
        // so maybe check for that when driving and omit the print.
        p("method: "+method()+" &&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        StringBuffer sb=new StringBuffer();
        sb.append("histories from tablet: "+tablet+" ------------------------------------");
        for(Object i:info.keySet()) {
            Histories history=info.get(i).history;
            if(history.anyFailures()||history.client.client.attempts()!=0) sb.append("\nfor group ("+serialNumber+") tablet: "+i+" history: "+history);
            else sb.append("\nfor group ("+serialNumber+") tablet: "+i+" history: no failures.");
        }
        Map<Object,Float> failures=new LinkedHashMap<>();
        double sends=0;
        for(Object i:info.keySet()) {
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
    public Map<Object,Info> info() {
        Map<Object,Info> copy=new LinkedHashMap<>();
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
    private final Map<Object,Info> info; // usually 1-n
    public final Messages messages;
    public final Integer serialNumber;
    public final Integer groupId;
    public final boolean replying;
    public final ExecutorService executorService;
    public final ScheduledExecutorService canceller;
    public Toaster toaster;
    public Integer connectTimeout;
    public Integer sendTimeout;
    public Integer reportPeriod=Histories.defaultReportPeriod;
    public boolean waitForSendCallable;
    public boolean runCanceller;
    private static int serialNumbers;
    public static final Integer defaultButtons=11;
    public static final Integer defaultTablets=defaultButtons+2;
    public static final Integer maxTablets=50;
    public static final Integer minTabletId=1,maxTabletId=99;
    public static final Integer maxButtons=20;
    public final Logger l=Logger.getLogger(getClass().getName());
}

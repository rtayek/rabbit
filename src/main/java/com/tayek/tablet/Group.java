package com.tayek.tablet;
import static com.tayek.io.IO.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import com.tayek.*;
import com.tayek.Sender.Client;
import com.tayek.Sender.Client.SendCallable;
import com.tayek.Tablet.Factory.FactoryImpl.TabletABC;
import com.tayek.io.Audio.AudioObserver;
import com.tayek.Tablet.Config;
import com.tayek.tablet.Group.TabletImpl2;
import com.tayek.tablet.Message.*;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.io.Server;
import com.tayek.utilities.*;
import static com.tayek.utilities.Utility.*;
public class Group implements Cloneable { // maybe this belongs in sender?
    public static class Groups {
        public static String add(String host,Integer service,Map<String,Required> map) {
            Required required=new Required(host,service);
            map.put(required.id,required);
            return required.id;
        }
        public Groups() {
            // this needs to take testing prefix
            // really? for socket logging or what?
            boolean old=false;
            if(!old) {
                int tablets=10;
                String host,id;
                for(int i=1;i<=tablets;i++) {
                    host=tabletRouterPrefix+(10+i);
                    add(host,defaultReceivePort,g0);
                }
                host=raysPcOnTabletNetworkToday;
                add(host,defaultReceivePort,g0);
                host=laptopToday;
                add(host,defaultReceivePort,g0);
                groups.put("g0",g0);
            } else {
                g0.put(aTabletId(1),new Required("fire 1",tabletRouterPrefix+21,defaultReceivePort));
                g0.put(aTabletId(2),new Required("fire 2",tabletRouterPrefix+22,defaultReceivePort));
                g0.put(aTabletId(3),new Required("nexus 7",tabletRouterPrefix+70,defaultReceivePort));
                g0.put(aTabletId(4),new Required("pc-4",tabletRouterPrefix+100,defaultReceivePort+4));
                g0.put(aTabletId(5),new Required("pc-5",tabletRouterPrefix+100,defaultReceivePort+5));
                g0.put(aTabletId(6),new Required("azpen",tabletRouterPrefix+33,defaultReceivePort));
                g0.put(aTabletId(7),new Required("at&t",tabletRouterPrefix+77,defaultReceivePort));
                g0.put(aTabletId(8),new Required("conrad",tabletRouterPrefix+88,defaultReceivePort));
                //g0.put(99,new Info("nexus 4",99,IO.defaultReceivePort)));
            }
            groups.put("g0",g0);
            add(testingHost,defaultReceivePort+4,g2OnPc);
            add(testingHost,defaultReceivePort+5,g2OnPc);
            groups.put("g2OnPc",g2OnPc);
            add(defaultHost,defaultReceivePort+4,g2OnRouter);
            add(defaultHost,defaultReceivePort+5,g2OnRouter);
            groups.put("g2OnRouter",g2OnRouter);
            //g1each.put(3,new Info("nexus 7",Main.networkPrefix+70,Main.defaultReceivePort));
            // two fake tablets on pc, but on different networks.
            // the 100 is dhcp'ed, so it may change once in a while.
            add(defaultHost,defaultReceivePort+4,g1each);
            add(testingHost,defaultReceivePort+5,g1each);
            groups.put("g1each",g1each);
        }
        private final Map<String,Required> g2OnPc=new TreeMap<>();
        private final Map<String,Required> g2OnRouter=new TreeMap<>();
        private final Map<String,Required> g0=new TreeMap<>();
        private final Map<String,Required> g1each=new TreeMap<>();
        public final Map<String,Map<String,Required>> groups=new TreeMap<>();
        // hack, change the above before calling new Groups!
    }
    public static class ReceiverImpl implements Receiver {
        ReceiverImpl(Object id,TabletImpl2 tablet,Set<? extends Object> ids,MessageReceiver messageReceiver) {
            // get rid of tablet
            // use a set of Id's
            this.id=id;
            this.tablet=tablet;
            this.messageReceiver=messageReceiver;
            receiverHistory=tablet.histories().receiverHistory;
            if(ids!=null) for(Object x:ids)
                lastMessageNumbers.put(x,null);
            l.info("initial lastMessageNumbers: "+lastMessageNumbers);
            this.messageFactory=tablet.messageFactory(); // maybe only needs a from?
        }
        @Override public void receive(Object message) {
            if(Message.empty.equals(message)) l.severe("empty message received by: "+id);
            else processMessageObject(id,message.toString());
        }
        void checkForMissing(Message message) {
            if(!lastMessageNumbers.containsKey(message.from())) l.severe("message from foreign tablet: "+message);
            Integer lastMessageNumber=lastMessageNumbers.get(message.from());
            boolean ignoreMissingOrOutOfOrder=true;
            // move this to config!
            if(lastMessageNumber!=null) {
                l.info("last: "+lastMessageNumber+", current: "+message.number());
                if(message.number()==lastMessageNumber+1) receiverHistory.missing.success();
                else {
                    l.warning(id+": got #"+receiverHistory.history.attempts()+", expected number: "+(lastMessageNumber+1)+" from: "+message.from()+", but got: "+message.number());
                    if(message.number()<lastMessageNumber+1) {
                        l.warning("#"+receiverHistory.history.attempts()+", out of order!");
                        if(ignoreMissingOrOutOfOrder) receiverHistory.missing.success(); // count first as success
                        else receiverHistory.missing.failure("out of order: "+(message.number()-(lastMessageNumber+1)));
                    } else {
                        l.warning("#"+receiverHistory.history.attempts()+", missing or out of order, expected number: "+(lastMessageNumber+1)+", but got: "+message.number());
                        if(ignoreMissingOrOutOfOrder) receiverHistory.missing.success(); // count first as success
                        else receiverHistory.missing.failure("missing: "+(message.number()-(lastMessageNumber+1)));
                    }
                }
            } else {
                // maybe missed a whole bunch!
                if(message.number()!=1) l.severe("first message is: #"+message.number()+", "+message);
                receiverHistory.missing.success(); // count first as success
            }
            lastMessageNumbers.put(message.from(),message.number());
            l.info(id+" put last #"+message.number()+" into: "+message.from());
            if(receiverHistory.missed.isDuplicate(message.number())) {
                l.severe(id+": missing detected a duplicate!: "+receiverHistory.missed);
            }
            receiverHistory.missed.adjust(message.number());
        }
        // move this to messages?
        private void processMessageObject(Object id,String string) {
            l.info("enter process message: "+string+", lastMessageNumbers: "+lastMessageNumbers);
            if(string!=null&&!string.isEmpty()) {
                Message message=messageFactory.from(string);
                //checkForMissing(message); // makse no sense in this context
                // since we get messages from all the tablets 
                l.fine(id+" received: "+message+" at: "+System.currentTimeMillis());
                switch(message.type()) {
                    case ping:
                        l.warning("i got a ping!");
                        Message ack=messageFactory.other(Type.ack,tablet.group().groupId,tablet.tabletId());
                        ackEt=new Et();
                        InetSocketAddress inetSocketAddress=tablet.group().socketAddress(message.from());
                        new Thread(new SendCallable(tablet.tabletId(),ack,message.from(),tablet.group().required(message.from()).histories(),inetSocketAddress),"ack").start();
                        break;
                    case ack:
                        p(id+", received ack: "+message+", after: "+ackEt);
                        l.warning(id+", received ack: "+message+", after: "+ackEt);
                        ackEt=null;
                        break;
                    case rolloverLogNow:
                        p(id+", received rollover: "+message);
                        l.warning(id+", received rollover: "+message);
                        break;
                    case drive:
                        tablet.driveInThread(true);
                        break;
                    case stopDriving:
                        tablet.stopDriving=true;
                        break;
                    case forever:
                        tablet.foreverInThread();
                        break;
                    default:
                        if(!message.from().equals(id)) tablet.model().receive(message);
                        else l.fine("discarding message from self");
                }
            }
            l.info("exit lastMessageNumbers: "+lastMessageNumbers);
        }
        final TabletImpl2 tablet; // try to remove this
        // maybe just use group?
        final Object id;
        final MessageReceiver messageReceiver;
        final Histories.ReceiverHistory receiverHistory; // change to histories?
        
        Et ackEt;
        private final Factory messageFactory;
        private final Map<Object,Integer> lastMessageNumbers=new LinkedHashMap<>();
    }
    public static class TabletImpl2 extends TabletABC {
        // get group out of constructor!
        // or fix all of the callers!
        public TabletImpl2(Group group,String tabletId,Model model) {
            super(group,tabletId,model,group.required(tabletId).histories());
            messageFactory=Message.instance.create(required.host,required.service,new Single<Integer>(0));
            int n=group.keys().size();
            model().histories=histories();
        }
        public Group group() {
            return group;
        }
        // strange dream about two ways to do something
        // (kike get host and service?)
        // about how to see which is faster or better?
        // do them on seperate threads?
        @Override public void broadcast(Object message) {
            // maybe pass in a histories, so we can see what happened
            // to all of the tablets.
            // then add it in?
            l.info("broadcasting: "+message);
            for(String destinationTabletId:group.keys()) {
                InetSocketAddress inetSocketAddress=group.socketAddress(destinationTabletId);
                SendCallable sendCallable=new SendCallable(tabletId(),message,destinationTabletId,group.required(destinationTabletId).histories(),inetSocketAddress);
                new Thread(new SendCallable(tabletId(),message,destinationTabletId,group.required(destinationTabletId).histories(),inetSocketAddress)).start();
                try {
                    Thread.sleep(10); // to out of order
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Histories.SenderHistory clientHistory=histories.senderHistory;
            // maybe sleep for a while here?
            if(histories.reportPeriod>0&&histories.anyAttempts()&&clientHistory.history.attempts()%histories.reportPeriod==0) l.warning("histories from client: "+histories());
            if(histories.reportPeriod>0&&histories.anyAttempts()&&clientHistory.history.attempts()%(10*histories.reportPeriod)==0) l.warning("report histories from client: "+report(tabletId()));
        }
        @Override public String report(String id) {
            return group.report(tabletId());
        }
        public void accumulateToAll() { // adds send times for all tablets together
            // not quite what we want accumulate to do.
            // seems to be only used by driver.
            Histories histories=histories();
            p("before: "+histories.senderHistory.allSendTimes);
            for(String destinationTabletId:group.keys()) {
                Histories h=group.required(destinationTabletId).histories();
                //p("adding: "+h.clientHistory.client.successHistogram);
                //p("to: "+history.clientHistory.allSendTimes);
                histories.senderHistory.allSendTimes.add(h.senderHistory.history.successHistogram);
                //p("result: "+history.clientHistory.allSendTimes);
                histories.senderHistory.allFailures.add(h.senderHistory.history.failureHistogram);
            }
            p("after: "+histories.senderHistory.allSendTimes);
        }
        boolean historiesAreInconsitant() {
            if(tabletId()!=null&&group.keys()!=null) if(!histories.equals(group.required(tabletId()).histories())) {
                p("history in tablet: "+histories.serialNumber);
                if(tabletId()!=null&&group.keys()!=null) p("history in tablet: "+group.required(tabletId()).histories().serialNumber);
                l.severe("histories are not equal");
                return true;
            }
            return false;
        }
        @Override public Histories histories() {
            historiesAreInconsitant();
            return histories;
        }
        @Override public String toString() {
            return tabletId()+" "+model();
        }
        @Override public boolean startServer() {
            // should need only enough information to bind to the correct interface 
            // but at this point we should know our ip address for the correct interface
            l.info(tabletId()+" binding to: "+required.host+':'+required.service);
            InetSocketAddress inetSocketAddress=new InetSocketAddress(required.host,required.service);
            ReceiverImpl receiver=new ReceiverImpl(tabletId(),this,group.keys(),model());
            ServerSocket serverSocket=serverSocket(inetSocketAddress);
            if(serverSocket!=null&&serverSocket.isBound()) {
                server=new Server(this,serverSocket,receiver,config,histories());
                server.startServer();
                histories().tabletHistory.history.success();
                return true;
            } else {
                l.warning("tablet: "+tabletId()+", looks like we can not bind to: "+required.host+':'+required.service);
                histories().tabletHistory.history.failure("looks like we can not bind to: "+required.host+':'+required.service);
                return false;
            }
        }
        @Override public void stopServer() {
            if(server!=null) {
                server.stopServer();
                server=null;
            }
        }
        public void drive(int n,int wait,boolean sendReset) {
            // try sending 3 real fast then waiting a while
            // repeat a lot.
            Random random=new Random();
            int i=0,j0=1;
            if(sendReset) {
                click(model().resetButtonId);
                j0++;
                try {
                    Thread.sleep(2_000);
                } catch(InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            boolean sequential=true;
            double lastToggle=Double.NaN;
            Et et=new Et();
            for(int j=j0;j<=n&&!stopDriving;j++) {
                if(lastToggle!=Double.NaN) ; //p((et.etms()-lastToggle)+" between toggles.");
                if(sequential) i=(j-j0)%(model().buttons-1);
                else i=random.nextInt(model().buttons-1); // omit reset button if any
                toggle(i+1);
                try {
                    Thread.sleep(wait);
                } catch(InterruptedException e) {
                    l.warning("drive caught: '"+e+"'");
                    e.printStackTrace();
                }
                lastToggle=et.etms();
            }
            if(false) try {
                Thread.sleep(5_000);
            } catch(InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        void driveInThread(final boolean sendReset) {
            new Thread(new Runnable() {
                @Override public void run() {
                    drive(100,Config.defaultDriveWait,sendReset);
                    l.severe("start drive histories.");
                    l.severe("drive: "+histories());
                    l.severe("end drive histories.");
                }
            }).start();
        }
        void forever() {
            for(int k=1;k<1000&&!stopDriving;k++) {
                Message message=messageFactory.other(Type.reset,group.groupId,tabletId());
                broadcast(message);
                try {
                    Thread.sleep(5_000);
                } catch(InterruptedException e1) {
                    e1.printStackTrace();
                }
                for(int i=1;i<100&&!stopDriving;i++) {
                    drive(3,75/*Stuff.defaultDriveWait*/,false);
                    try {
                        Thread.sleep(5_000);
                    } catch(InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                report(tabletId());
                message=messageFactory.other(Type.rolloverLogNow,group.groupId,tabletId());
                broadcast(message);
                try {
                    Thread.sleep(1_000);
                } catch(InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            stopDriving=false;
        }
        void foreverInThread() {
            new Thread(new Runnable() {
                @Override public void run() {
                    forever();
                }
            }).start();
        }
        public void startSimulating() {
            if(simulationTimer!=null) stopSimulating();
            ArrayList<String> ids=new ArrayList<>(group.keys());
            final int dt=500;
            final Random random=new Random();
            simulationTimer=new Timer();
            simulationTimer.schedule(new TimerTask() {
                @Override public void run() {
                    int i=random.nextInt(model().buttons-1);
                    click(i+1);
                }
            },1_000+ids.indexOf(tabletId())*dt,dt*group.keys().size());
        }
        public void stopSimulating() {
            if(simulationTimer!=null) {
                simulationTimer.cancel();
                simulationTimer=null;
            }
        }
        @Override public Message.Factory messageFactory() {
            return messageFactory;
        }
        boolean stopDriving;
        public Server server;
        Timer simulationTimer;
        public final Message.Factory messageFactory;
    }
    public Group(String id) { // makes a new one 
        this(id,Collections.<String,Required> emptyMap(),(Model)null);
    }
    public Group(String id,Map<String,Required> idToInfo,Model model) { // makes a new one 
        this(id,idToInfo,model,++serialNumbers);
    }
    private Group(String id,Map<String,Required> idToInfo,Model model,int serialNumber) {
        groupId=id;
        this.idToRequired.putAll(idToInfo);
        // these may all end up with the same info, hence histories!
        prototype=model;
    }
    private Group(String id,Group group,int serialNumber) { // same serial #, copy of map, same requireds!
        groupId=id;
        synchronized(group) {
            if(group.idToRequired!=null) this.idToRequired.putAll(group.idToRequired);
            prototype=group.prototype;
        }
    }
    @Override public Group clone() {
        //super.clone();
        Group group=new Group(this.groupId,this,0);
        return group;
    }
    public Set<Tablet> createAll() { // mostly for testing
        Set<Tablet> tablets=new LinkedHashSet<>();
        for(String tabletId:keys())
            tablets.add(Tablet.factory.create2(this,tabletId,getModelClone()));
        return tablets;
    }
    public static Set<Tablet> createGroupAndstartTablets(String groupId,Map<String,Required> requireds) {
        //Set<Tablet> tablets=new LinkedHashSet<>();
        Group group=new Group("1",requireds,Model.mark1);
        Set<Tablet> tablets=group.createAll();
        for(Tablet tablet:tablets) {
            tablet.model().addObserver(new AudioObserver(tablet.model())); // maybe not if testing?
            if(tablet instanceof TabletImpl2) ((TabletImpl2)tablet).startServer();
        }
        return tablets;
    }
    public Set<String> onHost(String host) { // sync?
        Set<String> set=new TreeSet<>();
        for(String key:keys())
            if(idToRequired.get(key).host.equals(host)) set.add(key);
        return set;
    }
    public InetSocketAddress socketAddress(String tabletId) {
        Required required=required(tabletId);
        return required==null?null:new InetSocketAddress(required.host,required.service);
    }
    public Set<String> keys() {
        if(idToRequired!=null) synchronized(F) {
            return idToRequired.keySet();
        }
        else return null;
    }
    public Required required(String id) { // sync this?
        return id==null?null:idToRequired!=null?idToRequired.get(id):null;
    }
    public String getTabletIdFromHost(String host,Integer service) {
        // this is called on the android!
        synchronized(idToRequired) {
            for(String i:keys()) // fragile!
                if(required(i)!=null&&host.equals(required(i).host)&&(service==null||service==0||service.equals(required(i).service))) return i;
            return null;
        }
    }
    public String getTabletIdFromInetAddress(InetAddress inetAddress,Integer service) {
        // this is called on the android!
        return getTabletIdFromHost(inetAddress.getHostAddress(),service);
    }
    public Model getModelClone() {
        return prototype!=null?prototype.clone():null;
    }
    @Override public String toString() {
        StringBuffer sb=new StringBuffer();
        sb.append("group: ");
        sb.append(groupId);
        sb.append(' ');
        sb.append(keys());
        return sb.toString();
    }
    // move this to histories?
    // get rid of stuff class and just use Map<String,Required>
    // what about clone?
    // need to clone the map?
    // tablet ctor is smart enough to copy the histories from the map
    // and they are all different
    public String report(String id) { // sync?
        // need to find the tablet to get the right history?
        // looks like the server history only makes sense for the driving tablet when testing.
        // so maybe check for that when driving and omit the print.
        StringBuffer sb=new StringBuffer();
        sb.append("histories: "+id+" ------------------------------------");
        for(String i:keys()) {
            Histories histories=required(i).histories();
            if(histories.anyFailures()||histories.senderHistory.history.attempts()!=0) sb.append("\n\tfor "+id+" to: "+i+", history: "+histories.toString("report"));
            else sb.append("\n\tfrom "+id+" to: "+i+", history: no failures.");
        }
        Map<Object,Float> failures=new LinkedHashMap<>();
        double sends=0;
        for(String i:keys()) {
            Histories histories=required(i).histories();
            sends+=histories.senderHistory.history.attempts();
            failures.put(i,(float)(1.*histories.senderHistory.history.failures()/histories.senderHistory.history.attempts()));
            if(false) sb.append("\nsummary from: "+id+", to:"+i+" send failure rate: "+histories.senderHistory.history.failures()+"/"+histories.senderHistory.history.attempts()+"="
                    +(1.*histories.senderHistory.history.failures()/histories.senderHistory.history.attempts()));
        }
        sb.append("\n failure rates from: "+id+" to others: "+failures);
        failures.clear();
        for(String i:keys()) {
            Histories receiverHistory=required(i).histories();
            sends+=receiverHistory.senderHistory.retries.attempts();
            failures.put(i,(float)(1.*receiverHistory.senderHistory.retries.failures()/receiverHistory.senderHistory.retries.attempts()));
            if(false) sb.append("\nsummary from: "+id+", to:"+i+" retry failure rate: "+receiverHistory.senderHistory.retries.failures()+"/"+receiverHistory.senderHistory.retries.attempts()+"="
                    +(1.*receiverHistory.senderHistory.retries.failures()/receiverHistory.senderHistory.retries.attempts()));
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
    public Message random(TabletImpl2 tablet) {
        int buttonId=random.nextInt(tablet.model().buttons)+1;
        return tablet.messageFactory.normal(groupId,tablet.tabletId(),buttonId,tablet.model().toCharacters());
    }
    public final int x=2;
    public final String groupId;
    public final Config config=new Config();
    private final Model prototype; // use only for cloning
    private final Map<String,Required> idToRequired=new TreeMap<>();
    // move this to tablet
    // split these into an options class and a constants class?
    public final Random random=new Random();
    private static int serialNumbers;
}

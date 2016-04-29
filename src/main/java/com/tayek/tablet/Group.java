package com.tayek.tablet;
import static com.tayek.io.IO.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import com.tayek.*;
import com.tayek.Tablet.Factory.FactoryImpl.TabletABC;
import com.tayek.io.Audio.AudioObserver;
import com.tayek.tablet.Message.*;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.io.Server;
import com.tayek.tablet.io.Sender.Client;
import com.tayek.tablet.io.Sender.Client.SendCallable;
import com.tayek.utilities.*;
import static com.tayek.utilities.Utility.*;
public class Group implements Cloneable { // maybe this belongs in sender?
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
    public static class Config {
        public boolean useExecutorService;
        public boolean waitForSendCallable=true;
        public boolean runCanceller;
        public boolean replying;
        public Integer connectTimeout=defaultConnectTimeout; // set by tablet
        public Integer sendTimeout=defaultSendTimeout; // set by tablet
        @Override public String toString() {
            return "Config [replying="+replying+", connectTimeout="+connectTimeout+", sendTimeout="+sendTimeout+", waitForSendCallable="+waitForSendCallable+", useExecutorService="+useExecutorService
                    +", runCanceller="+runCanceller+"]";
        }
        public static Integer defaultConnectTimeout=2_000; // 40;
        public static Integer defaultSendTimeout=defaultConnectTimeout+50; // 60;
        public static Integer defaultDriveWait=200; // 100;
    }
    public class TabletImpl2 extends TabletABC {
        // no need for ip address really,
        // maybe set the tablet id after construction
        // or before broadcast
        // and add "instance" stuff!
        // like last mesgage sent
        // and ip addresses
        // so let's try:
        // adding host:service to start listening
        // and map<String,SocketAddress> to broadcast;
        public TabletImpl2(String tabletId,Required required,Model model) {
            super(groupId,tabletId,model,required.histories());
            this.required=required;
            messageFactory=Message.instance.create(required,new Single<Integer>(0));
            int n=keys().size();
            executorService=Executors.newFixedThreadPool(4*n+2);
            canceller=Executors.newScheduledThreadPool(4*n+2);
            model().histories=histories();
        }
        public Group group() {
            return Group.this;
        }
        @Override public void broadcast(Object message) {
            l.info("broadcasting: "+message);
            for(String destinationTabletId:keys()) {
                InetSocketAddress inetSocketAddress=socketAddress(destinationTabletId);
                SendCallable sendCallable=new SendCallable(tabletId(),message,destinationTabletId,required(destinationTabletId).histories(),inetSocketAddress);
                if(config.useExecutorService)
                    Client.executeTaskAndCancelIfItTakesTooLong(executorService,sendCallable,config.sendTimeout,config.runCanceller?canceller:null,config.waitForSendCallable);
                else new Thread(new SendCallable(tabletId(),message,destinationTabletId,required(destinationTabletId).histories(),inetSocketAddress)).start();
                Thread.yield();
            }
            Histories.SenderHistory clientHistory=histories.senderHistory;
            // maybe sleep for a while here?
            if(histories.reportPeriod>0&&histories.anyAttempts()&&clientHistory.history.attempts()%histories.reportPeriod==0) l.warning("histories from client: "+histories());
            if(histories.reportPeriod>0&&histories.anyAttempts()&&clientHistory.history.attempts()%(10*histories.reportPeriod)==0) l.warning("report histories from client: "+report(tabletId()));
        }
        @Override public String report(String id) {
            return Group.this.report(tabletId());
        }
        public void accumulateToAll() { // adds send times for all tablets together
            // not quite what we want accumulate to do.
            // seems to be only used by driver.
            Histories histories=histories();
            p("before: "+histories.senderHistory.allSendTimes);
            for(String destinationTabletId:keys()) {
                Histories h=required(destinationTabletId).histories();
                //p("adding: "+h.clientHistory.client.successHistogram);
                //p("to: "+history.clientHistory.allSendTimes);
                histories.senderHistory.allSendTimes.add(h.senderHistory.history.successHistogram);
                //p("result: "+history.clientHistory.allSendTimes);
                histories.senderHistory.allFailures.add(h.senderHistory.history.failureHistogram);
            }
            p("after: "+histories.senderHistory.allSendTimes);
        }
        boolean historiesAreInconsitant() {
            if(tabletId()!=null&&keys()!=null) if(!histories.equals(required(tabletId()).histories())) {
                p("history in tablet: "+histories.serialNumber);
                if(tabletId()!=null&&keys()!=null) p("history in tablet: "+required(tabletId()).histories().serialNumber);
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
        public boolean startListening() {
            // should need only enough information to bind to the correct interface 
            // but at this point we should know our ip address for the correct interface
            InetSocketAddress inetSocketAddress=new InetSocketAddress(required.host,required.service);
            try {
                Receiver.ReceiverImpl receiver=new Receiver.ReceiverImpl(tabletId(),this,keys(),model());
                server=new Server(this,inetSocketAddress,receiver,config,histories());
                server.startServer();
                return true;
            } catch(BindException e) {
                l.warning("bind caught: '"+e+"'");
            } catch(IOException e) {
                e.printStackTrace();
            }
            return false;
        }
        public void stopListening() {
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
                    drive(100,Group.Config.defaultDriveWait,sendReset);
                    l.severe("start drive histories.");
                    l.severe("drive: "+histories());
                    l.severe("end drive histories.");
                }
            }).start();
        }
        void forever() {
            for(int k=1;k<1000&&!stopDriving;k++) {
                Message message=messageFactory.other(Type.reset,groupId,tabletId());
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
                message=messageFactory.other(Type.rolloverLogNow,groupId,tabletId());
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
        public void startHeatbeat() {
            if(heartbeatTimer!=null) stopHeartbeat();
            if(true) {
                final int dt=500;
                ArrayList<String> ids=new ArrayList<>(keys());
                l.info(""+System.currentTimeMillis());
                heartbeatTimer=new Timer();
                heartbeatTimer.schedule(new TimerTask() {
                    @Override public void run() {
                        broadcast(messageFactory.other(Type.heartbeat,groupId,tabletId()));
                    }
                },1_000+ids.indexOf(tabletId())*dt,dt*keys().size());
            }
        }
        public void stopHeartbeat() {
            if(heartbeatTimer!=null) {
                heartbeatTimer.cancel();
                heartbeatTimer=null;
            }
        }
        public void startSimulating() {
            if(simulationTimer!=null) stopSimulating();
            ArrayList<String> ids=new ArrayList<>(keys());
            final int dt=500;
            final Random random=new Random();
            simulationTimer=new Timer();
            simulationTimer.schedule(new TimerTask() {
                @Override public void run() {
                    int i=random.nextInt(model().buttons-1);
                    click(i+1);
                }
            },1_000+ids.indexOf(tabletId())*dt,dt*keys().size());
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
        final Required required;
        public Config config=new Config(); // maybe pass in ctor later?
        public final ScheduledExecutorService canceller;
        public final ExecutorService executorService;
        public Server server;
        Timer simulationTimer;
        Timer heartbeatTimer;
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
        if(group.idToRequired!=null) this.idToRequired.putAll(group.idToRequired);
        prototype=group.prototype;
    }
    @Override public Group clone() {
        //super.clone();
        Group group=new Group(this.groupId,this,0);
        return group;
    }
    public Set<TabletImpl2> createAll() { // mostly for testing
        Set<TabletImpl2> tablets=new LinkedHashSet<>();
        for(String tabletId:keys())
            tablets.add((TabletImpl2)Tablet.factory.create2(tabletId,this,getModelClone()));
        return tablets;
    }
    public static Set<TabletImpl2> createGroupAndstartTablets(String groupId,Map<String,Required> requireds) {
        //Set<Tablet> tablets=new LinkedHashSet<>();
        Group group=new Group("1",requireds,Model.mark1);
        Set<TabletImpl2> tablets=group.createAll();
        for(TabletImpl2 tablet:tablets) {
            tablet.model().addObserver(new AudioObserver(tablet.model())); // maybe not if testing?
            tablet.startListening();
        }
        return tablets;
    }
    public InetSocketAddress socketAddress(String tabletId) {
        Required required=required(tabletId);
        return required==null?null:new InetSocketAddress(required.host,required.service);
    }
    public Set<String> keys() {
        return idToRequired!=null?idToRequired.keySet():null;
    }
    public Required required(String id) {
        return id==null?null:idToRequired!=null?idToRequired.get(id):null;
    }
    public String getTabletIdFromInetAddress(InetAddress inetAddress,Integer service) {
        // this is called on the android!
        for(String i:keys()) // fragile!
            if(required(i)!=null&&inetAddress.getHostAddress().equals(required(i).host)&&(service==null||service==0||service.equals(required(i).service))) return i;
        return null;
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
    public String report(String id) {
        // need to find the tablet to get the right history?
        // looks like the server history only makes sense for the driving tablet when testing.
        // so maybe check for that when driving and omit the print.
        StringBuffer sb=new StringBuffer();
        sb.append("histories: "+id+" ------------------------------------");
        for(String i:keys()) {
            Histories histories=required(i).histories();
            if(histories.anyFailures()||histories.senderHistory.history.attempts()!=0) sb.append("\n\tfor "+id+" to: "+i+", history: "+histories.toString());
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
    private final Model prototype; // use only for cloning
    private final Map<String,Required> idToRequired=new TreeMap<>();
    // move this to tablet
    // split these into an options class and a constants class?
    public final Random random=new Random();
    private static int serialNumbers;
}

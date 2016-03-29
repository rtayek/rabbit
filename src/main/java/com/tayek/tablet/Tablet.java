package com.tayek.tablet;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import com.tayek.io.IO;
import com.tayek.tablet.Main;
import com.tayek.tablet.Main.Stuff;
import com.tayek.tablet.Main.Stuff.Info;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.Messages.*;
import com.tayek.tablet.io.*;
import com.tayek.tablet.io.Audio.Sound;
import com.tayek.tablet.io.Sender.Client;
import com.tayek.tablet.io.Sender.Client.SendCallable;
import static com.tayek.io.IO.*;
public class Tablet {
    // no need for ip address really,
    // maybe set the tablet id after construction
    // or before broadcast
    // and add "instance" stuff!
    // like last mesage sent
    // and ip addresses
    // so let's try:
    // adding host:service to start listening
    // and map<String,SocketAddress> to broadcast;
    public Tablet(Stuff stuff,String tabletId) {
        this.stuff=stuff;
        groupId=""+stuff.groupIdx;
        this.tabletId=tabletId;
        model=stuff.getModelClone();
        this.colors=new Colors();
        Histories histories=null;
        if(stuff.keys()!=null) {
            if(tabletId!=null) {
                if(stuff.keys().contains(tabletId)) {
                    histories=stuff.info(tabletId()).histories();
                    p("tablet: "+tabletId()+" is using history from stuff: "+histories.serialNumber);
                } else {
                    histories=new Histories();
                    l.severe("tablet: "+tabletId+" is not a member of group: "+stuff);
                }
            } else l.severe("tablet id is null!");
        } else l.severe("keys is null!");
        this.histories=histories!=null?histories:new Histories();
        if(model!=null) {
            model.tablet=this;
            model.histories=histories;
        } else l.severe("model is null!");
    }
    public void accumulateToAll() {
        Histories history=histories();
        p("before: "+history.client.allSendTimes);
        for(String destinationTabletId:stuff.keys()) {
            Histories h=stuff.info(destinationTabletId).histories();
            //p("adding: "+h.clientHistory.client.successHistogram);
            //p("to: "+history.clientHistory.allSendTimes);
            history.client.allSendTimes.add(h.client.client.successHistogram);
            //p("result: "+history.clientHistory.allSendTimes);
            history.client.allFailures.add(h.client.client.failureHistogram);
        }
        p("after: "+history.client.allSendTimes);
    }
    // continue to move these into sender!
    /*
    static class BroadcastCallable implements Callable<Void> {
        BroadcastCallable(Tablet tablet,Message message,Map<String,Info> info,Stuff stuff) {
            this.tablet=tablet;
            this.id=tablet.tabletId();
            this.message=message;
            this.info=info;
            this.stuff=stuff;
        }
        @Override public Void call() throws Exception {
            Thread.currentThread().setName(getClass().getName()+" "+id+" broadcast ing");
            staticLogger.info("broadcast: "+message);
            for(String destinationId:info.keySet()) {
                staticLogger.info("broadcasting to: "+destinationId);
                // what should this wait really be?
                InetSocketAddress inetSocketAddress=Group.socketAddress(info,destinationId);
                Future<Void> future=Client.executeTaskAndCancelIfItTakesTooLong(tablet.group.executorService,
                        new SendCallable(id,message,destinationId,stuff,tablet.group.idToInfo().get(destinationId).history,inetSocketAddress),stuff.sendTimeout,
                        stuff.runCanceller?tablet.group.canceller:null,stuff.waitForSendCallable);
            }
            return null;
        }
        final Tablet tablet;
        final String id;
        final Message message;
        final Map<String,Info> info;
        final Stuff stuff;
    }
    */
    public void broadcast(final Message message,Stuff stuff) {
        l.info("broadcasting: "+message);
        for(String destinationTabletId:stuff.keys()) {
            InetSocketAddress inetSocketAddress=stuff.socketAddress(destinationTabletId);
            SendCallable sendCallable=new SendCallable(tabletId(),message,destinationTabletId,stuff,stuff.info(destinationTabletId).histories(),inetSocketAddress);
            if(stuff.useExecutorService)
                Client.executeTaskAndCancelIfItTakesTooLong(stuff.executorService,sendCallable,this.stuff.sendTimeout,stuff.runCanceller?stuff.canceller:null,stuff.waitForSendCallable);
            else new Thread(new SendCallable(tabletId(),message,destinationTabletId,stuff,stuff.info(destinationTabletId).histories(),inetSocketAddress)).start();
        }
        Histories.ClientHistory clientHistory=histories.client;
        if(stuff.reportPeriod>0&&histories.anyAttempts()&&clientHistory.client.attempts()%stuff.reportPeriod==0) l.warning("report histories from client: "+histories());
        if(stuff.reportPeriod>0&&histories.anyAttempts()&&clientHistory.client.attempts()%(10*stuff.reportPeriod)==0) l.warning("report histories from client: "+stuff.report(tabletId()));
    }
    public void toggle(int id) {
        Boolean state=!model.state(id);
        model.setState(id,state);
        Message message=stuff.messages.normal(groupId,tabletId(),id,model);
        broadcast(message,stuff);
    }
    public void click(int id) {
        synchronized(model) {
            if(model.resetButtonId!=null&&id==model.resetButtonId) {
                model.reset();
                Message message=stuff.messages.other(Type.reset,groupId,tabletId());
                broadcast(message,stuff);
            } else {
                Boolean state=!model.state(id);
                model.setState(id,state);
                Message message=stuff.messages.normal(groupId,tabletId(),id,model);
                broadcast(message,stuff);
            }
        }
    }
    public static Tablet create(Stuff stuff,String tabletId) {
        Tablet tablet=new Tablet(stuff.clone(),tabletId);
        return tablet;
    }
    public static Set<Tablet> create(Stuff stuff) { // mostly for testing
        Set<Tablet> tablets=new LinkedHashSet<>();
        for(String tabletId:stuff.keys())
            tablets.add(Tablet.create(stuff.clone(),tabletId));
        for(Tablet tablet:tablets)
            p("stuff history # for "+tablet.tabletId+" is "+tablet.stuff.info(tablet.tabletId()).histories().serialNumber);
        p("created: "+tablets);
        return tablets;
    }
    public static Set<Tablet> createGroupAndstartTablets(Map<String,Info> infos) {
        //Set<Tablet> tablets=new LinkedHashSet<>();
        Stuff stuff=new Stuff(1,infos,Model.mark1);
        Set<Tablet> tablets=create(stuff);
        for(Tablet tablet:tablets) {
            p("tablet id: "+tablet.tabletId+" "+tablet.histories().serialNumber);
            tablet.model.addObserver(new AudioObserver(tablet.model)); // maybe not if testing?
            SocketAddress socketAddress=tablet.stuff.socketAddress(tablet.tabletId());
            tablet.startListening(socketAddress);
        }
        return tablets;
    }
    public static Set<Tablet> createForTest(int n,int offset) {
        Map<String,Info> map=new TreeMap<>();
        // search for linked hash map and use tree map instead.
        for(int i=1;i<=n;i++)
            map.put("T"+i+" on PC",new Info("T"+i+" on PC",Main.testingHost,Main.defaultReceivePort+100+offset+i));
        Stuff stuff=new Stuff(1,map,Model.mark1);
        Set<Tablet> tablets=create(stuff);
        return tablets;
    }
    public boolean historiesAreInconsitant() {
        if(tabletId()!=null&&stuff.keys()!=null) if(!histories.equals(stuff.info(tabletId()).histories())) {
            p("tablet history in tablet: "+histories.serialNumber);
            if(tabletId()!=null&&stuff.keys()!=null) p("stuff history in tablet: "+stuff.info(tabletId()).histories().serialNumber);
            l.severe("histories are not equal");
            return true;
        }
        return false;
    }
    public Histories histories() {
        historiesAreInconsitant();
        return histories;
    }
    public String tabletId() {
        return tabletId;
    }
    @Override public String toString() {
        return tabletId()+" "+model;
    }
    public String toString2() {
        return tabletId()+" "+model+"\n"+histories();
    }
    boolean doingLastOnFrom=false;
    public String getButtonText(Integer buttonId) {
        String text=null;
        if(!doingLastOnFrom) {
            if(buttonId.equals(model.resetButtonId)) text="R";
            else if((buttonId-1)/colors.columns%2==0) text=""+(char)('0'+buttonId);
        } else {
            String ourName="T"+tabletId()+":B"+buttonId;
            if(model.state(buttonId).equals(true)) {
                Object lastOnFrom=model.lastOnFrom(buttonId);
                if(lastOnFrom!=null) {
                    String hisName="T"+lastOnFrom+":B"+buttonId;
                    text=pad(ourName+" ("+hisName+")");
                } else text=pad(ourName);
            } else text=pad(ourName);
        }
        return text;
    }
    public static String pad(String string) {
        for(;string.length()<length;string+=' ')
            ;
        return string;
    }
    public void print(String tabletId) {
        l.info("group: "+groupId+"("+stuff.serialNumber+"):"+tabletId());
        for(String i:stuff.keys())
            l.info("\t"+i+": "+stuff.info(i));
    }
    public boolean startListening(SocketAddress socketAddress) {
        // should need only enough info to bind to the correct interface 
        // but at this point we should know our ip address for the correct interface
        if(socketAddress==null) {
            l.severe("socket address is null!");
            return false;
        }
        try {
            Receiver.ReceiverImpl receiver=new Receiver.ReceiverImpl(tabletId(),this,stuff.keys(),model);
            server=new Server(this,socketAddress,receiver,stuff,histories());
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
    public static Message random(Tablet tablet) {
        int buttonId=random.nextInt(tablet.model.buttons)+1;
        return tablet.stuff.messages.normal(tablet.groupId,tablet.tabletId(),buttonId,tablet.model);
    }
    // this seems to never send messages to itself
    // or it's not responding to them.
    public static void startSimulating(final Tablet tablet) {
        if(tablet.simulationTimer!=null) tablet.stopSimulating();
        ArrayList<String> ids=new ArrayList<>(tablet.stuff.keys());
        final int dt=500;
        final Random random=new Random();
        tablet.simulationTimer=new Timer();
        tablet.simulationTimer.schedule(new TimerTask() {
            @Override public void run() {
                int i=random.nextInt(tablet.model.buttons-1);
                tablet.click(i+1);
            }
        },1_000+ids.indexOf(tablet.tabletId())*dt,dt*tablet.stuff.keys().size());
    }
    public void stopSimulating() {
        if(simulationTimer!=null) {
            simulationTimer.cancel();
            simulationTimer=null;
        }
    }
    public static void startHeatbeat(final Tablet tablet) {
        if(tablet.heartbeatTimer!=null) tablet.stopHeartbeat();
        if(true) {
            final int dt=500;
            ArrayList<String> ids=new ArrayList<>(tablet.stuff.keys());
            l.info(""+System.currentTimeMillis());
            tablet.heartbeatTimer=new Timer();
            tablet.heartbeatTimer.schedule(new TimerTask() {
                @Override public void run() {
                    tablet.broadcast(tablet.stuff.messages.other(Type.heartbeat,tablet.groupId,tablet.tabletId()),tablet.stuff);
                }
            },1_000+ids.indexOf(tablet.tabletId())*dt,dt*tablet.stuff.keys().size());
        }
    }
    public void stopHeartbeat() {
        if(heartbeatTimer!=null) {
            heartbeatTimer.cancel();
            heartbeatTimer=null;
        }
    }
    // move the timers to group?
    public static void startChimer(final Tablet tablet) {
        if(tablet.chimer==null) {
            l.info(""+System.currentTimeMillis());
            tablet.chimer=new Timer();
            tablet.chimer.schedule(new TimerTask() {
                @Override public void run() {
                    Audio.audio.play(Sound.electronic_chime_kevangc_495939803);
                }
            },0,5_000);
        }
    }
    public void stopChimer() {
        if(chimer!=null) {
            chimer.cancel();
            chimer=null;
        }
    }
    public static void main(String[] arguments) throws IOException,InterruptedException {
        System.getProperties().list(System.out);
    }
    Timer simulationTimer;
    Timer heartbeatTimer;
    Timer chimer;
    public String groupId;
    private String tabletId;
    private final Histories histories;
    public Server server;
    public final Model model;
    public final Colors colors;
    public Stuff stuff;
    static final int length=10;
    public static final Random random=new Random();
    public static Integer driveWait=60; // 100;
}

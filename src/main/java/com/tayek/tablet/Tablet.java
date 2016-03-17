package com.tayek.tablet;
import static com.tayek.tablet.io.IO.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import com.tayek.tablet.Messages.Message;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.io.*;
import com.tayek.utilities.*;
public class Tablet {
    // no need for ip address really,
    // maybe set the tablet id after construction
    // or before broadcast
    // and add "instance" stuff!
    // like last mesage sent
    // and ip addresses
    public Tablet(Group group,Object tabletId) {
        this(group,tabletId,Histories.defaultConnectTimeout,Histories.defaultSendTimeout);
    }
    public Tablet(Group group,Object tabletId,int connectTimeout,int sendTimeout) {
        this.group=group;
        this.tabletId=tabletId;
        group.connectTimeout=connectTimeout;
        group.sendTimeout=sendTimeout;
        model=group.getModelClone();
        model.history=group.info(tabletId).history.model;
        model.tablet=this;
        this.colors=new Colors();
        if(!group.tabletIds().contains(tabletId)) l.severe("tablet: "+tabletId+" is not a member of group: "+group);
    }
    public void accumulateToAll() {
        Histories history=group.info(tabletId()).history;
        p("before: "+history.client.allSendTimes);
        for(Object destinationTabletId:group.tabletIds()) {
            Histories h=group.info(destinationTabletId).history;
            //p("adding: "+h.clientHistory.client.successHistogram);
            //p("to: "+history.clientHistory.allSendTimes);
            history.client.allSendTimes.add(h.client.client.successHistogram);
            //p("result: "+history.clientHistory.allSendTimes);
            history.client.allFailures.add(h.client.client.failureHistogram);
        }
        p("after: "+history.client.allSendTimes);
    }
    public void broadcast(final Message message,int unused) {
        group.broadcast(this,message,unused);
    }
    // add toggle for testing?
    public void toggle(int id) {
        Boolean state=!model.state(id);
        model.setState(id,state);
        Message message=group.messages.normal(group.groupId,tabletId(),id,model);
        broadcast(message,0);
    }
    public void click(int id) {
        synchronized(model) {
            if(model.resetButtonId!=null&&id==model.resetButtonId) {
                model.reset();
                Message message=group.messages.reset(group.groupId,tabletId(),id);
                broadcast(message,0);
            } else {
                Boolean state=!model.state(id);
                model.setState(id,state);
                Message message=group.messages.normal(group.groupId,tabletId(),id,model);
                broadcast(message,0);
            }
        }
    }
    public Object tabletId() {
        return tabletId;
    }
    @Override public String toString() {
        return tabletId()+" "+model;
    }
    public String toString2() {
        return tabletId()+" "+model+"\n"+group.info(tabletId).history;
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
    public boolean startListening() {
        // should need only enough info to bind to the correct interface 
        // but at this point we should know our ip address for the correct interface
        SocketAddress socketAddress=group.socketAddress(tabletId());
        if(socketAddress==null) {
            l.severe("socket address is null!");
            return false;
        }
        Histories history=group.info(tabletId()).history;
        try {
            Receiver.ReceiverImpl receiver=new Receiver.ReceiverImpl(tabletId(),this,group.tabletIds(),model);
            server=new Server(this,socketAddress,receiver,group.replying,history.server);
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
    // this seems to never send messages to itself
    // or it's not responding to them.
    public static void startSimulating(final Tablet tablet) {
        if(tablet.simulationTimer!=null) tablet.stopSimulating();
        ArrayList<Object> ids=new ArrayList<>(tablet.group.tabletIds());
        final Random random=new Random();
        if(true) {
            tablet.l.info(""+System.currentTimeMillis());
            final long t0=1_447_900_000_000l;
            final int dt=500;
            tablet.l.info("before wait, time: "+System.currentTimeMillis());
            while(System.currentTimeMillis()%1000>10)
                ;
            tablet.l.info("after wait, time: "+System.currentTimeMillis());
            tablet.simulationTimer=new Timer();
            tablet.simulationTimer.schedule(new TimerTask() {
                @Override public void run() {
                    if(true) { // fix simulating bug
                        int i=random.nextInt(tablet.model.buttons-1);
                        tablet.click(i+1);
                    } else {
                        Message message=Group.randomToggle(tablet);
                        tablet.l.info(""+(System.currentTimeMillis()-t0)+" "+tablet+" "+message);
                        tablet.broadcast(message,0);
                        //tablet.model.receive(message); // maybe do not need since sending to self now?
                        // we do need this
                    }
                }
            },1_000+ids.indexOf(tablet.tabletId())*dt,dt*tablet.group.tabletIds().size());
        }
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
            tablet.l.info(""+System.currentTimeMillis());
            tablet.heartbeatTimer=new Timer();
            tablet.heartbeatTimer.schedule(new TimerTask() {
                @Override public void run() {
                    tablet.broadcast(tablet.group.messages.heartbeat(tablet.group.groupId,tablet.tabletId),0);
                }
            },1_000);
        }
    }
    public void stopHeartbeat() {
        if(heartbeatTimer!=null) {
            heartbeatTimer.cancel();
            heartbeatTimer=null;
        }
    }
    public static void main(String[] arguments) throws IOException,InterruptedException {
        System.getProperties().list(System.out);
    }
    Timer simulationTimer;
    Timer heartbeatTimer;
    private final Object tabletId;
    public Server server;
    public static Integer driveWait=50; // 100;
    public final Model model;
    public final Colors colors;
    public final Group group;
    public final Logger l=Logger.getLogger(getClass().getName());
    static final int length=10;
}

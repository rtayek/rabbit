package com.tayek.tablet;
import static com.tayek.tablet.io.IO.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import com.tayek.tablet.Group.Info.Histories;
import com.tayek.tablet.Tablet.SendCallable;
import com.tayek.tablet.io.*;
import com.tayek.utilities.*;
public class Tablet {
    public enum LevelSubMenuItem {
        all(Level.ALL),finest(Level.FINEST),finer(Level.FINER),fine(Level.FINE),config(Level.CONFIG),info(Level.INFO),warning(Level.WARNING),sever(Level.SEVERE),none(Level.OFF);
        LevelSubMenuItem(Level level) {
            this.level=level;
        }
        public void doItem(Tablet tablet) {
            doItem(this,tablet);
        }
        public static boolean isItem(int ordinal) {
            return item(ordinal)!=null;
        }
        public static LevelSubMenuItem item(int ordinal) {
            return 0<=ordinal&&ordinal<values().length?values()[ordinal]:null;
        }
        public static void doItem(int ordinal,Tablet tablet) { // used by android
            if(tablet!=null) if(0<=ordinal&&ordinal<values().length) values()[ordinal].doItem(tablet);
            else tablet.l.warning(ordinal+" is invalid ordinal for!");
            else IO.staticLogger.warning("tablet is null in do item!");
        }
      public static void doItem(LevelSubMenuItem levelSubMenuItem,final Tablet tablet) {
            LoggingHandler.setLevel(levelSubMenuItem.level);
        }
        private final Level level;
    }
    public enum MenuItem {
        Reset,Ping,Heartbeat,Connect,Disconnect,Log,Sound,Simulate,Quit,Drive;
        public void doItem(Tablet tablet) {
            doItem(this,tablet);
        }
        public static boolean isItem(int ordinal) {
            return item(ordinal)!=null;
        }
        public static MenuItem item(int ordinal) {
            return 0<=ordinal&&ordinal<values().length?values()[ordinal]:null;
        }
        public static void doItem(int ordinal,Tablet tablet) { // used by android
            if(tablet!=null) if(0<=ordinal&&ordinal<values().length) values()[ordinal].doItem(tablet);
            else tablet.l.warning(ordinal+" is invalid ordinal for!");
            else IO.staticLogger.warning("tablet is null in do item!");
        }
        public static void doItem(MenuItem tabletMenuItem,final Tablet tablet) {
            switch(tabletMenuItem) {
                case Reset:
                    tablet.model.reset();
                    break;
                case Ping:
                    tablet.broadcast(Message.ping(tablet.group.groupId,tablet.tabletId),0);
                    break;
                case Heartbeat:
                    if(tablet.heartbeatTimer!=null) Tablet.startHeatbeat(tablet);
                    else tablet.stopHeartbeat();
                    break;
                case Disconnect:
                    tablet.stopListening();
                    break;
                case Connect:
                    if(!tablet.startListening()) tablet.l.info(Utility.method()+" startListening() failed!");
                    break;
                case Log:
                    // gui.textView.setVisible(!gui.textView.isVisible());
                    break;
                case Sound:
                    Audio.Instance.sound=!Audio.Instance.sound;
                    tablet.l.info("sound: "+Audio.Instance.sound);
                    break;
                case Simulate:
                    if(tablet.simulationTimer==null) Tablet.startSimulating(tablet);
                    else tablet.stopSimulating();
                    break;
                case Quit:
                    // System.exit(0); // how to test this?
                    break;
                case Drive:
                    new Thread(new Runnable() {
                        @Override public void run() {
                            int n=100; // put this in main?
                            tablet.reportPeriod=n;
                            tablet.drive(n,driveWait);
                            try {
                                Thread.sleep(100);
                            } catch(InterruptedException e) {
                                tablet.l.severe("caught: "+e);
                            }
                            tablet.l.severe("start histories()");
                            tablet.l.severe(tablet.group.histories(tablet));
                            tablet.l.severe("start histories()");
                        }
                    }).start();
                    break;
                default:
                    tablet.l.severe(tabletMenuItem+" was not handled!");
            }
        }
    }
    private void send(final Message message,Integer destinationTabletId,int timeout) {
        InetSocketAddress inetSocketAddress=group.socketAddress(destinationTabletId);
        if(message.type.equals(Message.Type.ack)) l.warning("sending act to: "+inetSocketAddress);
        Client client=new Client(inetSocketAddress,group.replying,timeout);
        //if(!destinationTabletId.equals(tabletId()))
        Histories history=group.info(destinationTabletId).history;
        l.info("tablet "+name+"("+tabletId()+") sending: #"+(history.client.client.attempts()+1)+" to tablet "+group.name(destinationTabletId));
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
        public SendCallable(Message message,Integer destinationTabletId,int timeout) {
            this.message=message;
            this.destinationTabletId=destinationTabletId;
            this.timeout=timeout;
        }
        @Override public Void call() throws Exception {
            Thread.currentThread().setName(getClass().getSimpleName()+", tablet: "+tabletId+" send to: "+destinationTabletId);
            l.fine("call send to: "+destinationTabletId);
            if(message.type.equals(Message.Type.ack)) l.warning(Tablet.this+", sending ack: "+message+", to: "+destinationTabletId);
            send(message,destinationTabletId,timeout);
            return null;
        }
        private final Message message;
        private final Integer destinationTabletId;
        private final Integer timeout;
    }
    private class BroadcastCallable implements Callable<Void> {
        BroadcastCallable(Message message) {
            this.message=message;
        }
        @Override public Void call() throws Exception {
            Thread.currentThread().setName(getClass().getName()+" tablet: "+tabletId+" broadcast");
            l.info("broadcast: "+message);
            for(Integer destinationTabletId:group.tabletIds()) {
                l.info("broadcasting to: "+destinationTabletId);
                // what should this wait really be?
                executeTaskAndCancelIfItTakesTooLong(new SendCallable(message,destinationTabletId,connectTimeout),sendTimeout,canceller);
            }
            return null;
        }
        final Message message;
    }
    public Tablet(Group group,int tabletId) {
        this(group,tabletId,defaultConnectTimeout,(int)(defaultConnectTimeout*1.3));
    }
    public Tablet(Group group,int tabletId,int connectTimeout,int sendTimeout) {
        this.group=group;
        this.tabletId=tabletId;
        this.connectTimeout=connectTimeout;
        this.sendTimeout=sendTimeout;
        int n=group.info().size()+4;
        this.executorService=Executors.newFixedThreadPool(n+2);
        canceller=Executors.newScheduledThreadPool(n+2);
        model=group.getModelClone();
        model.history=group.info(tabletId).history.model;
        model.tablet=this;
        this.colors=new Colors();
        if(!group.tabletIds().contains(tabletId)) l.severe("tablet: "+tabletId+" is not a member of group: "+group);
        setName("T"+tabletId);
        if(group.info(tabletId)!=null&&group.info(tabletId).name!=null) setName(group.info(tabletId).name);
    }
    public <T> Future<T> executeTaskAndCancelIfItTakesTooLong(final Callable<T> callable,final long timeoutMS,ScheduledExecutorService canceller) {
        final Future<T> future=executorService.submit(callable);
        // awk this makes another thread!
        canceller.schedule(new Callable<Void>() {
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
    public void accumulateToAll() {
        Histories history=group.info(tabletId()).history;
        p("before: "+history.client.allSendTimes);
        for(Integer destinationTabletId:group.tabletIds()) {
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
        // does this wait?
        // false seemed to work, but still got a lot of timeouts
        if(false) executorService.submit(new BroadcastCallable(message));
        else for(Integer destinationTabletId:group.tabletIds())
            executeTaskAndCancelIfItTakesTooLong(new SendCallable(message,destinationTabletId,connectTimeout),sendTimeout,canceller);
        // no, it does not wait!
        // we only need this to be on a separate thread if it waits
        // and currently, it does not wait.
        Histories both=group.info(tabletId()).history;
        Client.History clientHistory=both.client;
        if(reportPeriod>0&&both.anyAttempts()&&(clientHistory.client.attempts()%reportPeriod==0||both.server.server.attempts()%reportPeriod==0))
            if(true||both.anyFailures()) l.warning("histories from client: "+group.histories(this));
        else l.warning("no failures in client (really?)&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
    }
    // add toggle for testing?
    public void toggle(int id) {
        Boolean state=!model.state(id);
        model.setState(id,state);
        Message message=Message.normal(group.groupId,tabletId(),id,model);
        broadcast(message,0);
    }
    public void click(int id) {
        synchronized(model) {
            if(model.resetButtonId!=null&&id==model.resetButtonId) {
                model.reset();
                Message message=Message.reset(group.groupId,tabletId(),id);
                broadcast(message,0);
            } else {
                Boolean state=!model.state(id);
                model.setState(id,state);
                Message message=Message.normal(group.groupId,tabletId(),id,model);
                broadcast(message,0);
            }
        }
    }
    public void setName(String name) {
        this.name=name;
    }
    public String name() {
        return name;
    }
    public Integer tabletId() {
        return tabletId;
    }
    @Override public String toString() {
        return name+" "+model;
    }
    public String toString2() {
        return name+" "+model+"\n"+group.info(tabletId).history;
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
                Integer lastOnFrom=model.lastOnFrom(buttonId);
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
            server=new Server(this,socketAddress,model,group.replying,history);
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
    public void drive(int n,int wait) {
        // try sending 3 real fast then waiting a while
        // repeat a lot.
        Message message=Message.rolloverLogNow(group.groupId,tabletId);
        broadcast(message,0);
        try {
            Thread.sleep(1_000);
        } catch(InterruptedException e1) {
            e1.printStackTrace();
        }
        Random random=new Random();
        int i=0;
        click(model.resetButtonId);
        try {
            Thread.sleep(1_000);
        } catch(InterruptedException e1) {
            e1.printStackTrace();
        }
        boolean sequential=true;
        for(int j=3;j<=n;j++) {
            Et et=new Et();
            if(sequential) i=(j-3)%(model.buttons-1);
            else i=random.nextInt(model.buttons-1);
            toggle(i+1);
            try {
                Thread.sleep(wait);
            } catch(InterruptedException e) {
                l.warning("drive caught: '"+e+"'");
                e.printStackTrace();
            }
        }
        //l.warning(histories());
    }
    // this seems to never send messages to itself
    // or it's not responding to them.
    public static void startSimulating(final Tablet tablet) {
        if(tablet.simulationTimer!=null) tablet.stopSimulating();
        ArrayList<Integer> ids=new ArrayList<>(tablet.group.tabletIds());
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
        ArrayList<Integer> ids=new ArrayList<>(tablet.group.tabletIds());
        if(true) {
            tablet.l.info(""+System.currentTimeMillis());
            tablet.heartbeatTimer=new Timer();
            tablet.heartbeatTimer.schedule(new TimerTask() {
                @Override public void run() {
                    tablet.broadcast(Message.heartbeat(tablet.group.groupId,tablet.tabletId),0);
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
    private Timer simulationTimer;
    Timer heartbeatTimer;
    private String name;
    private final Integer tabletId;
    public final Integer connectTimeout;
    public final Integer sendTimeout;
    public Integer reportPeriod=100;
    public Server server;
    public final ExecutorService executorService;
    public final ScheduledExecutorService canceller;
    public final Model model;
    public final Colors colors;
    public final Group group;
    public static Integer defaultConnectTimeout=100; // 40;
    public static Integer defaultSendTimeout=150; // 60;
    public static Integer driveWait=200; // 100;
    public final Logger l=Logger.getLogger(getClass().getName());
    static final int length=10;
}

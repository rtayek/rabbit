package com.tayek;
import static com.tayek.io.IO.*;
import java.util.*;
import com.tayek.Tablet.Config;
import com.tayek.speed.Server;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.*;
import com.tayek.tablet.Message.Type;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.utilities.Et;
public interface Tablet {
    Config config();
    Group group();
    String tabletId();
    int tablets();
    //boolean isServerAccepting(); // maybe add this
    Message.Factory messageFactory();
    void broadcast(Object message);
    void click(int id);
    Model model();
    void toggle(int id);
    Histories histories();
    String report(String id);
    boolean startServer();
    void stopServer();
    void startSimulating();
    boolean isSimulating();
    void stopSimulating();
    boolean isServerRunning();
    interface HasATablet {
        Tablet tablet();
        void setTablet(Tablet tablet);
        void setStatusText(String string);
    }
    enum Type {
        normal,speed,kryo;
    }
    interface Factory {
        Tablet create(Type type,Group group,String id,Model model);
        class FactoryImpl implements Factory {
            @Override public Tablet create(Type type,Group group,String id,Model model) {
                switch(type) {
                    case speed:
                        Server server=Server.factory.create(group.required(id),model);
                        return new TabletImpl1(group.clone(),id,server,group.getModelClone());
                    case normal:
                        return new TabletImpl2(group.clone(),id,model);
                    case kryo:
                        return new TabletKryoImpl(group.clone(),id,model);
                    default:
                        pl("strange type: "+type);
                        return null;
                }
            }
            public static abstract class TabletABC implements Tablet {
                public TabletABC(Group group,String tabletId,Model model,Histories histories) {
                    this.group=group;
                    required=group.required(tabletId);
                    this.tabletId=tabletId;
                    this.model=model;
                    if(!model.serialNumber.equals(group.getModelClone().serialNumber)) l.severe("models have different serial numbers! &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
                    this.histories=histories;
                }
                @Override public Group group() {
                    return group;
                }
                @Override public int tablets() {
                    return group.keys().size();
                }
                @Override public String tabletId() {
                    return tabletId;
                }
                @Override public Config config() {
                    return config;
                }
                @Override public Model model() {
                    return model;
                }
                @Override public void toggle(int id) {
                    Boolean state=!model().state(id);
                    model().setState(id,state);
                    Message message=messageFactory().normal(group().groupId,tabletId(),id,model().toCharacters());
                    broadcast(message);
                }
                // move this to model!
                // is this a controller?
                @Override public void click(int id) {
                    l.info("click: "+id+" in: "+this);
                    try {
                        if(1<=id&&id<=model().buttons) {
                            p("sync on: "+model());
                            synchronized(model()) {
                                if(model().resetButtonId!=null&&id==model().resetButtonId) {
                                    model().reset();
                                    Message message=messageFactory().other(Message.Type.reset,group().groupId,tabletId());
                                    broadcast(message);
                                } else {
                                    Boolean state=!model().state(id);
                                    model().setState(id,state);
                                    Message message=messageFactory().normal(group().groupId,tabletId(),id,model().toCharacters());
                                    broadcast(message);
                                }
                            }
                        } else {
                            l.warning(id+" is not a model button!");
                        }
                    } catch(Exception e) {
                        l.severe("click caught: "+e);
                        e.printStackTrace();
                    }
                }
                @Override public synchronized boolean isSimulating() {
                    return simulationTimer!=null;
                }
                @Override public synchronized void startSimulating() {
                    if(isSimulating()) stopSimulating();
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
                @Override public synchronized void stopSimulating() {
                    if(isSimulating()) {
                        simulationTimer.cancel();
                        simulationTimer=null;
                    }
                }
                @Override public Histories histories() {
                    return histories;
                }
                protected final Group group;
                protected final Required required;
                private final String tabletId;
                private final Model model;
                public Config config=new Config();
                public final Histories histories;
                private Timer simulationTimer;
                boolean stopDriving;
            }
            /* private */ public class TabletImpl1 extends TabletABC { // stays connected
                TabletImpl1(Group group,String id,Server server,Model model) {
                    super(group,id,model,server.histories());
                    this.server=server;
                }
                @Override public boolean startServer() {
                    return server.startServer();
                }
                @Override public boolean isServerRunning() {
                    return server!=null&&server.isServerRunning();
                }
                @Override public void stopServer() {
                    server.stopServer();
                }
                @Override public com.tayek.tablet.Message.Factory messageFactory() {
                    return server.messageFactory();
                }
                @Override public void broadcast(Object message) {
                    server.broadcast(message);
                }
                @Override public String report(String id) {
                    return id+": "+server.report();
                }
                public final Server server;
            }
        }
    }
    class Config implements Cloneable {
        public Config() {}
        public Config(boolean replying,boolean logErrors,Integer connectTimeout,Integer sendTimeout) {
            super();
            this.replying=replying;
            this.logErrors=logErrors;
            this.connectTimeout=connectTimeout;
            this.sendTimeout=sendTimeout;
        }
        @Override public Config clone() {
            Config config=new Config(replying,logErrors,connectTimeout,sendTimeout);
            return config;
        }
        // put all the switches in here
        // maybe all the static stuff in io also?
        public boolean replying;
        public boolean logErrors=defaultLogErrors;
        public Integer connectTimeout=defaultConnectTimeout; // set by tablet
        public Integer sendTimeout=defaultSendTimeout; // set by tablet
        @Override public String toString() {
            return "Config [replying="+replying+", logErrors="+logErrors+", connectTimeout="+connectTimeout+", sendTimeout="+sendTimeout+"]";
        }
        public static Boolean defaultLogErrors=false;
        public static Integer defaultConnectTimeout=1_000; // 40;
        public static Integer defaultSendTimeout=defaultConnectTimeout+50; // 60;
        public static Integer defaultDriveWait=200; // 100;
    }
    Factory factory=new Factory.FactoryImpl();
}

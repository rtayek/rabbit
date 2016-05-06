package com.tayek;
import static com.tayek.io.IO.*;
import java.util.*;
import com.tayek.Tablet.Config;
import com.tayek.speed.Server;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.*;
import com.tayek.tablet.Message.Type;
import com.tayek.tablet.MessageReceiver.Model;
public interface Tablet {
    Config config();
    Group group();
    String tabletId();
    int tablets();
    Message.Factory messageFactory();
    void broadcast(Object message);
    void click(int id);
    Model model();
    void toggle(int id);
    Histories histories();
    String report(String id);
    boolean isHeatbeatOn();
    void startHeatbeat();
    void stopHeartbeat();
    interface HasATablet {
        Tablet tablet();
        void setTablet(Tablet tablet);
        void setStatusText(String string);
    }
    interface Factory {
        Tablet create1(Group group,String id);
        Tablet create2(Group group,String id);
        class FactoryImpl implements Factory {
            @Override public TabletImpl1 create1(Group group,String id) {
                Server server=Server.factory.create(group.required(id));
                return new TabletImpl1(group.clone(),id,server,group.getModelClone());
            }
            @Override public TabletImpl2 create2(Group group,String id) {
                Group clone=group.clone();
                return clone.new TabletImpl2(id);
            }
            // second just needs an id?
            // first needs group or map: id->required
            public static abstract class TabletABC implements Tablet {
                public TabletABC(Group group,String tabletId,Model model,Histories histories) {
                    this.group=group;
                    required=group.required(tabletId);
                    this.tabletId=tabletId;
                    this.model=model;
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
                    try {
                        if(1<=id&&id<=model().buttons) synchronized(model()) {
                            if(model().resetButtonId!=null&&id==model().resetButtonId) {
                                model().reset();
                                Message message=messageFactory().other(Type.reset,group().groupId,tabletId());
                                broadcast(message);
                            } else {
                                Boolean state=!model().state(id);
                                model().setState(id,state);
                                Message message=messageFactory().normal(group().groupId,tabletId(),id,model().toCharacters());
                                broadcast(message);
                            }
                        }
                        else {
                            l.warning(id+" is not a model button!");
                        }
                    } catch(Exception e) {
                        l.severe("click caught: "+e);
                    }
                }
                @Override public Histories histories() {
                    return histories;
                }
                @Override public boolean isHeatbeatOn() {
                    return heartbeatTimer!=null;
                }
                @Override public void startHeatbeat() {
                    if(heartbeatTimer!=null) stopHeartbeat();
                    if(true) {
                        final int dt=500;
                        ArrayList<String> ids=new ArrayList<>(tablets());
                        l.info(""+System.currentTimeMillis());
                        heartbeatTimer=new Timer();
                        heartbeatTimer.schedule(new TimerTask() {
                            @Override public void run() {
                                broadcast(messageFactory().other(Type.heartbeat,group().groupId,tabletId()));
                            }
                        },1_000+ids.indexOf(tabletId())*dt,dt*tablets());
                    }
                }
                @Override public void stopHeartbeat() {
                    if(heartbeatTimer!=null) {
                        heartbeatTimer.cancel();
                        heartbeatTimer=null;
                    }
                }
                protected final Group group;
                protected final Required required;
                private final String tabletId;
                private final Model model;
                public Config config=new Config();
                public final Histories histories;
                protected Timer heartbeatTimer;
            }
            private class TabletImpl1 extends TabletABC { // stays connected
                TabletImpl1(Group group,String id,Server server,Model model) {
                    super(group,id,model,server.histories());
                    this.server=server;
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
                final Server server;
            }
        }
    }
    class Config implements Cloneable {
        public Config() {}
        public Config(boolean useExecutorService,boolean waitForSendCallable,boolean runCanceller,boolean replying,boolean logErrors,Integer connectTimeout,Integer sendTimeout) {
            super();
            this.useExecutorService=useExecutorService;
            this.waitForSendCallable=waitForSendCallable;
            this.runCanceller=runCanceller;
            this.replying=replying;
            this.logErrors=logErrors;
            this.connectTimeout=connectTimeout;
            this.sendTimeout=sendTimeout;
        }
        @Override public Config clone() {
            Config config=new Config(useExecutorService,waitForSendCallable,runCanceller,replying,logErrors,connectTimeout,sendTimeout);
            return config;
        }
        // put all the switches in here
        // maybe all the static stuff in io also?
        public boolean useExecutorService;
        public boolean waitForSendCallable=true;
        public boolean runCanceller;
        public boolean replying;
        public boolean logErrors=false;
        public Integer connectTimeout=defaultConnectTimeout; // set by tablet
        public Integer sendTimeout=defaultSendTimeout; // set by tablet
        @Override public String toString() {
            return "Config [replying="+replying+", connectTimeout="+connectTimeout+", sendTimeout="+sendTimeout+", waitForSendCallable="+waitForSendCallable+", useExecutorService="+useExecutorService
                    +", runCanceller="+runCanceller+"]";
        }
        public static Integer defaultConnectTimeout=1_000; // 40;
        public static Integer defaultSendTimeout=defaultConnectTimeout+50; // 60;
        public static Integer defaultDriveWait=200; // 100;
    }
    Factory factory=new Factory.FactoryImpl();
}

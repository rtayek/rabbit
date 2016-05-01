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
    String groupId();
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
        Tablet create1(String groupId,int tablets,String id,Required required,Model model);
        Tablet create2(String id,Group group,Model model);
        class FactoryImpl implements Factory {
            @Override public TabletImpl1 create1(String groupId,int tablets,String id,Required required,Model model) {
                Server server=Server.factory.create(required);
                return new TabletImpl1(groupId,tablets,id,server,model);
            }
            @Override public TabletImpl2 create2(String id,Group group,Model model) {
                p("required: "+group.required(id));
                return group.clone().new TabletImpl2(id,group.keys().size(),group.required(id),model);
            }
            // second just needs an id?
            // first needs group or map: id->required
            public static abstract class TabletABC implements Tablet {
                public TabletABC(String groupId,int tablets,String tabletId,Model model,Histories histories) {
                    this.groupId=groupId;
                    this.tablets=tablets;
                    this.tabletId=tabletId;
                    this.model=model;
                    this.histories=histories;
                }
                @Override public String groupId() {
                    return groupId;
                }
                @Override public int tablets() {
                    return tablets;
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
                    Message message=messageFactory().normal(groupId(),tabletId(),id,model().toCharacters());
                    broadcast(message);
                }
                // move this to model!
                // is this a controller?
                @Override public void click(int id) {
                    if(1<=id&&id<=model().buttons) synchronized(model()) {
                        if(model().resetButtonId!=null&&id==model().resetButtonId) {
                            model().reset();
                            Message message=messageFactory().other(Type.reset,groupId(),tabletId());
                            broadcast(message);
                        } else {
                            Boolean state=!model().state(id);
                            model().setState(id,state);
                            Message message=messageFactory().normal(groupId(),tabletId(),id,model().toCharacters());
                            broadcast(message);
                        }
                    }
                    else {
                        l.warning(id+" is not a model button!");
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
                                broadcast(messageFactory().other(Type.heartbeat,groupId,tabletId()));
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
                private final String groupId,tabletId;
                private final Model model;
                public final int tablets;
                public Config config=new Config(); // maybe pass in ctor later? - maybe not!
                public final Histories histories;
                protected Timer heartbeatTimer;
            }
            private class TabletImpl1 extends TabletABC { // stays connected
                TabletImpl1(String groupId,int tablets,String id,Server server,Model model) {
                    super(groupId,tablets,id,model,server.histories());
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
    class Config {
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
        public static Integer defaultConnectTimeout=1_000; // 40;
        public static Integer defaultSendTimeout=defaultConnectTimeout+50; // 60;
        public static Integer defaultDriveWait=200; // 100;
    }
    Factory factory=new Factory.FactoryImpl();
}

package com.tayek;
import static com.tayek.io.IO.*;
import com.tayek.speed.Server;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.*;
import com.tayek.tablet.Message.Type;
import com.tayek.tablet.MessageReceiver.Model;
public interface Tablet {
    String groupId();
    String tabletId();
    Message.Factory messageFactory();
    void broadcast(Object message);
    void click(int id);
    Model model();
    void toggle(int id);
    Histories histories();
    String report(String id);
    interface HasATablet {
        Tablet tablet();
        void setTablet(Tablet tablet);
    }
    interface Factory {
        Tablet create1(String groupId,String id,Required required,Model model);
        Tablet create2(String id,Group group,Model model);
        class FactoryImpl implements Factory {
            @Override public TabletImpl1 create1(String groupId,String id,Required required,Model model) {
                Server server=Server.factory.create(required);
                return new TabletImpl1(groupId,id,server,model);
            }
            @Override public TabletImpl2 create2(String id,Group group,Model model) {
                p("required: "+group.required(id));
                return group.clone().new TabletImpl2(id,group.required(id),model);
            }
            // second just needs an id?
            // first needs group or map: id->required
            public static abstract class TabletABC implements Tablet {
                public TabletABC(String groupId,String tabletId,Model model,Histories histories) {
                    this.groupId=groupId;
                    this.tabletId=tabletId;
                    this.model=model;
                    this.histories=histories;
                }
                @Override public String groupId() {
                    return groupId;
                }
                @Override public String tabletId() {
                    return tabletId;
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
                private final String groupId,tabletId;
                private final Model model;
                public final Histories histories;
                }
            private class TabletImpl1 extends TabletABC { // stays connected
                TabletImpl1(String groupId,String id,Server server,Model model) {
                    super(groupId,id,model,server.histories());
                    this.server=server;
                }
                @Override public String groupId() {
                    return null;
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
    Factory factory=new Factory.FactoryImpl();
}

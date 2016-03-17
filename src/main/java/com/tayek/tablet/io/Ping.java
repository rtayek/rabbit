package com.tayek.tablet.io;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Level;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.*;
import com.tayek.tablet.Messages.Message;
import com.tayek.tablet.MessageReceiver.Model;
import static com.tayek.tablet.io.IO.*;
public class Ping {
    public static void main(String[] args) throws InterruptedException {
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.ALL);
        Map<Object,Info> infos=new Groups().groups.get("g0");
        infos.put(99,new Info("T99","localhost",Main.defaultReceivePort));
        Group group=new Group(1,infos,Model.mark1,false);
        Tablet tablet=group.create(99);
        group=null; // tablet has a clone of group.
        tablet.startListening();
        Message message=tablet.group.messages.ping(tablet.group.groupId,tablet.tabletId());
        // not working on the tablets
        // because they don't have a socket address for tablet 99
        // so how do we handle this?
        // really do not want to put in a hack for this.
        // maybe refactor client and send to use an ip address?
        if(true) 
            tablet.broadcast(message,0);
        else for(Object tabletId:group.info().keySet()) {
            // use tablet.group if false
            p("ping tablet: "+tabletId);
            Future<Void> future=tablet.group.executeTaskAndCancelIfItTakesTooLong(tablet.group.new SendCallable(tablet,message,tabletId,tablet.group.connectTimeout),tablet.group.connectTimeout+100,tablet.group.canceller);
            Thread.sleep(100);
        }
        // Client.report(tablet);
    }
}

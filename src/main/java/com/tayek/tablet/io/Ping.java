package com.tayek.tablet.io;
import java.net.*;
import java.util.Map;
import java.util.logging.Level;
import com.tayek.*;
import com.tayek.io.LoggingHandler;
import com.tayek.tablet.*;
import com.tayek.tablet.Group.*;
import com.tayek.tablet.Message.Type;
import com.tayek.tablet.MessageReceiver.Model;
import static com.tayek.io.IO.*;
public class Ping {
    public static void main(String[] args) throws InterruptedException {
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.ALL);
        Map<String,Required> requireds=new Groups().groups.get("g0");
        String tabletId=aTabletId(99);
        requireds.put(tabletId,new Required(tabletId,"localhost",defaultReceivePort));
        Group group=new Group("1",requireds,Model.mark1);
        Model model=group.getModelClone();
        Tablet tablet=Tablet.factory.create2(tabletId,group,model);
        group=null; // tablet has a clone of group.
        ((TabletImpl2)tablet).startListening();
        Message message=tablet.messageFactory().other(Type.ping,tablet.groupId(),tablet.tabletId());
        // not working on the tablets
        // because they don't have a socket address for tablet 99
        // so how do we handle this?
        // really do not want to put in a hack for this.
        // maybe refactor client and send to use an ip address?
        tablet.broadcast(message);
        // Client.report(tablet);
    }
}

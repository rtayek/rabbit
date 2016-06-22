package com.tayek.tablet.io;
import java.net.*;
import java.util.Map;
import java.util.logging.Level;
import javax.sound.sampled.ReverbType;
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
        String tabletId=Groups.add("localhost",defaultReceivePort,requireds);
        Group group=new Group("1",requireds,Model.mark1);
        Tablet tablet=Tablet.factory.create(Tablet.Type.normal,group,tabletId,group.getModelClone());
        group=null; // tablet has a clone of group.
        tablet.startServer();
        Message message=tablet.messageFactory().other(Type.ping,tablet.group().groupId,tablet.tabletId());
        // not working on the tablets
        // because they don't have a socket address for tablet 99
        // so how do we handle this?
        // really do not want to put in a hack for this.
        // maybe refactor client and send to use an ip address?
        tablet.broadcast(message);
        // Client.report(tablet);
    }
}

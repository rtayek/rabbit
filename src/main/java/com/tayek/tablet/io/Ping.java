package com.tayek.tablet.io;
import java.net.*;
import java.util.Map;
import java.util.logging.Level;
import com.tayek.tablet.*;
import com.tayek.tablet.Main.Stuff;
import static com.tayek.tablet.Main.Stuff.*;
import com.tayek.tablet.MessageReceiver.Model;
import com.tayek.tablet.Messages.*;
public class Ping {
    public static void main(String[] args) throws InterruptedException {
        LoggingHandler.init();
        LoggingHandler.setLevel(Level.ALL);
        Map<String,Info> infos=new Groups().groups.get("g0");
        String tabletId=Stuff.aTabletId(99);
        infos.put(tabletId,new Info(tabletId,"localhost",Main.defaultReceivePort));
        Stuff stuff=new Stuff(1,infos,Model.mark1);
        Tablet tablet=Tablet.create(stuff,tabletId);
        stuff=null; // tablet has a clone of group.
        SocketAddress socketAddress=tablet.stuff.socketAddress(tablet.tabletId());
        tablet.startListening(socketAddress);
        Message message=tablet.stuff.messages.other(Type.ping,tablet.groupId,tablet.tabletId());
        // not working on the tablets
        // because they don't have a socket address for tablet 99
        // so how do we handle this?
        // really do not want to put in a hack for this.
        // maybe refactor client and send to use an ip address?
        tablet.broadcast(message,stuff);
        // Client.report(tablet);
    }
}
